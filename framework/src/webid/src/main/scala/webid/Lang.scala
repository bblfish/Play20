/*
 * Copyright (c) 2011 W3C under the W3C licence defined at http://opensource.org/licenses/W3C
 */

package org.w3.readwriteweb

import org.w3.rdf.{RDFSerialization, N3, Turtle, RDFXML}
import scala.None


object Lang {

  val supportedLanguages = Set(RDFXML, Turtle, N3)
//  val supportContentTypes = supportedLanguages map (_.contentType)
//  val supportedAsString = supportContentTypes mkString ", "

  val default = RDFXML

  def apply(contentType: String): Option[RDFSerialization] =
    contentType.trim.toLowerCase match {
      case "text/n3" => Some(N3)
      case "text/rdf+n3"=>Some(N3)
      case "text/turtle" => Some(Turtle)
      case "application/rdf+xml" => Some(RDFXML)
//      case "text/html" => Some(HTML)
//      case "application/xhtml+xml" => Some(XHTML)
      case _ => None
    }

  def apply(cts: Iterable[String]): Option[RDFSerialization] =
    cts map Lang.apply collectFirst { case Some(lang) => lang }


  def contentType(lang: RDFSerialization) = lang match {
    case RDFXML => "application/rdf+xml"
    case Turtle => "text/turtle"
    case N3 => "text/n3"
    //    case XHTML => "application/xhtml+xml"
    //    case HTML => "text/html"
  }

  def jenaLang(lang: RDFSerialization) = lang match {
    case RDFXML => "RDF/XML-ABBREV"
    case Turtle => "TURTLE"
    case N3 => "N3"
    //    case HTML => "HTML"
    //    case XHTML => "XHTML"
  }
}
