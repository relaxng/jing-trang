package com.thaiopensource.resolver.catalog;

import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.xml.XMLDocumentIdentifier;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.readers.OASISXMLCatalogReader;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.net.URL;

/**
 * A catalog with customized parsing of catalog files. In particular, it only supports
 * OASIS XML Catalogs and it uses a SAXResolver for access to the catalog URIs.
 */
class OasisCatalog extends Catalog {
  private final SAXResolver saxResolver;
  
  OasisCatalog(CatalogManager catalogManager, SAXResolver saxResolver) {
    super(catalogManager);
    this.saxResolver = saxResolver;
    // don't call setupReaders; since we use our own parseCatalogFile
    // we'll load the catalogs lazily
  }

  protected void parseCatalogFile(String uri) throws IOException {
    OASISXMLCatalogReader catalogReader = new OASISXMLCatalogReader();
    try {
      SAXSource source = saxResolver.resolve(new XMLDocumentIdentifier(uri, null, OASISXMLCatalogReader.namespaceName));
      String systemId = source.getInputSource().getSystemId();
      if (systemId == null)
        systemId = uri;
      base = new URL(systemId);
      catalogReader.setCatalog(this);
      XMLReader xmlReader = source.getXMLReader();
      xmlReader.setEntityResolver(new CatalogEntityResolver(xmlReader.getEntityResolver()));
      xmlReader.setContentHandler(catalogReader);
      xmlReader.setErrorHandler(new DraconianErrorHandler());
      xmlReader.parse(source.getInputSource());
    }
    catch (SAXException e) {
      Exception wrapped = e.getException();
      // this will get unwrapped by CatalogResolver
      throw new ResolverIOException(wrapped instanceof ResolverException
                                    ? (ResolverException)wrapped
                                    : new ResolverException(e));
    }
  }
}
