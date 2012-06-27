package org.w3.play.rdf

import org.w3.banana.{SPARQLOperations, SPARQL, RDF}
import java.net.URL
import play.api.libs.iteratee.Iteratee

/**
 * Iteratee for reading in SPARQL Queries
 * @author Henry Story
 */
class SparqlQueryIteratee[Rdf<:RDF, Sparql<:SPARQL, +SyntaxType]
(implicit ops: SPARQLOperations[Rdf,Sparql])
  extends RDFIteratee[Sparql#Query, SyntaxType] {
  /**
   *
   * @param loc the location of the document to evaluate relative URLs (this will not make a connection)
   * @return an iteratee to process a streams of bytes that will parse to an RDF#Graph
   */
  def apply(loc: Option[URL]) = Iteratee.fold[Array[Byte],StringBuilder](new StringBuilder){
    (builder,bytes) => builder.append(bytes)
  }.mapDone{
    stringbuilder =>
      ops.Query(stringbuilder.toString()).either
  }
}



