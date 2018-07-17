package services.redisProtocolServer

object RedisResponse {
  val OK =  "*1\r\n$3\r\n+OK"
  val ERR = "*1\r\n$4\r\n-ERR"
  val COMMAND = "1) 1) \"get\"\n   2) (integer) 2\n   3) 1) readonly\n   4) (integer) 1\n   5) (integer) 1\n   6) (integer) 1"
}
