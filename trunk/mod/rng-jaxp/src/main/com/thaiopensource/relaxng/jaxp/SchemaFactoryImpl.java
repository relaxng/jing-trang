package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.pattern.SchemaBuilderImpl;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.xml.sax.BasicResolver;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;

public abstract class SchemaFactoryImpl extends SchemaFactory {
  private ErrorHandler errorHandler = null;
  private LSResourceResolver resourceResolver = null;
  private DatatypeLibraryFactory dlf = null;

  protected SchemaFactoryImpl() {
  }

  public void setErrorHandler(ErrorHandler errorHandler) {
    this.errorHandler = errorHandler;
  }

  public ErrorHandler getErrorHandler() {
    return errorHandler;
  }

  public void setResourceResolver(LSResourceResolver resourceResolver) {
    this.resourceResolver = resourceResolver;
  }

  public LSResourceResolver getResourceResolver() {
    return resourceResolver;
  }

  public Schema newSchema() throws SAXException {
    throw new UnsupportedOperationException();
  }

  public Schema newSchema(Source[] schemas) throws SAXException {
    if (schemas.length != 1)
      throw new UnsupportedOperationException();
    SAXSource source = sourceToSAXSource(schemas[0]);
    BasicResolver resolver;
    if (resourceResolver == null)
      resolver = new BasicResolver();
    else
      resolver = new Resolver(resourceResolver);
    ErrorHandler eh = errorHandler;
    if (eh == null)
      eh = new DraconianErrorHandler();
    // XXX add a property to enable user to set this
    if (dlf == null)
      dlf = new DatatypeLibraryLoader();
    Parseable parseable = createParseable(source, resolver, eh);
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    try {
      return new SchemaImpl(spb, SchemaBuilderImpl.parse(parseable, eh, dlf, spb, false));
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
    catch (IllegalSchemaException e) {
      // XXX not sure what we're supposed to do here
      throw new SAXException("invalid schema");
    }
  }

  abstract protected Parseable createParseable(SAXSource source, BasicResolver resolver, ErrorHandler eh) throws SAXException;

  static private SAXSource sourceToSAXSource(Source source) throws SAXException {
    if (source instanceof SAXSource)
      return (SAXSource)source;
    InputSource inputSource = SAXSource.sourceToInputSource(source);
    // XXX transform it to a SAXSource
    if (inputSource == null)
      throw new IllegalArgumentException("unsupported type of Source for schema");
    return new SAXSource(inputSource);
  }
}
