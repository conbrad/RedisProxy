package tools

import java.io._
import java.net._

import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.Configuration

import scala.io._

@Singleton
class TcpClient @Inject()(config: Configuration) {
  private val logger = LoggerFactory.getLogger(getClass)
  val host: String = config.get[String]("protocolserver.host")
  val port: Int = config.get[Int]("protocolserver.port")
  val numTestValues: Int = config.get[Int]("protocolserver.numTestValues")

  val socket = new Socket(InetAddress.getByName(host), port)
  val in = new BufferedSource(socket.getInputStream).getLines()
  val out = new PrintStream(socket.getOutputStream)

  logger.info(s"Connecting to redis protocl server on host: $host and port: $port")
  logger.info("Setting test keys")
  for(i <- 1 to numTestValues) {
    logger.info(s"Setting key: test$i to value: test$i")
    out.println(s"set test$i value$i")
    out.flush()
    logger.info("Received: " + in.next())
  }

  socket.close()
}