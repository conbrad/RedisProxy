package services.redisProtocolServer

object RedisTokens {
  val SET = "set"
  val GET = "get"
  val COMMAND = "command"
  val commandResponse = "1) 1) \"get\"\n   2) (integer) 2\n   3) 1) readonly\n   4) (integer) 1\n   5) (integer) 1\n   6) (integer) 1"

  def opType(redisCommand: RedisCommand): Option[String] = {
    redisCommand.op.toLowerCase match {
      case SET => Some(SET)
      case GET => Some(GET)
      case COMMAND => Some(COMMAND)
      case _ => None
    }
  }
}
