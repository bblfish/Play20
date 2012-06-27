package org.w3.readwriteweb.play

import play.api.mvc._
import org.w3.banana._
import java.net.URL
import org.w3.banana.jena._
import java.io.{File, ByteArrayOutputStream}
import akka.actor.{Props, ActorSystem}
import scala.Some
import akka.util.Timeout
import scalaz.{Either3, Validation}
import play.api.libs.iteratee.{Enumerator, Done}
import play.api.libs.iteratee.Input.Empty
import org.w3.play.rdf.IterateeSelector
import org.w3.play.rdf.jena.{JenaAsync, JenaSparqlQueryIteratee, JenaBlockingSparqlIteratee}


/**
 * An action to enable
 */
//class RWW_Action[Rdf <: RDF] extends Action[Rdf#Graph] {
//
//  def apply(request: Request[Rdf#Graph]) =
//
//}

object Writer {

  //return writer from request header
  def writerFor[Obj](req: RequestHeader)
               (implicit writerSelector: RDFWriterSelector[Obj])
  :  Option[BlockingWriter[Obj, Any]] = {
    //these two lines do more work than needed, optimise to get the first
    val ranges = req.accept.map{ range => MediaRange(range) }
    val writer = ranges.flatMap(range => writerSelector(range)).headOption
    writer
  }

  def result[Obj](code: Int, writer: BlockingWriter[Obj,_])(obj: Obj) = {
    SimpleResult(
      header = ResponseHeader(200, Map("Content-Type" -> writer.syntax.mimeTypes.head.mime)),  //todo
      body = toEnum(writer)(obj)
    )
  }

  def toEnum[Obj](writer: BlockingWriter[Obj,_]) =
    (obj: Obj) => {
    val res = new ByteArrayOutputStream()
    val tw = writer.write(obj, res, "http://localhost:8888/")
    Enumerator(res.toByteArray)
  }

}


object ReadWriteWeb_App extends Controller {
  import akka.pattern.ask
  import play.api.libs.concurrent._
  import Writer._

  val system = ActorSystem("MySystem")
  implicit val timeout = Timeout(10 * 1000)

//  if this class were shipped as a plugin, then the code below might work.
//  But it is a bit odd given that you have to specify whether something is secure or not.
//
//  lazy val url = call.absoluteURL(true)
//  def call : Call = org.w3.readwriteweb.play.routes.ReadWriteWeb_App.get

  val url = new URL("https://localhost:8443/2012/")
  lazy val rwwActor = system.actorOf(Props(new ResourceManager(new File("test_www"), url)), name = "rwwActor")

  //import some implicits
  import JenaAsync.graphIterateeSelector
  import JenaRDFBlockingWriter.{WriterSelector=>RDFWriterSelector}
  import SparqlAnswerWriter.{WriterSelector=>SparqWriterSelector}


//    JenaRDFBlockingWriter.WriterSelector()
//    req.accept.collectFirst {
//      case "application/rdf+xml" =>  (writeable(JenaRdfXmlWriter),ContentTypeOf[Jena#Graph](Some("application/rdf+xml")))
//      case "text/turtle" => (writeable(JenaTurtleWriter), ContentTypeOf[Jena#Graph](Some("text/turtle")))
//      case m @ SparqlAnswerJson.mime => (writeable(JenaSparqlJSONWriter), ContentTypeOf[JenaSPARQL#Solutions](Some(m)))
//    }.get



  def get(path: String) = Action { request =>
    System.out.println("in GET with path="+request.path)
    val future = for (answer <- rwwActor ask Request("GET", path ) mapTo manifest[Validation[BananaException, Jena#Graph]])
    yield {
         answer.fold(
          e => ExpectationFailed(e.getMessage),
          g => {
            writerFor[Jena#Graph](request)(RDFWriterSelector).map {
              wr => result(200, wr)(g)
            } getOrElse {
              UnsupportedMediaType("could not find serialiserfor Accept types"+request.headers.get(play.api.http.HeaderNames.ACCEPT))
            }
          }
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
      //this is a good piece of code for a future, as serialising the graph is very fast
      System.out.println("triple num== " + request.body)
      request.body match {
        case GraphRwwContent(graph: Jena#Graph) => {
          Async {
            Akka.future {
             writerFor[Jena#Graph](request)(RDFWriterSelector).map { wr=>
                result(200,wr)(graph)
             }.getOrElse(UnsupportedMediaType("cannot parse content type"))
            }
          }
        }
        case q: QueryRwwContent[JenaSPARQL] => {
          val future = for (answer <- rwwActor ask Query[JenaSPARQL](q,request.path) mapTo manifest[Validation[BananaException,Either3[JenaSPARQL#Solutions, Jena#Graph, Boolean]]])
          yield {
            answer.fold(
              e => ExpectationFailed(e.getMessage),
              answer => answer.fold(
                sol => writerFor[JenaSPARQL#Solutions](request)(SparqWriterSelector).map {
                  wr => result(200, wr)(sol)
                },
                graph => writerFor[Jena#Graph](request)(RDFWriterSelector).map {
                  wr => result(200, wr)(graph)
                },
                bool => writerFor[Boolean](request).map {
                  wr => result(200, wr)(bool)
                }
              ).getOrElse(UnsupportedMediaType("cannot parse content type"))
            )
          }
          Async {
            future.asPromise
          }
        }
        case _ => Ok("received content")
      }
  }


}

object jenaRwwBodyParser extends
   RwwBodyParser[Jena, JenaSPARQL](JenaOperations, JenaSPARQLOperations,
     JenaAsync.graphIterateeSelector, JenaSparqlQueryIteratee.sparqlSelector )


class RwwBodyParser[Rdf <: RDF, Sparql <: SPARQL]
(val ops: RDFOperations[Rdf],
 val sparqlOps: SPARQLOperations[Rdf, Sparql],
 val graphSelector: IterateeSelector[Rdf#Graph],
 val sparqlSelector: IterateeSelector[Sparql#Query])
  extends BodyParser[RwwContent] {

  import play.api.mvc.Results._
  import play.api.mvc.BodyParsers.parse

  def apply(rh: RequestHeader) =  {
    if (rh.method == "GET" || rh.method == "HEAD") Done(Right(emptyContent), Empty)
    else rh.contentType.map { str =>
       MimeType(str) match {
        case sparqlSelector(iteratee) => iteratee().mapDone {
          case Left(e) => Left(BadRequest("could not parse query "+e))
          case Right(sparql) => Right(QueryRwwContent(sparql))
        }
        case graphSelector(iteratee) => iteratee(Some(new URL("http://localhost:9000/" + rh.uri))).mapDone {
          case Left(e) => Left(BadRequest("cought " + e))
          case Right(graph) => Right(GraphRwwContent(graph))
        }
        case mime: MimeType => parse.raw(rh).mapDone {
          _.right.map(rb => BinaryRwwContent(rb, mime.mime))
        }
      }
    }.getOrElse {
      Done(Left(BadRequest("missing Content-type header. Please set the content type in the HTTP header of your message ")),
        Empty)
    }
  }


  override def toString = "BodyParser(" + ops.toString + ")"

}


trait RwwContent

case object emptyContent extends RwwContent

case class GraphRwwContent[Rdf<:RDF](graph: Rdf#Graph) extends RwwContent

case class QueryRwwContent[Sparql<:SPARQL](query: Sparql#Query) extends RwwContent

case class BinaryRwwContent(binary: RawBuffer, mime: String) extends RwwContent



