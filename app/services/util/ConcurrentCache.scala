package services.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantReadWriteLock

import akka.actor.{ActorSystem, Cancellable}
import javax.inject.{Inject, Singleton}
import play.api.Configuration

import scala.collection.JavaConverters._
import scala.collection.concurrent
import scala.concurrent.duration.{FiniteDuration, MILLISECONDS}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Provides O(1) lookups via a backing hashmap,
  * LRU semantics with a backing linked list.
  *
  * Node existence between the hashmap and linkedlist
  * are synchronized to ensure correctness of the lookup
  * and LRU properties above
  *
  * @param linkedList to enable LRU behaviour
  * @param config to get the configured ttl and capacity
  */

@Singleton
class ConcurrentCache @Inject()(linkedList: LinkedList,
                                config: Configuration) {

  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  val ttl = FiniteDuration(config.get[Long]("cache.expiry_time"), MILLISECONDS)
  val capacity: Int = config.get[Int]("cache.capacity")

  var cache: concurrent.Map[String, (Node, Cancellable)] =
    new ConcurrentHashMap[String, (Node, Cancellable)]().asScala

  val lock = new ReentrantReadWriteLock

  def get(key: String): Option[String] = {
    lock.readLock().lock()
    try {
      cache.get(key) match {
        case Some(value) =>
          Some(value._1.value)
        case _ => None
      }
    } finally {
      lock.readLock().unlock()
    }
  }

  def remove(key: String): Unit = synchronized {
    lock.writeLock().lock()
    try {
      cache.remove(key) match {
        case Some(value) =>
          linkedList.remove(value._1)
        case _ => // already removed
      }
    } finally {
      lock.writeLock().unlock()
    }
  }

  def add(key: String, value: String): Unit = synchronized {
    lock.writeLock().lock()
    try {
      cache.remove(key) match {
        case Some(lookupValue) =>
          lookupValue._2.cancel()
          linkedList.remove(lookupValue._1)
        case _ => // already gone
      }

      val newNode = linkedList.add(value)
      cache.put(key, (newNode, akkaSystem.scheduler.scheduleOnce(ttl, () => remove(key))))
    } finally {
      lock.writeLock().unlock()
    }
  }
}
