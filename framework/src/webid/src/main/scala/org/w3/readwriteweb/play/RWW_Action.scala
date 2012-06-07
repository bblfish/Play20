package org.w3.readwriteweb.play

import play.api.mvc._
import org.w3.banana._
import org.w3.play.rdf.{JenaSerializerMap, SerializerMap}
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

  def put(path: String) = Action(jenaRdfBodyParser) {
    request =>
      System.out.println("in put with path="+path)
      val future = for (answer <- rwwActor ask PutGraph[Jena](path,request.body) mapTo manifest[Validation[BananaException, Unit]])
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

  def post = Action(jenaRdfBodyParser) {
    request =>
      import play.api.Play.current
      Async {
        Akka.future {
        //this is a good piece of code for a future, as serialising the graph is very fast
          System.out.println("triple num== " + request.body.size())
          val (wr,ct) = writerFor(request)
          Ok(request.body)(wr,ct)
        }
      }
  }


}

object jenaRdfBodyParser extends RdfBodyParser(JenaOperations, JenaSerializerMap )

class RdfBodyParser[Rdf <: RDF](val ops: RDFOperations[Rdf],
                                val serializers: SerializerMap[Rdf]) extends BodyParser[Rdf#Graph] {

  import play.api.mvc.Results._

  def apply(rh: RequestHeader) = {
    val lang = rh.contentType match {
      case Some(mime) => {
        Lang(mime.split(";")(0)) getOrElse Lang.default
      }
      case None => RDFXML //todo: it would be better to try to do a bit of guessing in this case by looking at content
    }
    serializers.iteratee4(lang)(Some(new URL("http://localhost:9000/"+rh.uri))).map{
      _.left.map(e=>BadRequest("cought "+e))
    }
  }

  override def toString = "BodyParser(" + ops.toString + ")"

}

