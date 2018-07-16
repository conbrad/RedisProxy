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

  logger.info(s"Connecting to redis protocol server on host: $host and port: $port")
  logger.info("Setting test keys")
  for(i <- 1 to 5) {
    logger.info(s"Setting key: test$i to value: test$i")
    val key = "test" + i
    val value = "test" + i
    out.print("*3\r\n$3\r\nset\r\n$5\r\n" + key + "\r\n$5\r\n" + value + "\r\n")
    out.print("*2\r\n$3\r\nget\r\n$5\r\n" + key + "\r\n")
    out.flush()
    logger.info("Received: " + in.next())
  }

  socket.close()
}