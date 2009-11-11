package com.thaiopensource.validate;

import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;

import javax.xml.transform.URIResolver;

/**
 * Provides common properties to control reading schemas and validation.
 *
 * @see Schema#createValidator
 * @see SchemaReader#createSchema
 * @see PropertyMap
 * @see PropertyId
 * @see com.thaiopensource.validate.prop.rng.RngProperty
 * @see com.thaiopensource.validate.prop.schematron.SchematronProperty
 * @see com.thaiopensource.validate.prop.wrap.WrapProperty
 */
public class ValidateProperty {
  private ValidateProperty() {
  }

  /**
   * Property specifying ErrorHandler to be used for reporting errors.  The value
   * to which this PropertyId maps must be an instance of ErrorHandler.
   *
   * @see ErrorHandler
   */
  public static final PropertyId<ErrorHandler> ERROR_HANDLER
          = PropertyId.newInstance("ERROR_HANDLER", ErrorHandler.class);

  /**
   * Property specifying EntityResolver to be used for resolving entities. The value
   * to which this PropertyId maps must be an instance of EntityResolver.
   *
   * @see EntityResolver
   */
  public static PropertyId<EntityResolver> ENTITY_RESOLVER
          = PropertyId.newInstance("ENTITY_RESOLVER", EntityResolver.class);

  /**
   * Property specifying URIResolver to be used for resolving URIs. The value
   * to which this PropertyId maps must be an instance of URIResolver.
   *
   * @see URIResolver
   */
  public static final PropertyId<URIResolver> URI_RESOLVER
          = PropertyId.newInstance("URI_RESOLVER", URIResolver.class);

  /**
   * Property specifying Resolver to be used. The value
   * to which this PropertyId maps must be an instance of Resolver.
   *
   * @see Resolver
   */
  public static final PropertyId<Resolver> RESOLVER
          = PropertyId.newInstance("RESOLVER", Resolver.class);

  /**
   * Property specifying XMLReaderCreator used to create XMLReader objects needed for
   * parsing XML documents.  The value to which this PropertyId maps must be an
   * instance of XMLReaderCreator.
   */
  public static final PropertyId<XMLReaderCreator> XML_READER_CREATOR
          = PropertyId.newInstance("XML_READER_CREATOR", XMLReaderCreator.class);
}
