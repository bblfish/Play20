package org.w3.play.rdf

import java.net.URL
import play.api.libs.iteratee.Iteratee

/**
 *
 * @tparam Result The object resulting of the Iteratee application
 * @tparam SyntaxType The Syntax type that this iteratee parses
 */
trait RDFIteratee[Result, +SyntaxType] {

  /**
   *
   * @param loc the location of the document to evaluate relative URLs (this will not make a connection)
   * @return an iteratee to process a streams of bytes that will parse to an RDF#Graph
   */
  def apply(loc: Option[URL] = None): Iteratee[Array[Byte], Either[Exception, Result]]

}


