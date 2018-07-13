package services

import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import redis.RedisClient
import services.util.ConcurrentCache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Provides GET interface for cache operations
  * Delegates cache operations to underlying cache,
  * and updates underlying cache with results from redis
  */

@Singleton
class RedisCache @Inject()(redis: RedisClient,
                           concurrentCache: ConcurrentCache) {

  private val logger = LoggerFactory.getLogger(getClass)

  def get(key: String): Future[String] = {
    concurrentCache.get(key) match {
      case Some(value) =>
        logger.info("Cache GET successful, key: " + key)
        Future.successful(value)
      case _ =>
        logger.info("Cache GET unsuccessful, requesting from redis, key: " + key)
        getFromRedis(key)
    }
  }

  def getFromRedis(key: String): Future[String] = {
    redis.get(key).map {
      case Some(byteString) =>
        val strVal = byteString.utf8String
        concurrentCache.add(key, strVal)
        strVal
      case _ =>
        logger.info("Redis does not contain key: " + key)
        ""
    }
  }
}
