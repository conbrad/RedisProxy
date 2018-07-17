package services.redisProtocolServer

object RedisTokens {
  val SET = "set"
  val GET = "get"
  val COMMAND = "command"

  def opType(redisCommand: RedisCommand): Option[String] = {
    redisCommand.op.toLowerCase match {
      case SET => Some(SET)
      case GET => Some(GET)
      case COMMAND => Some(COMMAND)
      case _ => None
    }
  }
}
