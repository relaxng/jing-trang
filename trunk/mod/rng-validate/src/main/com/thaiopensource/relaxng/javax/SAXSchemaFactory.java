package com.thaiopensource.relaxng.javax;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.parse.sax.UriResolverImpl;
import com.thaiopensource.xml.sax.BasicResolver;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;

public class SAXSchemaFactory extends SchemaFactoryImpl {
  static final public String SCHEMA_LANGUAGE = WellKnownNamespaces.RELAX_NG;
  
  protected Parseable createParseable(SAXSource source, BasicResolver resolver, ErrorHandler eh) throws SAXException {
    if (source.getXMLReader() == null)
      source = new SAXSource(resolver.createXMLReader(), source.getInputSource());
    return new SAXParseable(source, new UriResolverImpl(resolver), eh);
  }
  
  public boolean isSchemaLanguageSupported(String schemaLanguage) {
    return schemaLanguage.equals(SCHEMA_LANGUAGE);
  }

}
