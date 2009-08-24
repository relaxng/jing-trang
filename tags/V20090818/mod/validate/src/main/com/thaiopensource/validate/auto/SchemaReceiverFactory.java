package com.thaiopensource.validate.auto;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Option;

public interface SchemaReceiverFactory {
  static final PropertyId<SchemaReceiverFactory> PROPERTY
          = PropertyId.newInstance("SCHEMA_RECEIVER_FACTORY", SchemaReceiverFactory.class);
  SchemaReceiver createSchemaReceiver(String namespaceUri,
                                      PropertyMap properties);
  Option getOption(String uri);
}
