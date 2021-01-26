package controllers

import javax.inject._
import models._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import utils.LikeParser

import scala.concurrent.{ExecutionContext, Future}

case class CommentForm(content: String)

@Singleton
class Comments @Inject()(repo: CommentsRepository, cc: MessagesControllerComponents)
                                 (implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {
  import scala.language.implicitConversions

  def select(id: Long, prefix: String = ""): Action[AnyContent] = Action.async { implicit request =>
    repo.select(id).map { comments =>
      if ( comments.nonEmpty ) Ok({ Json.toJson(comments.map(c => Comment(c.id,prefix+c.content))) })
      else
        NotFound( Json.toJson(Map("result" -> Json.toJson(false), "reason" -> Json.toJson("Item not found"))))
    }
  }

  val commentForm: Form[CommentForm] = Form {
    mapping("content" -> nonEmptyText )(CommentForm.apply)(CommentForm.unapply)
  }
  def insert: Action[AnyContent] = Action.async { implicit request =>
  commentForm.bindFromRequest.fold(
    errorForm => Future(InternalServerError),
    comment => { repo.insert(comment.content).map { comments => Ok(Json.toJson(Map("id" -> comments.id)))}}
  )}

  def update(id: Long): Action[AnyContent] = Action.async { implicit request =>
    /* NoContent TO DO */
    commentForm.bindFromRequest.fold(
      errorForm => Future(Status(422)),
      comment => {
        repo.update(id,comment.content).map { comments =>
          if (comments) Ok("{\"result\":true}")
          else NotFound( Json.toJson(Map("result" -> Json.toJson(false), "reason" -> Json.toJson("Item not found"))))
        }
      }
    )}

  def delete(id: Long): Action[AnyContent] = Action.async { implicit request =>
    repo.delete(id).map { comments =>
      if (comments) Ok("{\"result\":true}")
      else NotFound( Json.toJson(Map("result" -> Json.toJson(false), "reason" -> Json.toJson("Item not found"))))
    }
  }

  def query(sort: String = "", filter: String = "", prefix: String = "", offset: Int = 0, limit: Int = -1): Action[AnyContent]
  = Action.async { implicit request => {

    val allowedSorting = List("", "L", "S", "D")
    if (!allowedSorting.contains(sort)) {
      Future(NotImplemented(Json.toJson(Map("result" -> Json.toJson(false),
        "reason" -> Json.toJson("Sort type not implemented. Allowed types: " + allowedSorting.map("'" + _ + "'"))))))
    } else {
      val finalFilter = filter.replaceAll("[^()-*\\w\\s,]", "").toLowerCase
      LikeParser.count(finalFilter) match {
        case Some(ilike) =>
          repo.query(sort, ilike, offset, limit).map { comments =>
            Ok(Json.toJson(comments.map(x => Comment(x.id, prefix + x.content))))
          }
        case _ =>
          Future(NotImplemented(Json.toJson(Map("result" -> Json.toJson(false), "reason" -> Json.toJson("malformed filter")))))
      }
    }
  }}
}
