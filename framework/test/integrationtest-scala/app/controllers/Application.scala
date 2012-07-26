package controllers

import play.api._
import libs.json.Json
import play.api.mvc._

import play.api.Play.current

object Application extends Controller {
  
  def index(name: String) = Action {
      Ok(views.html.index(name))
  }
  
  def key = Action {
    Play.configuration.getString("key").map {
      key => Ok("Key=" + key)
    }.getOrElse(InternalServerError("Configuration missing"))
  }

  def jsonWithContentType = Action { request =>
    request.headers.get("AccEPT") match {
      case Some("application/json") =>  {
        val acceptHdr = request.headers.toMap.collectFirst{ case (header,valueSeq) if header.equalsIgnoreCase("Accept") => (header, valueSeq) }
        acceptHdr.map{
          case (name,value) => Ok("{\""+name+"\":\""+ value.head+ "\"}").as("application/json")
        }.getOrElse(InternalServerError)
      }
      case _ => UnsupportedMediaType

    }
  }

  def jsonWithContentTypeAndCharset = Action {
    Ok("{}").as("application/json; charset=utf-8")
  }

  def json = Action { request =>
    request.body.asJson.map { json =>
      Ok(json)
    }.getOrElse(
      Ok(Json.toJson(Map("status" -> "KO", "message" -> "JSON Body missing")))
    )

  }
  
}