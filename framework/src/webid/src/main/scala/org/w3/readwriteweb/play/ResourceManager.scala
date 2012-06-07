package org.w3.readwriteweb.play

import akka.actor.{InvalidActorNameException, Props, ActorRef, Actor}
import org.w3.banana._
import java.io.{FileOutputStream, File}
import jena.{JenaTurtleWriter, JenaReaderFactory, JenaOperations}
import org.w3.readwriteweb.play.Request
import akka.actor.InvalidActorNameException
import java.net.URL
import scalaz.Scalaz._
import scalaz.Validation
import akka.event.Logging


trait ReadWriteWebException extends Exception

case class CannotCreateResource(msg: String) extends ReadWriteWebException
/**
 *
 */

class ResourceManager(baseDirectory: File, baseUrl: URL) extends Actor {

  def getOrCreateResourceActor(path: String): ActorRef = {
    try {
      context.children.find(path == _.path.name) getOrElse {
        val fileOnDisk = new File(baseDirectory, path)
        context.actorOf(Props(new JenaResourceActor(fileOnDisk,path,new URL(baseUrl,path))), name = path)
      }
    } catch {
      case iane: InvalidActorNameException => context.actorFor(self.path / path)
    }
  }

  protected def receive: Actor.Receive = {
    case req @ Request(_,path)=>  {
      val resourceActorRef = getOrCreateResourceActor(path)
      resourceActorRef.forward(req)
    }
    case req @ PutGraph(path,_)=>  {
      val resourceActorRef = getOrCreateResourceActor(path)
      resourceActorRef.forward(req)
    }

  }
}

case class Request(method: String, path: String )
case class PutGraph[Rdf<:RDF](path: String, graph: Rdf#Graph)

abstract class ResourceActor[Rdf<:RDF](ops: RDFOperations[Rdf],
                              file: File,
                              path: String,
                              url: URL) extends Actor {

  val log = Logging(context.system, this)

  val ser = Turtle
  var graph: Validation[BananaException, Rdf#Graph] = _
  def reader[T<:RDFSerialization](ser: T): RDFReader[Rdf,_]
  def writer(ser: RDFSerialization): BlockingWriter[Rdf]
  lazy val parent = file.getParentFile

  log.info("Resource actor for "+file.getAbsolutePath+" and url="+url)


  protected def receive: Actor.Receive = {
    case req @ Request("GET",path) => {
      System.out.println("reqceive message "+req)
      graph = reader(ser).read(file,url.toString)
      sender ! graph
    }
    case pg @ PutGraph(path,model: Rdf#Graph) => {
      sender ! WrappedThrowable.fromTryCatch{
        if (parent.isDirectory) true
        else parent.mkdirs()
      }.flatMap {
        case true => {
          log.info("created dir "+parent + " now saving file")
          writer(ser).write(model, file, url.toString)
        }
        case false => {
          log.warning("could not create dir "+parent)
          WrongExpectation("path="+path+" file="+file.getAbsolutePath).fail
        }
      }
    }
    case unknown => {
      log.warning("received unknown message=>"+unknown)
      sender ! WrongExpectation("could not parse message "+unknown).fail
    }
  }
}

class JenaResourceActor(file: File, path: String, url: URL) extends ResourceActor(JenaOperations,file,path,url) {

  def reader[T <: RDFSerialization](ser: T) = JenaReaderFactory.find(ser).getOrElse(sys.error("Cannot read serialisations with Sesame"))

  def writer(ser: RDFSerialization) = JenaTurtleWriter
}