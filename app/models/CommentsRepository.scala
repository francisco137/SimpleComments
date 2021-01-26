package models

import javax.inject.{Inject, Singleton}
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CommentsRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {

  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  private class CommentsTable(tag: Tag) extends Table[Comment](tag, "comments") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def content = column[String]("content")
    def * = (id, content) <> ((Comment.apply _).tupled, Comment.unapply)
  }

  private val comments = TableQuery[CommentsTable]

  implicit val getCommentResult: AnyRef with GetResult[Comment] = slick.jdbc.GetResult(r => Comment(r.<<,r.<<))

  def select(id: Long): Future[Vector[Comment]] = {
    val action = sql"""SELECT id, content FROM comments WHERE id = #$id""".as[Comment]
    db.run {action}
  }

  def insert(content: String): Future[Comment] = db.run {
    (comments.map(c => c.content)
      returning comments.map(_.id)
      into ((content, id) => Comment(id, content))
    ) += content
  }

  def update(id: Long, content: String): Future[Boolean] = db.run {
    comments.filter(_.id === id).update(Comment(id,content))
  } map(_ > 0)

  def delete(id: Long): Future[Boolean] = db.run {
    comments.filter(_.id === id).delete
  } map (_ > 0)

  /**
   * The query method defines pure SQL query to get data from the comments table using filter.
   * Specially in the case of relational databases where there are three factor which is worth to use:
   * 1. There are optimizers to perform the frase WHERE (with ilike in this case)
   * 2. There are quick sorting mechanisms (indexes)
   * 3. There are huge limitations in network data transport*/
//  implicit val getCommentResult = slick.jdbc.GetResult(r => Comment(r.<<,r.<<))
  def query(sort: String, ilike: String, offset: Int, limit: Int): Future[Vector[Comment]] = {

    val page = if ( limit > -1 ) s" OFFSET = $offset LIMIT = $limit " else ""
    val ( content, orderBy ) = sort match {
      case "L" => ("content_list", " ORDER BY content_list::bytea ")
      case "S" => ("content"     , " ORDER BY content::bytea "     )
      case "D" => ("content_dict", " ORDER BY content_dict::bytea ")
      case _   => ("content"     , ""                       )
    }
//    val like = Option(LikeParser.parseAll(LikeParser.expr(""),filter)) match {
//      case Some(l) => l.get
//    }
    val where = if (ilike != "") " WHERE " + ilike else ""
    val action  = sql"""SELECT id, #$content as content FROM comments #$where #$orderBy #$page""".as[Comment]
    db.run { action }
  }
/*
  lazy val arraySort: (List[String], List[String]) => Boolean = (x: List[String], y: List[String]) => {
    ( x, y ) match {
      case (      Nil,      Nil) => true
      case (      Nil, a :: Nil) => true
      case ( a :: Nil,      Nil) => false
      case ( h1:: t1 , h2:: t2 ) if h1 < h2 => true
      case ( h1:: t1 , h2:: t2 ) if h1 > h2 => false
      case ( h1:: t1 , h2:: t2 ) => arraySort(t1,t2)
    }
  }

  /**
   * This queryORM method request full comments table and then filter to the expected set
   * Such approach is incorrect in the case of long tables where the filtering process should be
   * performed in a database and not send through the network
   */
  def queryORM(sort: String, filter: String, offset: Int, limit: Int) = {
    val sortAsList: (Comment, Comment) => Boolean = (x: Comment, y: Comment) => {
      val la = x.content.toLowerCase.replaceAll("[\\W]"," ").split(" ").filter(_.nonEmpty).toList
      val lb = y.content.toLowerCase.replaceAll("[\\W]"," ").split(" ").filter(_.nonEmpty).toList
      arraySort(la,lb)
    }

    val sortAsString: (Comment, Comment) => Boolean = (x: Comment, y: Comment) => {
      x.content.toLowerCase < y.content.toLowerCase
    }

    val sortAsDict: (Comment, Comment) => Boolean = (x: Comment, y: Comment) => {
      val la = x.content.toLowerCase.replaceAll("[\\W]"," ").split(" ").filter(_.nonEmpty).toList.sorted
      val lb = y.content.toLowerCase.replaceAll("[\\W]"," ").split(" ").filter(_.nonEmpty).toList.sorted
      arraySort(la,lb)
    }

    val commentList = db.run { comments.result } map (
      items => items.map(item =>
        Comment(item.id,item.content)).filter( c =>
          if ( filter == "" ) true
          else
            CommentParser.parseAll(CommentParser.expr(c.content.toLowerCase()),filter).get )
    )

    def transformContent(content: String) = {
      content.toLowerCase.replaceAll("[\\W]"," ").split(" ").filter(_.nonEmpty).toSet.toList.sorted
        .reduce(_ + " " + _)
    }

    val commentDict = ( db.run { comments.result } map (
      items => items.map(item => Comment(item.id,item.content))
        .filter(_.content.toLowerCase.contains(filter.toLowerCase))
      )).map(_.sortWith(sortAsDict).map(x => Comment(x.id,transformContent(x.content))))

    if      ( sort == "L" && limit <  0 ) commentList.map(_.sortWith(sortAsList  ))
    else if ( sort == "S" && limit <  0 ) commentList.map(_.sortWith(sortAsString))
    else if ( sort == "D" && limit <  0 ) commentDict.map(_.sortWith(sortAsDict  ))
    else if ( sort == "L" && limit >= 0 ) commentList.map(_.sortWith(sortAsList  ).slice(offset,offset + limit))
    else if ( sort == "S" && limit >= 0 ) commentList.map(_.sortWith(sortAsString).slice(offset,offset + limit))
    else if ( sort == "D" && limit >= 0 ) commentDict.map(_.sortWith(sortAsDict  ).slice(offset,offset + limit))
    else if (                limit >= 0 ) commentList.map(_.slice(offset,offset + limit))
    else                                  commentList
  }
*/
}
