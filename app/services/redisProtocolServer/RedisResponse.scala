package services.redisProtocolServer

object RedisResponse {
  val OK =  "+OK\r\n"
  val ERR = "-ERR\r\n"
  val COMMAND = "1) 1) \"get\"\n   2) (integer) 2\n   3) 1) readonly\n   4) (integer) 1\n   5) (integer) 1\n   6) (integer) 1"
}
