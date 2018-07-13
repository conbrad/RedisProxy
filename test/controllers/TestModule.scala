package controllers

import com.google.inject.{AbstractModule, Provides, Singleton}
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import play.api.Configuration
import redis.RedisClient

class TestModule extends AbstractModule with MockitoSugar {

  override def configure(): Unit = {}

  @Provides
  @Singleton
  def redisClient(configuration: Configuration): RedisClient = {
    mock[RedisClient](RETURNS_DEEP_STUBS)
  }
}
