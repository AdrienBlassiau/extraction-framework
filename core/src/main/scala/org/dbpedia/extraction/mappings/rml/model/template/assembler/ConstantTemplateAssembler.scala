package org.dbpedia.extraction.mappings.rml.model.template.assembler

import java.net.URI

import org.dbpedia.extraction.mappings.rml.model.AbstractRMLModel
import org.dbpedia.extraction.mappings.rml.model.template.assembler.TemplateAssembler.Counter
import org.dbpedia.extraction.mappings.rml.model.resource.{RMLLiteral, RMLPredicateObjectMap, RMLTriplesMap, RMLUri}
import org.dbpedia.extraction.mappings.rml.model.template.ConstantTemplate
import org.dbpedia.extraction.mappings.rml.translate.dbf.DbfFunction
import org.dbpedia.extraction.ontology.{OntologyObjectProperty, RdfNamespace}
import org.dbpedia.extraction.util.Language

/**
  * Created by wmaroy on 25.07.17.
  */
class ConstantTemplateAssembler(rmlModel : AbstractRMLModel, baseUri : String, language : String, template: ConstantTemplate, counter : Counter, independent : Boolean) {

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Public methods
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  def assemble() : List[RMLPredicateObjectMap] = {
    addConstantMappingToTriplesMap(baseUri, rmlModel.triplesMap)
  }

  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //  Private methods
  //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  private def addConstantMappingToTriplesMap(uri: String, triplesMap: RMLTriplesMap) : List[RMLPredicateObjectMap]  =
  {
    val constantMappingUri = RMLUri(uri + "/ConstantMapping/" + counter.constants)

    val constantPom = if(!independent) {
      triplesMap.addPredicateObjectMap(constantMappingUri)
    } else {
      rmlModel.rmlFactory.createRMLPredicateObjectMap(constantMappingUri)
    }
    addConstantValuePredicateObjectMap(constantPom)
    List(constantPom)
  }

  private def addConstantValuePredicateObjectMap(constantPom: RMLPredicateObjectMap) =
  {
    constantPom.addPredicate(RMLUri(template.ontologyProperty.uri))

    if(template.unit == null) {

      if(template.ontologyProperty.isInstanceOf[OntologyObjectProperty]) {

        // if it is a URI return it directly
        val uri = new URI(template.value)

        val valueURI =

          // if the URI is absolute, we can use it directly. otherwise we make a DBpedia resource URI
          if (!uri.isAbsolute) Language(language).resourceUri.append(template.value)
          else uri.toString

        constantPom.addObject(RMLUri(valueURI))
      } else {
        constantPom.addObject(new RMLLiteral(template.value))
      }
    } else {

      addUnitToPredicateObjectMap(constantPom)
    }

  }


  private def addUnitToPredicateObjectMap(constantPom: RMLPredicateObjectMap) =
  {
    val functionTermMapUri = constantPom.uri.extend("/FunctionTermMap")
    val functionTermMap = constantPom.addFunctionTermMap(functionTermMapUri)
    val functionValueUri = functionTermMapUri.extend("/FunctionValue")
    val functionValue = functionTermMap.addFunctionValue(functionValueUri)
    functionValue.addLogicalSource(rmlModel.logicalSource)
    functionValue.addSubjectMap(rmlModel.functionSubjectMap)

    val executePomUri = functionValueUri.extend("/ExecutePOM")
    val executePom = functionValue.addPredicateObjectMap(executePomUri)
    executePom.addPredicate(RMLUri(RdfNamespace.FNO.namespace + "executes"))
    val ExecuteObjectMapUri = executePomUri.extend("/ObjectMap")
    executePom.addObjectMap(ExecuteObjectMapUri).addConstant(RMLUri(RdfNamespace.DBF.namespace + DbfFunction.unitFunction.name))

    addParameterFunction(DbfFunction.unitFunction.unitParameter, functionValue)
    addParameterFunction(DbfFunction.unitFunction.valueParameter, functionValue)

  }

  private def addParameterFunction(param : String, functionValue: RMLTriplesMap) =
  {
    val parameterPomUri = functionValue.uri.extend("/" + param + "ParameterPOM")
    val parameterPom = functionValue.addPredicateObjectMap(parameterPomUri)
    parameterPom.addPredicate(RMLUri(RdfNamespace.DBF.namespace + param + "Parameter"))
    val parameterObjectMapUri = parameterPomUri.extend("/ObjectMap")
    parameterPom.addObjectMap(parameterObjectMapUri).addRMLReference(new RMLLiteral(getParameterValue(param)))
  }

  private def getParameterValue(param: String) : String =
  {
    param match {
      case "unitParameter" => template.unit.name
      case "valueParameter" => template.value
    }
  }


}
