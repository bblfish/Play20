package org.w3.play

import org.w3.banana.jena.Jena
import java.net.URL
import play.api.libs.iteratee.Iteratee
import com.fasterxml.aalto.{AsyncInputFeeder, AsyncXMLStreamReader}
import com.fasterxml.aalto.stax.InputFactoryImpl
import com.hp.hpl.jena.rdf.model.{ModelFactory, Model}
import patch.AsyncJenaParser
import com.hp.hpl.jena.rdf.arp.SAX2Model
import org.w3.banana._
import java.io.{InputStream, IOException, PipedOutputStream, PipedInputStream}
import play.api.libs.concurrent.{Promise, Akka}
import scala.Right
import scala.Left
import scala.Right
import scala.Left
import scala.Right
import scala.Left

trait RDFIteratee[Rdf <: RDF] {

  /**
   *
   * @param loc the location of the document to evaluate relative URLs (this will not make a connection)
   * @return an iteratee to process a streams of bytes that will parse to an RDF#Graph
   */
  def apply(loc: Option[URL] = None): Iteratee[Array[Byte], Either[Exception,Rdf#Graph]]

}

object JenaRdfXmlAsync extends RDFIteratee[Jena] {

  def apply(loc: Option[URL]): Iteratee[Array[Byte],Either[Exception,Jena#Graph]] =
      Iteratee.fold2[Array[Byte], RdfXmlFeeder](new RdfXmlFeeder(loc)) {
        (feeder, bytes) =>
          try { //all this could be placed into a promise to be run by another actor if parsing takes too long
            if (feeder.feeder.needMoreInput()) {
              feeder.feeder.feedInput(bytes, 0, bytes.length)
              System.out.print("received:");System.out.write(bytes); System.out.println("-----")
            } else {
              throw new Exception("ERROR: The feeder could not take any  more input for " + loc)
            }
            //should one check if asyncParser needs more input?
            feeder.asyncParser.parse()
            Promise.pure(Pair(feeder,false))
          } catch {
            case e: Exception => {
              feeder.err = Some(e)
              Promise.pure(Pair(feeder, true))
            }
          }
      }.mapDone(_.result)



  protected case class RdfXmlFeeder(base: Option[URL]) {
    var err: Option[Exception] = None
    def result = err match {
      case None => Right(model.getGraph)
      case Some(e) => Left(e)
    }
    lazy val asyncReader: AsyncXMLStreamReader = new InputFactoryImpl().createAsyncXMLStreamReader()
    lazy val feeder: AsyncInputFeeder = asyncReader.getInputFeeder()
    lazy val model: Model = ModelFactory.createDefaultModel()
    lazy val asyncParser = new AsyncJenaParser(SAX2Model.create(base.map(_.toString).orNull, model),asyncReader)
  }

}


/**
 * RDF parsers based on the Jena library, all of which are blocking (synchronous)
 * @param serialization
 */
class JenaSyncRDFIteratee(val serialization: RDFSerialization) extends RDFIteratee[Jena] {
  import play.api.Play.current
  import webid.Logger.log
  new net.rootdev.javardfa.jena.RDFaReader  //import shellac's rdfa parser

  def apply(loc: Option[URL]=None): Iteratee[Array[Byte],Either[Exception,Jena#Graph]] = {
    {
      val in = new PipedInputStream()
      val out = new PipedOutputStream(in)
      val blockingIO = Akka.future {  // run blocking parser in its own thread
        try {
          modelFromInputStream(in, loc.orNull, serialization)
        } finally {
          in.close()
        }
      }
      Iteratee.fold[Array[Byte], PipedOutputStream](out) {
        (out, bytes) => { out.write(bytes); out }
      }.mapDone { finished =>
        try { out.flush(); out.close() } catch {
          case e: IOException => log.warn("exception caught closing stream with " + loc, e)
        }
        blockingIO.await(5000).get       //todo: should be settable (but very likely much shorter than 5 seconds, since io succeeded)
       }
    }

  }

  protected def modelFromInputStream( is: InputStream,
                                      base: URL,
                                      lang: RDFSerialization): Either[Exception, Jena#Graph] =
    try {
      val m = ModelFactory.createDefaultModel()
      m.getReader(janaName(lang)).read(m, is, base.toString)
      Right(m.getGraph)
    } catch {
      case t: Exception =>  {
        log.info("cought exception turning stream into model ",t)
        Left(t)
      }
    }

  def janaName(lang: RDFSerialization) = lang match {
    case RDFXML => "RDF/XML"
    case RDFXMLAbbrev => "RDF/XML-ABBREV"
    case Turtle => "TTL"
    case N3 => "N3"
    case RDFaHTML => "HTML"
    case RDFaXHTML => "XHTML"
  }

}
