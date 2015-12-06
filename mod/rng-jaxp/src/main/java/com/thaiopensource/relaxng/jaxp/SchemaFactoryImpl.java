package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.datatype.xsd.DatatypeLibraryFactoryImpl;
import com.thaiopensource.datatype.xsd.regex.java.RegexEngineImpl;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.pattern.SchemaBuilderImpl;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.xml.ls.LS;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.validation.Schema2;
import com.thaiopensource.validation.SchemaFactory2;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.util.VoidValue;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXParseException;
import org.xml.sax.Locator;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

/**
 * A SchemaFactory that supports RELAX NG.
 * This class is abstract: it has two concrete subclasses, one that supports the XML syntax
 * and one that supports the compact syntax.
 * @see XMLSyntaxSchemaFactory
 * @see CompactSyntaxSchemaFactory
 */
public abstract class SchemaFactoryImpl extends SchemaFactory2 {
  private DatatypeLibraryFactory datatypeLibraryFactory = null;
  /* If this is true, then logically datatypeLibraryFactory is an instance of DatatypeLibraryLoader,
     but we create it lazily, so that we don't need to create it if the user specifies their own. */
  private boolean defaultDatatypeLibraryFactory = true;

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

  public Schema2 newSchema(SAXSource source) throws SAXException {
    Resolver resolver = null;
    LSResourceResolver resourceResolver = getResourceResolver();
    if (resourceResolver != null)
      resolver = LS.createResolver(resourceResolver);
    SAXResolver saxResolver = new SAXResolver(resolver);
    ErrorHandler eh = getErrorHandler();
    if (eh == null)
      eh = new DraconianErrorHandler();
    Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> parseable
            = createParseable(source, saxResolver, eh);
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    try {
      return new SchemaImpl(this, spb, SchemaBuilderImpl.parse(parseable, eh, getDatatypeLibraryFactory(), spb, false));
    }
    catch (IOException io) {
      // this is a truly bizarre API; why can't we just throw the IOException
      SAXParseException e = new SAXParseException(io.getMessage(), null, io);
      eh.fatalError(e);
      throw e;
    }
    catch (IllegalSchemaException e) {
      // we have already reported something for this error, so don't give it to the error handler
      throw new SAXException("invalid schema");
    }
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

  abstract protected Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl>
  createParseable(SAXSource source, SAXResolver resolver, ErrorHandler eh) throws SAXException;
}
