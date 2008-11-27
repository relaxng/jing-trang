package com.thaiopensource.relaxng.parse;

public interface SubParseable extends Parseable {
  ParsedPattern parseAsInclude(SchemaBuilder f, IncludedGrammar g)
          throws BuildException, IllegalSchemaException;
  /* The returned URI will have disallowed characters escaped. May return null for top-level schema. */
  String getUri();
}
