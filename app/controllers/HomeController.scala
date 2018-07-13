package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import services.RedisService

import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               redisCache: RedisService) extends AbstractController(cc) {

  def get(key: String): Action[AnyContent] = Action.async {
    redisCache.get(key).map(result => Ok(Json.toJson(result)))
  }
}
