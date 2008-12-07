package com.thaiopensource.validate;

import com.thaiopensource.util.Service;

import java.util.Iterator;

/**
 * A SchemaReaderFactory that automatically discovers SchemaReader implementations.
 * For a SchemeaReader implementation to be discoverable by this class, it must have
 * a factory class with a no-argument constructor implementing SchemaReaderFactory,
 * and the fully-qualified name of this factory class must be listed in the file
 * <code>META-INF/services/com.thaiopensource.validate.SchemaReaderFactory</code>.
 */
public class SchemaReaderLoader implements SchemaReaderFactory {
  private final Service<SchemaReaderFactory> service = Service.newInstance(SchemaReaderFactory.class);
  public SchemaReader createSchemaReader(String namespaceUri) {
    for (Iterator<SchemaReaderFactory> iter = service.getProviders(); iter.hasNext();) {
      SchemaReaderFactory srf = iter.next();
      SchemaReader sr = srf.createSchemaReader(namespaceUri);
      if (sr != null)
        return sr;
    }
    return null;
  }

  public Option getOption(String uri) {
    for (Iterator<SchemaReaderFactory> iter = service.getProviders(); iter.hasNext();) {
      SchemaReaderFactory srf = iter.next();
      Option option = srf.getOption(uri);
      if (option != null)
        return option;
    }
    return null;
  }
}
