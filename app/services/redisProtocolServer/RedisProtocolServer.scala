package services.redisProtocolServer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Source, Tcp}
import akka.util.ByteString
import javax.inject.{Singleton, _}
import play.api.Configuration
import redis.RedisClient
import services.RedisCache

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

@Singleton
class RedisProtocolServer @Inject()(config: Configuration,
                                    redisCache: RedisCache,
                                    redis: RedisClient) {
  val host: String = config.get[String]("protocolserver.host")
  val port: Int = config.get[Int]("protocolserver.port")

  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val connections: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind(host, port)
  connections runForeach { connection =>
    // server logic, parses incoming commands
    val commandSender = Flow[String].map(command => {
      val redisCommands = CommandParser.parseCommand(command)
      redisCommands.map(redisCommand => {
        RedisTokens.opType(redisCommand) match {
          case Some(RedisTokens.GET) =>
            // We block here, unfortunately
            Await.result(redisCache.get(redisCommand.args.last), Duration.Inf)
          case Some(RedisTokens.SET) =>
            redis.set(redisCommand.args(1), redisCommand.args(2))
              .map {
                case true => RedisResponse.OK
                case false => RedisResponse.ERR
              }
          case Some(RedisTokens.COMMAND) =>
            RedisResponse.COMMAND
          case _ => RedisResponse.ERR
        }
      })
    })

    val serverLogic = Flow[ByteString]
      .via(commandAggregator)
      .map(_.utf8String)
      .via(commandSender)
      .map(result => {
        val unwrappedResult = result.headOption.getOrElse("").toString
        val redisResult = "$" + unwrappedResult.length + "\r\n" + unwrappedResult + "\r\n"
        ByteString(redisResult)
      })

    connection.handleWith(serverLogic)
  }

  private val commandAggregator = Flow[ByteString].statefulMapConcat { () =>
    byteString =>
      val command = byteString.utf8String
        .mkString("")
      List(ByteString(command))
  }

}
