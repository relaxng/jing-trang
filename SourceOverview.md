The source code is divided into a number of modules which live in the [mod](http://code.google.com/p/jing-trang/source/browse/#svn/trunk/mod) directory.

They can be divided into three groups.

  * **common** - modules used by both Jing and Trang
    * **util** - utility functions, some XML-related, some not; used by almost all other modules
    * **resolver** - integrates handling of the various kinds of URI/entity resolver found in the Java platform (e.g. EntityResolver, EntityResolver2, URIResolver, LSResourceResolver)
    * **catalog** - integrates support for OASIS XML Catalogs into the framework provided by the resolver module; uses the Apache XML Commons Resolver
    * **regex** - supports regular expressions using the syntax of W3C XML Schema Part 2; includes code to translate W3C XML Schema regular expressions into `java.util.regex` regular expressions
    * **regex-gen** - code that generates some of the code in the regex module
    * **datatype** - supports the idea of a datatype library
    * **xsd-datatype** - implements the datatypes of W3C XML Schema Part 2 as a datatype library; depends on the regex and datatype modules
    * **rng-parse** - parses both the XML and compact syntax of RELAX NG
  * **jing** - modules used only by Jing
    * **pattern** - matching and simplifying RELAX NG patterns; depends on datatype, rng-parse; this is the guts of Jing
    * **rng-jarv** - implements the JARV validation interface on top of pattern
    * **jaxp** - provides extensions and helpers for the JAXP javax.xml.validation API
    * **rng-jaxp** - implements the JAXP javax.xml.validation API on top of pattern; uses the jaxp module
    * **validate** - provides a schema language independent framework for validation; this predates the standard javax.xml.validation JAXP interface; eventually this will probably be superseded by the extended JAXP interface defined in the jaxp module
    * **rng-validate** - implements RELAX NG validation for the framework provided by the validate module using the pattern module; depends on datatype, validate, rng-parse, pattern
    * **nvdl** - implements NVDL validation for the framework provided by the validate module; depends on rng-validate (because it uses RELAX NG to validate NVDL schemas); also implements MNS and NRL, the non-standard predecessors to NVDL
    * **schematron** - implements Schematron 1.5 for the framework provided by the validate module; depends on rng-validate (because it uses RELAX NG to validate Schematron schemas)
    * **xerces** - uses Xerces2-J to implement support for W3C XML Schema based on the framework provided by the validate module
    * **picl** - some experimental code for a Path-based Integrity Constraint Language that was being developed for Part 7 of [DSDL](http://www.dsdl.org)
  * **trang** - modules used only by Trang
    * **rng-schema** - provides an object model for RELAX NG schemas, which is designed to capture everything about a schema that is significant for a schema author; supports input/output between the object model and RELAX NG XML and compact syntax; depends on rng-parse
    * **convert-to-xsd** - converts from the rng-schema object model to W3C XML Schema
    * **convert-to-dtd** - converts from the rng-schema object model to XML 1.0 DTDs
    * **dtd-parse** - parses XML 1.0 DTDs producing an object model that captures use of parameter entities; note that this doesn't use an external XML parser; depends only on the util module
    * **dtdinst** - an application that generates an XML representation of the dtd-parse object model
    * **convert-from-dtd** - uses the dtd-parse module to convert DTDs into the rng-schema object model
    * **infer** - infers an schema based on one or more XML instances; the schema has expressive level of XML DTDs together with XML Schema Datatypes; depends only on util and datatype
    * **convert-from-xml** - uses the infer module to generate an rng-schema object model from one or more XML instances
    * **trang** - contains the Trang application; this uses the various modules to convert the input format first into the rng-schema object model and then from the rng-schema object model to the output format
