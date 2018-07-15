package controllers

import modules.ProxyModule
import org.mockito.Mockito._
import org.scalatest.{MustMatchers, WordSpec}
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test._
import services.RedisCache

import scala.concurrent.Future

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  *
  * For more information, see https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
  */
class HomeControllerSpec extends WordSpec with GuiceOneAppPerTest with Injecting with MockitoSugar with MustMatchers {

  override def fakeApplication(): Application = {
    new GuiceApplicationBuilder()
      .disable[ProxyModule]
      .bindings(new TestModule)
      .build()
  }

  "A GET request to the HomeController" should {
    "return the json result from redis/redis cache" in {
      val testKey = "test"
      val testValue = "testVal"
      val mockRedisCache = mock[RedisCache](RETURNS_DEEP_STUBS)
      when(mockRedisCache.get(testKey)).thenReturn(Future.successful(testValue))
      val controller = new HomeController(stubControllerComponents(), mockRedisCache)
      val home = controller.get("test").apply(FakeRequest(GET, "/" + testKey))

      status(home) mustBe OK
      contentType(home) mustBe Some("application/json")
      contentAsJson(home) mustBe Json.toJson(testValue)
    }
  }
}
