
import akka.actor.ActorSystem
import com.google.inject.{AbstractModule, Provides, Singleton}
import play.api.{Configuration, Environment}
import redis.RedisClient

class ProxyModule(environment: Environment,
                  config: Configuration) extends AbstractModule {
  override def configure(): Unit = {}

  @Provides
  @Singleton
  def redis(configuration: Configuration): RedisClient = {
    implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
    val address: String = config.get[String]("redis.address")
    val port: Int = config.get[Int]("redis.port")
    RedisClient(address, port)
  }
}
