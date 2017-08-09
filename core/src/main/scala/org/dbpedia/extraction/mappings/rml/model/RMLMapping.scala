package org.dbpedia.extraction.mappings.rml.model

/**
  * RMLMappings converted from DBpedia mappings
  */
abstract class RMLMapping(modelWrapper: AbstractRMLModel) {

  def printAsNTriples(): Unit

  def printAsTurtle(): Unit

  def writeAsTurtle: String

  def writeAsNTriples: String
}
