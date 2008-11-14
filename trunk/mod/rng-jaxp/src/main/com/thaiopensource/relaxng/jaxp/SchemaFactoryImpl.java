package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.pattern.SchemaBuilderImpl;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.xml.ls.LS;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
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
    Source source = schemas[0];
    Resolver resolver = null;
    if (resourceResolver != null)
      resolver = LS.createResolver(resourceResolver);
    SAXResolver saxResolver = new SAXResolver(resolver);
    ErrorHandler eh = errorHandler;
    if (eh == null)
      eh = new DraconianErrorHandler();
    // XXX add a property to enable user to set this
    if (dlf == null)
      dlf = new DatatypeLibraryLoader();
    Parseable parseable = createParseable(source, saxResolver, eh);
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

  abstract protected Parseable createParseable(Source source, SAXResolver resolver, ErrorHandler eh) throws SAXException;
}
