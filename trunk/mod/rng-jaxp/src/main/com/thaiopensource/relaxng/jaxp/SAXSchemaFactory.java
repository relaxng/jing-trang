package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

public class SAXSchemaFactory extends SchemaFactoryImpl {
  static final public String SCHEMA_LANGUAGE = WellKnownNamespaces.RELAX_NG;
  
  protected Parseable createParseable(Source source, SAXResolver resolver, ErrorHandler eh) throws SAXException {
    return new SAXParseable(sourceToSAXSource(source, resolver), resolver, eh);
  }

  static private SAXSource sourceToSAXSource(Source source, SAXResolver resolver) throws SAXException {
    if (source instanceof SAXSource) {
      SAXSource saxSource = (SAXSource)source;
      if (saxSource.getXMLReader() != null)
        return saxSource;
    }
    InputSource inputSource = SAXSource.sourceToInputSource(source);
    // XXX transform it to a SAXSource
    if (inputSource == null)
      throw new IllegalArgumentException("unsupported type of Source for RELAX NG schema in XML syntax");
    return new SAXSource(resolver.createXMLReader(), inputSource);
  }

  public boolean isSchemaLanguageSupported(String schemaLanguage) {
    return schemaLanguage.equals(SCHEMA_LANGUAGE);
  }

}
