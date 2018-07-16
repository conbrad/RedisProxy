package services.redisProtocolServer

case class RedisCommand(op: String, args: Seq[String])
