package com.thaiopensource.resolver.catalog;

import org.apache.xml.resolver.helpers.BootstrapResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URL;

/**
 * An EntityResolver for use when parsing catalogs.
 */
class CatalogEntityResolver implements EntityResolver {
  private final EntityResolver entityResolver;

  CatalogEntityResolver(EntityResolver entityResolver) {
    this.entityResolver = entityResolver;
  }

  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    if (BootstrapResolver.xmlCatalogPubId.equals(publicId)
        || BootstrapResolver.xmlCatalogSysId.equals(systemId)) {
      URL url = BootstrapResolver.class.getResource("/org/apache/xml/resolver/etc/catalog.dtd");
      if (url != null) {
        InputSource in = new InputSource(url.toString());
        // Avoid any weirdness the parser may perform on URLs
        in.setByteStream(url.openStream());
        in.setPublicId(publicId);
        return in;
      }
    }
    if (entityResolver != null)
      return entityResolver.resolveEntity(publicId, systemId);
    return null;
  }
}
