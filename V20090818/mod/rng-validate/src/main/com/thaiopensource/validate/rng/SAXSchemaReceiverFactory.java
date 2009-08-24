package com.thaiopensource.validate.rng;

import com.thaiopensource.relaxng.parse.sax.SAXParseReceiver;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.ResolverFactory;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.auto.SchemaReceiver;
import com.thaiopensource.validate.auto.SchemaReceiverFactory;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.impl.SchemaReceiverImpl;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.ErrorHandler;

public class SAXSchemaReceiverFactory implements SchemaReceiverFactory {
  public SchemaReceiver createSchemaReceiver(String namespaceUri,
                                             PropertyMap properties) {
    // XXX allow namespaces with incorrect version
    if (!WellKnownNamespaces.RELAX_NG.equals(namespaceUri))
      return null;
    SAXResolver resolver = ResolverFactory.createResolver(properties);
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    return new SchemaReceiverImpl(new SAXParseReceiver(resolver, eh), properties);
  }

  public Option getOption(String uri) {
    return RngProperty.getOption(uri);
  }
}
