package services.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantReadWriteLock

import akka.actor.{ActorSystem, Cancellable}
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.collection.concurrent
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}

/**
  * Provides O(1) lookups/insertions via a backing hashmap
  * and O(1) LRU semantics with a backing linked list.
  *
  * Node existence between the hashmap and linkedlist
  * are synchronized to ensure correctness of the lookup
  * and LRU properties above
  *
  * @param linkedList to enable LRU behaviour
  * @param config     to get the configured ttl and capacity
  */

@Singleton
class ConcurrentCache @Inject()(linkedList: LinkedList,
                                config: Configuration) {

  private val logger = LoggerFactory.getLogger(getClass)

  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  val ttl = FiniteDuration(config.get[Long]("cache.expiry_time"), MILLISECONDS)

  val capacity: Long = config.get[Long]("cache.capacity")
  val count = new AtomicLong(0L)

  var cache: concurrent.Map[String, (Node, Cancellable)] =
    new ConcurrentHashMap[String, (Node, Cancellable)]().asScala

  val lock = new ReentrantReadWriteLock

  logger.info(s"Cache created with ttl: $ttl millis, capacity: $capacity")

  /**
    * Return the value mapped by key if it exists, else return None
    * Fires and forgets a LRU/TTL update asynchronously to avoid
    * having the client wait for a locking update.
    *
    * Does not use a read lock, meaning:
    * 1) We can read inconsistent state
    * 2) We reduce
    *
    * Since the underlying hashmap is a ConcurrentHashMap, it guarantees
    * a serialized happens-before property of updates, meaning we avoid
    * benign data races.
    *
    * The lookup takes O(1) time.
    *
    * @param key lookup key that maps to associated value
    * @return value the lookup key is associated with
    */

  def get(key: String): Option[String] = {
    cache.get(key) match {
      case Some(value) =>
        // fire and forget access update
        Future {
          updateAccess(key)
        }
        Some(value._1.value)
      case _ => None
    }
  }

  private def updateAccess(key: String): Unit = {
    lock.writeLock().lock()
    try {
      cache.get(key) match {
        case Some(value) =>
          remove(key)
          add(key, value._1.value)
        case None =>
          logger.info(s"Access update, key: $key already removed")
      }

    } finally {
      lock.writeLock().unlock()
    }
  }

  /**
    * Inserts the key/value pair to the cache. We first remove the
    * entry (if it exists) in both the backing hashmap and linkedlist so
    * we can cancel the existing TTL and remove it's current position
    * in the LRU sequence. Then we add it to the hashmap and linked list,
    * creating a new TTL and inserting it at the tail of the linked list
    * effectively making it the MRU item.
    *
    * We need to
    * acquire a lock here because this critical section involves
    * 2 resources that are required to be consistent with one another;
    * the backing hash map, and the linked list. The overall update
    * takes O(1) time.
    *
    * Since there's two underlying data structures there's no way
    * (that I know of) to update both in a lock-free manner, assuming
    * a CAS update.
    *
    * @param key   lookup key that maps to associated value
    * @param value value the lookup key is associated with
    */

  def add(key: String, value: String): Unit = {
    lock.writeLock().lock()
    if (count.get() >= capacity) {
      evictLRU()
    }
    try {
      remove(key)
      val newNode = linkedList.addFirst(key, value)
      cache.put(key, (newNode, akkaSystem.scheduler.scheduleOnce(ttl, () => remove(key))))
      count.incrementAndGet()
    } finally {
      lock.writeLock().unlock()
    }
  }

  /**
    * Inserts the key/value pair to the cache. We need to
    * acquire a lock here because this critical section involves
    * 2 resources that are required to be consistent with one another;
    * the backing hash map, and the linked list. The overall update
    * takes O(1) time.
    *
    * Since there's two underlying data structures there's no way
    * (that I know of) to update both in a lock-free manner, assuming
    * a CAS update.
    *
    * @param key lookup key that maps to associated value
    */

  def remove(key: String): Unit = {
    lock.writeLock().lock()
    try {
      cache.remove(key) match {
        case Some(value) =>
          linkedList.remove(value._1)
          value._2.cancel()
          count.decrementAndGet()
        case _ => // already removed
      }
    } finally {
      lock.writeLock().unlock()
    }
  }

  def evictLRU(): Unit = {
    // reentrant lock just increments a counter
    // when recursively grabbing locks by the same thread
    lock.writeLock().lock()
    try {
      linkedList.leastRecentlyUsed() match {
        case Some(node) =>
          cache.remove(node.key) match {
            case Some(value) =>
              value._2.cancel()
              count.decrementAndGet()
            case _ =>
              logger.error(s"Key: ${node.key} was in linked list but not cache")
          }
        case _ =>
          logger.error("Eviction when linked list is empty")
      }
    } finally {
      lock.writeLock().unlock()
    }
  }
}
