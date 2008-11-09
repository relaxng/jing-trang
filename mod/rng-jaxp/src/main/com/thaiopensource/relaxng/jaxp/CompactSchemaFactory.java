package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.parse.compact.UriOpenerImpl;
import com.thaiopensource.xml.sax.BasicResolver;
import org.xml.sax.ErrorHandler;

import javax.xml.transform.sax.SAXSource;

public class CompactSchemaFactory extends SchemaFactoryImpl {
  static final public String SCHEMA_LANGUAGE = "http://www.iana.org/assignments/media-types/application/relax-ng-compact-syntax";

  public boolean isSchemaLanguageSupported(String schemaLanguage) {
    return schemaLanguage.equals(SCHEMA_LANGUAGE);
  }

  protected Parseable createParseable(SAXSource source, BasicResolver resolver, ErrorHandler eh) {
    return new CompactParseable(source.getInputSource(), new UriOpenerImpl(resolver), eh);
  }
}
