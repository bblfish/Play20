package org.w3.play.rdf

import java.io._
import scalaz.Validation
import scalaz.Validation._
import play.api.libs.iteratee.Iteratee
import java.net.URL
import com.hp.hpl.jena.graph.Graph
import play.api.libs.concurrent.Akka
import com.hp.hpl.jena.rdf.model.{ModelFactory, Model}
import org.w3.rdf.jena.Jena
import com.hp.hpl.jena.rdf.arp.SAX2Model
import patch.AsyncJenaParser
import com.fasterxml.aalto.stax.InputFactoryImpl
import com.fasterxml.aalto.{AsyncXMLStreamReader, AsyncInputFeeder}
import org.w3.rdf._

trait RDFIteratee[Rdf <: RDF] {

  /**
   *
   * @param loc the location of the document to evaluate relative URLs (this will not make a connection)
   * @return an iteratee to process a streams of bytes that will parse to an RDF#Graph
   */
  def apply(loc: Option[URL]=None): Iteratee[Array[Byte],Rdf#Graph]

}

object JenaRdfXmlAsync extends RDFIteratee[Jena] {

  def apply(loc: Option[URL]): Iteratee[Array[Byte],Jena#Graph] =
    Iteratee.fold[Array[Byte], RdfXmlFeeder](new RdfXmlFeeder(loc)) {
      (graph, bytes) =>
        if (graph.feeder.needMoreInput()) {
          graph.feeder.feedInput(bytes, 0, bytes.length)
        } else throw new Throwable("ERROR: The feeder could not take any  more input for " + loc)

        //should one check if asyncParser needs more input?
        graph.asyncParser.parse()
        graph
    }.map(_.model.getGraph)


  protected case class RdfXmlFeeder(base: Option[URL]) {
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

  def apply(loc: Option[URL]=None): Iteratee[Array[Byte],Jena#Graph] = {
    {
      val in = new PipedInputStream()
      val out = new PipedOutputStream(in)
      val blockingIO = Akka.future {  // run blocking parser in its own thread
        try {
          modelFromInputStream(in, loc.orNull, serialization).fold(throw _,_.getGraph)
        } finally {
          in.close()
        }
      }
      Iteratee.fold[Array[Byte], PipedOutputStream](out) {
        (out, bytes) => { out.write(bytes); out }
      }.map(finished => {
        try { out.flush(); out.close() } catch { case e: IOException => log.warn("exception caught closing stream with " + loc, e) }
        blockingIO.await(5000).get       //todo: should be settable (but very likely much shorter than 5 seconds, since io succeeded)
      })
    }

  }

  protected def modelFromInputStream( is: InputStream,
                            base: URL,
                            lang: RDFSerialization): Either[Throwable, Model] =
    try {
      val m = ModelFactory.createDefaultModel()
      m.getReader(janaName(lang)).read(m, is, base.toString)
      Right(m)
    } catch {
      case t =>  {
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



