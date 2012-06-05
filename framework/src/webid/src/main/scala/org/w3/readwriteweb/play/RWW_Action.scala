package org.w3.readwriteweb.play

import play.api.mvc._
import org.w3.banana.{BlockingWriter, RDFXML, RDFOperations, RDF}
import org.w3.play.rdf.{JenaSerializerMap, SerializerMap}
import java.net.URL
import scala.Some
import org.w3.banana.jena._
import play.api.http.{ContentTypeOf, Writeable}
import java.io.ByteArrayOutputStream
import play.api.libs.concurrent.Akka
import scala.Some


/**
 * An action to enable
 */
//class RWW_Action[Rdf <: RDF] extends Action[Rdf#Graph] {
//
//  def apply(request: Request[Rdf#Graph]) =
//
//}

object ReadWriteWeb_App extends Controller {

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


  def get = Action {
    Ok("hello")
  }

  def put = Action(jenaRdfBodyParser) {
    request =>
      import play.api.Play.current
      val p = Akka.future {
        System.out.println("triple num= " + request.body.size())
        val (wr,ct) = writerFor(request)
        Ok(request.body)(wr, ct)
      }
      Async {
        p
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

