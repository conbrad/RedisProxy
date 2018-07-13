package services

import akka.actor.ActorSystem
import akka.util.ByteString
import javax.inject._
import play.api._
import redis.RedisClient

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class RedisService @Inject()(config: Configuration) {
  val address: String = config.get[String]("redis.address")
  val port: Int = config.get[Int]("redis.port")
  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()

  val redis = RedisClient(address, port)

  def get(key: String): Future[String] = {
    redis.get(key)
      .map(_.getOrElse(ByteString.empty).utf8String)
  }

  def set(key: String, value: String): Future[Boolean] = {
    redis.set(key, value)
  }

}