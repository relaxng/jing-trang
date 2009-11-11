package com.thaiopensource.validate.rng;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.rng.impl.SchemaReaderImpl;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.VoidValue;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;

import javax.xml.transform.sax.SAXSource;

public class SAXSchemaReader extends SchemaReaderImpl {
  private static final SchemaReader theInstance = new SAXSchemaReader();
  
  private SAXSchemaReader() {
  }
  
  public static SchemaReader getInstance() {
    return theInstance;
  }

  protected Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> createParseable(SAXSource source, SAXResolver resolver, ErrorHandler eh, PropertyMap properties) throws SAXException {
    if (source.getXMLReader() == null)
      source = new SAXSource(resolver.createXMLReader(), source.getInputSource());
    return new SAXParseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl>(source, resolver, eh);
  }
}
