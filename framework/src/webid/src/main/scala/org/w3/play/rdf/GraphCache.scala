package org.w3.play.rdf

import org.w3.banana.{RDFXML, RDFSerialization, RDFOperations, RDF}
import org.w3.banana.jena.{JenaOperations, Jena}
import java.net.URL
import play.api.libs.iteratee.Iteratee
import play.api.libs.concurrent.Promise
import play.api.libs.ws.WS
import org.w3.readwriteweb.play.Lang
import org.w3.play._

/**
 * Fetch resources on the Web and cache them
 * ( at a later point this would include saving them to an indexed quad store )
 *
 * @param ops the type of the underlying RDF library
 * @params serializers a map from RDF serialisations to the serialisers, synchronous or asynchronous, to transform
 *        input into a graph
 *
 * @author Henry Story
 * @created: 12/10/2011
 *
 */
class GraphCache[Rdf <: RDF](val ops: RDFOperations[Rdf], val serializers: SerializerMap[Rdf])  {
//  import dispatch._
  import webid.Logger.log
  import play.api.Play.current

//use shellac's rdfa parser
//  new net.rootdev.javardfa.jena.RDFaReader  //import rdfa parser


  //this is a simple but quite stupid web cache so that graphs can stay in memory and be used a little
  // bit across sessions
//  val cache: LoadingCache[URL,Validation[Throwable,Model]] =
//    CacheBuilder.newBuilder()
//      .expireAfterAccess(5, TimeUnit.MINUTES)
//      .softValues()
//      //         .expireAfterWrite(30, TimeUnit.MINUTES)
//      .build(new CacheLoader[URL, Validation[Throwable,Model]] {
//      def load(url: URL) = fetch(url)
//    })
//
//  val http = new Http with thread.Safety {
//    import org.apache.http.params.CoreConnectionPNames
//    client.getParams.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 3000)
//    client.getParams.setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000)
//  }
//
//  def basePath = null //should be cache dir?
//
//  def sanityCheck() = true  //cache dire exists? But is this needed for functioning?
//
//  def resource(u : URL) = new org.w3.readwriteweb.Resource {
//    import CacheControl._
//    def name() = u
//    def get(cacheControl: CacheControl.Value) = cacheControl match {
//      case CacheOnly => {
//        val res = cache.getIfPresent(u)
//        if (null==res) NoCachEntry.fail
//        else res
//      }
//      case CacheFirst => cache.get(u)
//      case NoCache => {
//        val res = fetch(u)
//        cache.put(u,res) //todo: should this only be done if say the returned value is not an error?
//        res
//      }
//    }
//    // when fetching information from the web creating directories does not make sense
//    //perhaps the resource manager should be split into read/write sections?
//    def save(model: Model) =  throw new MethodNotSupportedException("not implemented")
//
//    def createDirectory(model: Model) =  throw new MethodNotSupportedException("not implemented")
//  }


  // note we prefer rdf/xml and turtle over html, as html does not always contain rdfa, and we prefer those over n3,
  // as we don't have a full n3 parser. Better would be to have a list of available parsers for whatever rdf framework is
  // installed (some claim to do n3 when they only really do turtle)
  // we can't currently accept */* as we don't have GRDDL implemented

  //we need to tell the model about the content type
  def fetch(u: URL): Promise[Either[Exception,Rdf#Graph]] ={

  val prom= WS.url(u.toString).
      withHeaders("Accept" -> "application/rdf+xml,text/turtle,application/xhtml+xml;q=0.8,text/html;q=0.7,text/n3;q=0.2").
      get { response =>
        val lang = response.headers.get("Content-Type").map(_.head) match {
          case Some(mime) => {
            Lang(mime.split(";")(0)) getOrElse Lang.default
          }
          case None => RDFXML //todo: it would be better to try to do a bit of guessing in this case by looking at content
        }

        val loc = response.headers.get("Content-Location").map(_.head) match {
          case Some(loc) => new URL(u, loc)
          case None => new URL(u.getProtocol, u.getAuthority, u.getPort, u.getPath)
        }
        serializers.iteratee4(lang)(Some(loc))
    }
    prom.flatMap(_.run)
  }

    //      request.>+>[Validation[Throwable, Model]](res =>  {
//      res >:> { headers =>
//        val encoding = headers("Content-Type").headOption match {
//          case Some(mime) => {
//            Lang(mime.split(";")(0)) getOrElse Lang.default
//          }
//          case None => RDFXML  //todo: it would be better to try to do a bit of guessing in this case by looking at content
//        }
//        val loc = headers("Content-Location").headOption match {
//          case Some(loc) =>  new URL(u,loc)
//          case None => new URL(u.getProtocol,u.getAuthority,u.getPort,u.getPath)
//        }
//        res>>{ in=> modelFromInputStream(in,loc,encoding) }
//
//      }
//    })
//    try {
//      val future = http(handler)
//      future
//    } catch {
//      case e: ConnectException => {
//        logger.info("failed to connect to "+u.getHost,e)
//        e.fail
//      }
//    }
//
//  }


//  override def finalize() { http.shutdown() }
}

/**
 * For Jena based projects this is a good graph cacher.
 */
object JenaGraphCache extends GraphCache[Jena](JenaOperations, JenaSerializerMap)

/**
 * A set of serialisers that are currently efficient for Jena.
 * Contains an Asynchronous RDFXML serialiser and all the others are synchronous.
 */
object JenaSerializerMap extends SerializerMap[Jena] {
  def iteratee4(lang: RDFSerialization)(loc: Option[URL]) = lang match {
    case RDFXML => JenaRdfXmlAsync(loc)
    //case TURTLE => use my async turtle parser in some way similar as above
    case otherMime => (new JenaSyncRDFIteratee(otherMime))(loc)
  }
}

/**
 * Implementations map RDFSerialisation formats to Iteratees that can parse those formats
 * The serialisers can be synchronous or asynchronous, thought they MUST take care of execting their
 * logic in seperate thread pools if they are synchronous. It may be better to send the tasks to
 * actors for asynchronous parsers too.
 *
 * @tparam Rdf  the type of the Graph returned
 */
trait SerializerMap[Rdf <: RDF] {

  /**
   *
   * @param lang the serialisation type required
   * @param location the location of the document, for relative url resolution
   * @return  an Iteratee that will be able to parse the document
   */
  def iteratee4(lang: RDFSerialization)(location: Option[URL]): Iteratee[Array[Byte], Either[Exception,Rdf#Graph]]
}