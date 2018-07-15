package controllers

import javax.inject._
import play.api.libs.json.{JsObject, JsString}
import play.api.mvc._
import services.RedisCache

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               redisCache: RedisCache) extends AbstractController(cc) {

  def get(key: String): Action[AnyContent] = Action.async {
    redisCache.get(key).map(result => Ok(
      JsObject(
        Map(
          "result" -> JsString(result)
        ))
    ))
  }
}
