package org.w3.readwriteweb.play

import play.api.mvc._
import org.w3.banana._
import org.w3.play.rdf.{JenaRDFParserMap, ParserMap}
import java.net.URL
import scala.Some
import org.w3.banana.jena._
import play.api.http.{ContentTypeOf, Writeable}
import java.io.{File, ByteArrayOutputStream}
import play.api.libs.concurrent.Akka
import scala.Some
import akka.actor.{Props, ActorSystem}
import scala.Some
import akka.util.Timeout
import scalaz.Validation
import play.api.libs.iteratee.Done
import play.api.libs.iteratee.Input.Empty


/**
 * An action to enable
 */
//class RWW_Action[Rdf <: RDF] extends Action[Rdf#Graph] {
//
//  def apply(request: Request[Rdf#Graph]) =
//
//}

object ReadWriteWeb_App extends Controller {
  import akka.pattern.ask
  import play.api.libs.concurrent._

  val system = ActorSystem("MySystem")
  implicit val timeout = Timeout(10 * 1000)

//  if this class were shipped as a plugin, then the code below might work.
//  But it is a bit odd given that you have to specify whether something is secure or not.
//
//  lazy val url = call.absoluteURL(true)
//  def call : Call = org.w3.readwriteweb.play.routes.ReadWriteWeb_App.get

  val url = new URL("https://localhost:8443/2012/")
  lazy val rwwActor = system.actorOf(Props(new ResourceManager(new File("test_www"), url)), name = "rwwActor")

  def writerFor(req: RequestHeader) = req.accept.collectFirst {
    case "application/rdf+xml" =>  (writeable(JenaRdfXmlWriter),ContentTypeOf[Jena#Graph](Some("application/rdf+xml")))
    case "text/turtle" => (writeable(JenaTurtleWriter), ContentTypeOf[Jena#Graph](Some("text/turtle")))
  }.get

  def writeable(writer: BlockingWriter[Jena]) =
    Writeable[Jena#Graph] {
      graph =>
        val res = new ByteArrayOutputStream()
        val tw = writer.write(graph, res, "http://localhost:8888/")
        res.toByteArray
    }


  def get(path: String) = Action { request =>
    System.out.println("in GET with path="+request.path)
    val future = for (answer <- rwwActor ask Request("GET", path ) mapTo manifest[Validation[BananaException, Jena#Graph]])
    yield {
         answer.fold(
          e => ExpectationFailed(e.getMessage),
          g => { val (wr,ct) = writerFor(request); Ok(g)(wr,ct) }
         )
    }
    Async {
     future.asPromise
    }

  }

  def put(path: String) = Action(jenaRwwBodyParser) {
    request =>
      System.out.println("in put with path="+path)
      val msg = request.body match {
        case GraphRwwContent(graph) => Put[Jena](path,request.body)
        case _ => throw new Exception("error")
      }
      val future = for (answer <- rwwActor ask msg mapTo manifest[Validation[BananaException, Unit]])
      yield {
        answer.fold(
          e => ExpectationFailed(e.getMessage),
          _ =>  Ok("Succeeded")
        )
      }
      Async {
        future.asPromise
      }
  }

  def post = Action(jenaRwwBodyParser) {
    request =>
      import play.api.Play.current
      Async {
        Akka.future {
        //this is a good piece of code for a future, as serialising the graph is very fast
          System.out.println("triple num== " + request.body)
          request.body match {
            case GraphRwwContent(graph) => {
              val (wr,ct) = writerFor(request)
              Ok(graph.asInstanceOf[Jena#Graph])(wr,ct)
            }
            case _ => Ok("received content")
          }

        }
      }
  }


}

object jenaRwwBodyParser extends RwwBodyParser[Jena, JenaSPARQL](JenaOperations, JenaSPARQLOperations, JenaRDFParserMap )


class RwwBodyParser[Rdf <: RDF, Sparql <: SPARQL](val ops: RDFOperations[Rdf],
                                val sparqlOps: SPARQLOperations[Rdf, Sparql],
                                val graphParsers: ParserMap[RDFSerialization, Rdf#Graph]) extends BodyParser[RwwContent] {

  import play.api.mvc.Results._
  import play.api.mvc.BodyParsers.parse

  def apply(rh: RequestHeader) =
    rh.contentType match {
      case _ if rh.method == "GET" || rh.method == "HEAD" => Done(Right(emptyContent), Empty)
      case Some("application/sparql-query") => parse.text(rh).map {
        _.right.flatMap { sparql =>
            sparqlOps.Query(sparql).fold(
              fail => Left(BadRequest("could not parse the SPARQL sent: " + sparql)),
              res =>  Right(QueryRwwContent(res))
            )
        }
      }
      case Some(RdfLang(mime)) => graphParsers.iteratee4(mime)(Some(new URL("http://localhost:9000/" + rh.uri))).map {
        case Left(e) => Left(BadRequest("cought " + e))
        case Right(graph) => Right(GraphRwwContent(graph))
      }
      case Some(mime) => parse.raw(rh).map {
        _.right.map(rb => BinaryRwwContent(rb, mime))
      }
      case None => Done(Left(BadRequest("missing Content-type header. Please set the content type in the HTTP header " +
        " of your message ")), Empty)
    }


  override def toString = "BodyParser(" + ops.toString + ")"

}


object RdfLang {
  def unapply(mime: String) = mime match {
    case "application/rdf+xml" => Some(RDFXML)
    case "text/turtle" => Some(Turtle)
  }
}

trait RwwContent

case object emptyContent extends RwwContent

case class GraphRwwContent[Rdf<:RDF](graph: Rdf#Graph) extends RwwContent

case class QueryRwwContent[Sparql<:SPARQL](query: Sparql#Query) extends RwwContent

case class BinaryRwwContent(binary: RawBuffer, mime: String) extends RwwContent



