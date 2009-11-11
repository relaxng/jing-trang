package com.thaiopensource.validate.auto;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.Service;
import com.thaiopensource.validate.Option;

import java.util.Iterator;

public class SchemaReceiverLoader implements SchemaReceiverFactory {
  private final Service<SchemaReceiverFactory> service = Service.newInstance(SchemaReceiverFactory.class);
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             PropertyMap properties) {
    for (Iterator<SchemaReceiverFactory> iter = service.getProviders(); iter.hasNext();) {
      SchemaReceiverFactory srf = iter.next();
      SchemaReceiver sr = srf.createSchemaReceiver(namespaceUri, properties);
      if (sr != null)
        return sr;
    }
    return null;
  }

  public Option getOption(String uri) {
    for (Iterator<SchemaReceiverFactory> iter = service.getProviders(); iter.hasNext();) {
      SchemaReceiverFactory srf = iter.next();
      Option option = srf.getOption(uri);
      if (option != null)
        return option;
    }
    return null;
  }

}
