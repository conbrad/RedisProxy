package services

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Tcp.{IncomingConnection, ServerBinding}
import akka.stream.scaladsl.{Flow, Framing, Source, Tcp}
import akka.util.ByteString
import javax.inject.{Singleton, _}
import play.api.Configuration

import scala.concurrent.Future

@Singleton
class RedisProtocolServer @Inject()(config: Configuration,
                                    redisCache: RedisCache) {
  val host: String = config.get[String]("protocolserver.host")
  val port: Int = config.get[Int]("protocolserver.port")

  implicit val akkaSystem: ActorSystem = akka.actor.ActorSystem()
  implicit val materializer: ActorMaterializer = ActorMaterializer()

  val connections: Source[IncomingConnection, Future[ServerBinding]] =
    Tcp().bind(host, port)
  connections runForeach { connection =>
    // server logic, parses incoming commands
    val commandParser = Flow[String].takeWhile(_ != "BYE").map(_ + "!")

    val prompt = s"$host:$port>"

    val serverLogic = Flow[ByteString]
      .via(Framing.delimiter(
        ByteString("\n"),
        maximumFrameLength = 256,
        allowTruncation = true))
      .map(_.utf8String)
      .via(commandParser)
      .merge(Source.single(prompt))
      .map(_ + "\n")
      .map(ByteString(_))

    connection.handleWith(serverLogic)
  }
}
