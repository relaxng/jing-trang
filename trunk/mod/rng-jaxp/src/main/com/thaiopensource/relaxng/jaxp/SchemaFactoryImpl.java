package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.datatype.xsd.DatatypeLibraryFactoryImpl;
import com.thaiopensource.datatype.xsd.regex.java.RegexEngineImpl;
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
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.IOException;

/**
 * A SchemaFactory that supports RELAX NG.
 * This class is abstract: it has two concrete subclasses, one that supports the XML syntax
 * and one that supports the compact syntax.
 * @see XMLSyntaxSchemaFactory
 * @see CompactSyntaxSchemaFactory
 */
public abstract class SchemaFactoryImpl extends SchemaFactory {
  private ErrorHandler errorHandler = null;
  private LSResourceResolver resourceResolver = null;
  private DatatypeLibraryFactory datatypeLibraryFactory = null;
  /* If this is true, then logically datatypeLibraryFactory is an instance of DatatypeLibraryLoader,
     but we create it lazily, so that we don't need to create it if the user specifies their own. */
  private boolean defaultDatatypeLibraryFactory = true;
  // Corresponds to XMLConstants.FEATURE_SECURE_PROCESSING. Doesn't do anything yet.
  private boolean secureProcessing = false;
  /**
   * The name of the property that can be used to specify a DatatypeLibraryFactory.
   * The value of the property must implement org.relaxng.datatype.DatatypeLibraryFactory.
   * By default, a datatype library factory that supports XML Schema Datatypes is used.
   * If the value of this property is set to null, then only the built-in datatypes will be
   * supported. By default, datatype libraries will not be discovered dynamically; in order
   * to enable this, the value can be set to an instance of
   * org.relaxng.datatype.helpers.DatatypeLibraryLoader.
   * @see DatatypeLibraryFactory
   * @see org.relaxng.datatype.helpers.DatatypeLibraryLoader
   * @see #setProperty
   * @see #getProperty
   */
  static final public String PROPERTY_DATATYPE_LIBRARY_FACTORY = "http://relaxng.org/properties/datatype-library-factory";

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
    Parseable parseable = createParseable(source, saxResolver, eh);
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    try {
      return new SchemaImpl(spb, SchemaBuilderImpl.parse(parseable, eh, getDatatypeLibraryFactory(), spb, false));
    }
    catch (IOException e) {
      throw new SAXException(e);
    }
    catch (IllegalSchemaException e) {
      // XXX not sure what we're supposed to do here
      throw new SAXException("invalid schema");
    }
  }

  public void setFeature(String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (XMLConstants.FEATURE_SECURE_PROCESSING.equals(name))
      secureProcessing = value;
    else
      super.setFeature(name, value);
  }

  public boolean getFeature(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (XMLConstants.FEATURE_SECURE_PROCESSING.equals(name))
      return secureProcessing;
    return super.getFeature(name);
  }

  public void setProperty(String name, Object object) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (PROPERTY_DATATYPE_LIBRARY_FACTORY.equals(name)) {
      if (object instanceof DatatypeLibraryFactory) {
        datatypeLibraryFactory = (DatatypeLibraryFactory)object;
        defaultDatatypeLibraryFactory = false;
      }
      else
        throw new SAXNotSupportedException("value of \"" + PROPERTY_DATATYPE_LIBRARY_FACTORY +
                                           "\" property does not implement org.relaxng.datatype.DatatypeLibraryFactory");
    }
    else
      super.setProperty(name, object);
  }

  public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
    if (PROPERTY_DATATYPE_LIBRARY_FACTORY.equals(name))
      return getDatatypeLibraryFactory();
    return super.getProperty(name);
  }

  private DatatypeLibraryFactory getDatatypeLibraryFactory() {
    if (defaultDatatypeLibraryFactory) {
      datatypeLibraryFactory = new DatatypeLibraryFactoryImpl(new RegexEngineImpl());
      defaultDatatypeLibraryFactory = false;
    }
    return datatypeLibraryFactory;
  }

  abstract protected Parseable createParseable(Source source, SAXResolver resolver, ErrorHandler eh) throws SAXException;
}
