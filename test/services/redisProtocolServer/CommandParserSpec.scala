package services.redisProtocolServer

import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mockito.MockitoSugar

import scala.collection.mutable

class CommandParserSpec extends WordSpec with MustMatchers with MockitoSugar {
  "Redis Command Parser" should {
    "parse a single command" in {
      val commands = "*3\r\n$3\r\nset\r\n$3\r\nkey\r\n$5\r\nvalue\r\n"
      CommandParser.parseCommand(commands) mustBe mutable.Seq(
        RedisCommand("set", Seq("key", "value"))
      )
    }
    "parse multiple commands" in {
      val commands = "*3\r\n$3\r\nset\r\n$3\r\nkey\r\n$5\r\nvalue\r\n*2\r\n$3\r\nget\r\n$3\r\nkey\r\n"
      CommandParser.parseCommand(commands) mustBe mutable.Seq(
        RedisCommand("set", Seq("key", "value")),
        RedisCommand("get", Seq("key"))
      )
    }
  }
}
