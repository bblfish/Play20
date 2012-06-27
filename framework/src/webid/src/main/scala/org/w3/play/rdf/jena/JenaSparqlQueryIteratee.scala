package org.w3.play.rdf.jena

import org.w3.play.rdf.{IterateeSelector, SparqlQueryIteratee}
import org.w3.banana.jena.{JenaSPARQL, Jena}
import org.w3.banana.{RDFXML, SparqlQuery}

object JenaSparqlQueryIteratee {

 implicit val apply = new SparqlQueryIteratee[Jena, JenaSPARQL, SparqlQuery]

 val sparqlSelector = IterateeSelector[JenaSPARQL#Query, SparqlQuery]

}