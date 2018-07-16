package services.redisProtocolServer

import scala.collection.mutable.ArrayBuffer

object CommandParser {
  def parseCommand(commandToParse: String): Seq[RedisCommand] = {
    var tokenBuffer: ArrayBuffer[Char] = ArrayBuffer[Char]()
    var args: ArrayBuffer[String] = ArrayBuffer()
    var startOfCommand = false
    var wordCountHeader = false
    var parseWord = false
    var numWords = -1
    var numLetters = -1
    var commands: ArrayBuffer[RedisCommand] = ArrayBuffer[RedisCommand]()
    commandToParse.foreach { char: Char =>
      char match {
        case '*' =>
          startOfCommand = true
        case '$' =>
          wordCountHeader = true
        case '\r' => // eat token
        case '\n' =>
          if (startOfCommand) {
            numWords = tokenBuffer.mkString.toInt
            tokenBuffer = ArrayBuffer[Char]()
            startOfCommand = false
          } else if (wordCountHeader) {
            numLetters = tokenBuffer.mkString.toInt
            tokenBuffer = ArrayBuffer[Char]()
            parseWord = true
            wordCountHeader = false
          }
        case _: Char =>
          tokenBuffer.append(char)
          if (parseWord) {
            numLetters -= 1
            if (numLetters == 0) {
              args.append(tokenBuffer.mkString)
              tokenBuffer = ArrayBuffer()
              numWords -= 1
              if (numWords == 0) {
                commands += RedisCommand(args.head, args.tail)
                args = ArrayBuffer()
              }
            }
          }
      }
    }
    commands
  }
}
