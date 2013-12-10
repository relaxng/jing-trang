package com.thaiopensource.validate;

import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.SequenceResolver;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.resolver.xml.transform.Transform;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.xml.sax.XMLReaderCreator;
import org.xml.sax.EntityResolver;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.URIResolver;

public class ResolverFactory {
  static private class CustomSAXResolver extends SAXResolver {
    private final XMLReaderCreator xrc;

    private CustomSAXResolver(Resolver resolver, XMLReaderCreator xrc) {
      super(resolver);
      this.xrc = xrc;
    }

    protected XMLReader createXMLReaderWithoutResolver() throws SAXException {
      return xrc.createXMLReader();
    }
  }

  static public SAXResolver createResolver(PropertyMap properties) {
    Resolver[] resolvers = new Resolver[4];
    int i = 0;
    // user-specified Resolver first
    resolvers[0] = properties.get(ValidateProperty.RESOLVER);
    if (resolvers[0] != null)
      i++;
    // EntityResolver before uriResolver
    EntityResolver entityResolver = properties.get(ValidateProperty.ENTITY_RESOLVER);
    URIResolver uriResolver = properties.get(ValidateProperty.URI_RESOLVER);
    if (entityResolver != null)
      resolvers[i++] = SAX.createResolver(entityResolver, uriResolver == null);
    if (uriResolver != null)
      resolvers[i++] = Transform.createResolver(uriResolver);
    while (--i > 0)
      resolvers[i - 1] = new SequenceResolver(resolvers[i - 1], resolvers[i]);
    // XMLReaderCreator last, so it can create an EntityResolver
    XMLReaderCreator xrc = properties.get(ValidateProperty.XML_READER_CREATOR);
    if (xrc != null)
      return new CustomSAXResolver(resolvers[0], xrc);
    return new SAXResolver(resolvers[0]);
  }
}
