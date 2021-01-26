package utils

import scala.util.matching.Regex
import scala.util.parsing.combinator.RegexParsers

object CommentParser extends RegexParsers {

  private val word: Regex = "[ \\-\\w]+".r
  def expr(content: String): Parser[Boolean] =
    term(content) ~ opt(( "," | "*" ) ~ expr(content)) ^^ {
      case f ~ None => f
      case f ~ Some("*" ~ b) => f && b
      case f ~ Some("," ~ b) => f || b
      case _ => false
    }
  def term(content: String): Parser[Boolean] = word ^^ { w => {
    if (w.head == '-') !content.contains(w.tail) else content.contains(w)
  }} | "(" ~> expr(content) <~ ")"
}


object LikeParser extends RegexParsers {
  private val word = "[ \\-\\w]+".r
  def expr(content: String): Parser[String] = {
    term(content) ~ opt(( "*" | "," ) ~ expr(content)) ^^ {
      case f ~ None => f
      case f ~ Some("*" ~ b) => f + " AND " + b
      case f ~ Some("," ~ b) => "(" + f + " OR  " + b + ")"
      case _ => ""
    }
  }
  def term(content: String): Parser[String] =
    word ^^ { w => {
      if ( w.head == '-' ) "NOT(content ILIKE '%" + w.tail + "%')"
      else {
        "content ILIKE '%" + w + "%'"
      }
    }} | "(" ~> expr(content) <~ ")"

  def count(filter: String): Option[String] = {
    if ( filter == "" ) {
      Option("")
    } else {
      try {
        Option(this.parseAll(this.expr(""),filter).get)
      } catch {
        case _: Throwable => None
      }
    }
  }
}
