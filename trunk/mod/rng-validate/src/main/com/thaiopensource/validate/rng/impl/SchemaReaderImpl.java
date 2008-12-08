package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.datatype.DatatypeLibraryLoader;
import com.thaiopensource.relaxng.parse.IllegalSchemaException;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.pattern.AnnotationsImpl;
import com.thaiopensource.relaxng.pattern.CommentListImpl;
import com.thaiopensource.relaxng.pattern.FeasibleTransform;
import com.thaiopensource.relaxng.pattern.IdTypeMap;
import com.thaiopensource.relaxng.pattern.IdTypeMapBuilder;
import com.thaiopensource.relaxng.pattern.NameClass;
import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.PatternDumper;
import com.thaiopensource.relaxng.pattern.SchemaBuilderImpl;
import com.thaiopensource.relaxng.pattern.SchemaPatternBuilder;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.VoidValue;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.AbstractSchemaReader;
import com.thaiopensource.validate.CombineSchema;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.prop.wrap.WrapProperty;
import org.relaxng.datatype.DatatypeLibraryFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

public abstract class SchemaReaderImpl extends AbstractSchemaReader {
  private static final PropertyId<?>[] supportedPropertyIds = {
    ValidateProperty.XML_READER_CREATOR,
    ValidateProperty.ERROR_HANDLER,
    ValidateProperty.ENTITY_RESOLVER,
    ValidateProperty.URI_RESOLVER,
    ValidateProperty.RESOLVER,
    RngProperty.DATATYPE_LIBRARY_FACTORY,
    RngProperty.CHECK_ID_IDREF,
    RngProperty.FEASIBLE,
    WrapProperty.ATTRIBUTE_OWNER,
  };

  public Schema createSchema(SAXSource source, PropertyMap properties)
          throws IOException, SAXException, IncorrectSchemaException {
    SchemaPatternBuilder spb = new SchemaPatternBuilder();
    SAXResolver resolver = ResolverFactory.createResolver(properties);
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    DatatypeLibraryFactory dlf = properties.get(RngProperty.DATATYPE_LIBRARY_FACTORY);
    if (dlf == null)
      dlf = new DatatypeLibraryLoader();
    try {
      Pattern start = SchemaBuilderImpl.parse(createParseable(source, resolver, eh, properties), eh, dlf, spb,
                                              properties.contains(WrapProperty.ATTRIBUTE_OWNER));
      return wrapPattern(start, spb, properties);
    }
    catch (IllegalSchemaException e) {
      throw new IncorrectSchemaException();
    }
  }

  public Option getOption(String uri) {
    return RngProperty.getOption(uri);
  }

  static private class SimplifiedSchemaPropertyMap implements PropertyMap {
    private final PropertyMap base;
    private final Pattern start;

    SimplifiedSchemaPropertyMap(PropertyMap base, Pattern start) {
      this.base = base;
      this.start = start;
    }

    public <T> T get(PropertyId<T> pid) {
      if (pid == RngProperty.SIMPLIFIED_SCHEMA) {
        String simplifiedSchema = PatternDumper.toString(start);
        return pid.getValueClass().cast(simplifiedSchema);
      }
      else
        return base.get(pid);
    }

    public PropertyId<?> getKey(int i) {
      return i == base.size() ? RngProperty.SIMPLIFIED_SCHEMA : base.getKey(i);
    }

    public int size() {
      return base.size() + 1;
    }

    public boolean contains(PropertyId<?> pid) {
      return base.contains(pid) || pid == RngProperty.SIMPLIFIED_SCHEMA;
    }
  }

  static Schema wrapPattern(Pattern start, SchemaPatternBuilder spb, PropertyMap properties) throws SAXException, IncorrectSchemaException {
    if (properties.contains(RngProperty.FEASIBLE))
      start = FeasibleTransform.transform(spb, start);
    properties = new SimplifiedSchemaPropertyMap(AbstractSchema.filterProperties(properties, supportedPropertyIds),
                                                 start);
    Schema schema = new PatternSchema(spb, start, properties);
    if (spb.hasIdTypes() && properties.contains(RngProperty.CHECK_ID_IDREF)) {
      ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
      IdTypeMap idTypeMap = new IdTypeMapBuilder(eh, start).getIdTypeMap();
      if (idTypeMap == null)
        throw new IncorrectSchemaException();
      Schema idSchema;
      if (properties.contains(RngProperty.FEASIBLE))
        idSchema = new FeasibleIdTypeMapSchema(idTypeMap, properties);
      else
        idSchema = new IdTypeMapSchema(idTypeMap, properties);
      schema = new CombineSchema(schema, idSchema, properties);
    }
    return schema;
  }

  protected abstract Parseable<Pattern, NameClass, Locator, VoidValue, CommentListImpl, AnnotationsImpl> createParseable(SAXSource source, SAXResolver resolver, ErrorHandler eh, PropertyMap properties)
          throws SAXException;

}
