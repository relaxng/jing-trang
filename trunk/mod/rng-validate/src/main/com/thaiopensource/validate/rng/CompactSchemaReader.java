package com.thaiopensource.validate.rng;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.rng.impl.SchemaReaderImpl;
import org.xml.sax.ErrorHandler;

import javax.xml.transform.sax.SAXSource;

public class CompactSchemaReader extends SchemaReaderImpl {
  private static final SchemaReader theInstance = new CompactSchemaReader();

  private CompactSchemaReader() {
  }

  public static SchemaReader getInstance() {
    return theInstance;
  }

  protected Parseable createParseable(SAXSource source, SAXResolver saxResolver, ErrorHandler eh, PropertyMap properties) {
    return new CompactParseable(SAX.createInput(source.getInputSource()), saxResolver.getResolver(), eh);
  }
}
