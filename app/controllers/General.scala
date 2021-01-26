package controllers

import javax.inject._
import play.api.libs.json.Json
import play.api.mvc._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class General @Inject()(cc: MessagesControllerComponents)
                       (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  def index(): Action[AnyContent] = Action.async { implicit request =>
    Future(Ok(
      Json.toJson(Map(
        "Comments" -> Json.toJson("Welcome to the Machine!"),
        "Help"     -> Json.toJson( Map(
          "one record end points" -> Json.arr(
            Json.toJson(Map("insert" -> "POST   : /comments     ", "body" -> "{'content':'new comment text'}" )),
            Json.toJson(Map("update" -> "PUT    : /comments/{id}", "body" -> "{'content':'updated comment text'}" )),
            Json.toJson(Map("select" -> "GET    : /comments/{id}", "body" -> "None" )),
            Json.toJson(Map("delete" -> "DELETE : /comments/{id}", "body" -> "None" ))
          ),
          "many records end point" -> Json.toJson(
            Json.toJson(Map("select" -> "GET : /comments?filter={filter}&prefix={prefix}&sort={[L|S|D]}")),
            Json.toJson(Map("prefix" -> "optional parameter; adds prefix to the listed comments")),
            Json.toJson(Map("filter" -> "optional parameter; logical operands added: (AND -> *), (OR -> ,), (NOT -> -) and rounded brackets")),
            Json.toJson(Map("sort"   -> "optional parameter; allowed values (L)exical,(S)tring,(D)ictionary"))
          )
        ))
      ))
    ))
  }

  def notFound(path: String): Action[AnyContent] = Action.async { implicit request =>
    Future(NotFound( Json.toJson(Map("result" -> Json.toJson(false), "reason" -> Json.toJson("Path not found")))))
  }

  def methodNotAllowed(): Action[AnyContent] = Action.async { implicit request =>
    Future(MethodNotAllowed( Json.toJson(Map("result" -> Json.toJson(false), "reason" -> Json.toJson("Method not allowed for the chosen path")))))
  }
}
