package services.redisProtocolServer

object RedisResponse {
  val OK =  "*1\r\n$3\r\n+OK"
  val ERR = "*1\r\n$4\r\n-ERR"
}
