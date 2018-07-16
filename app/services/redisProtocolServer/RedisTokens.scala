package services.redisProtocolServer

object RedisTokens {
  val SET = "set"
  val GET = "get"

  def opType(redisCommand: RedisCommand): Option[String] = {
    redisCommand.args.headOption.getOrElse("").toLowerCase match {
      case SET => Some(SET)
      case GET => Some(GET)
      case _ => None
    }
  }
}
