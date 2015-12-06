package com.thaiopensource.resolver.catalog;

import org.apache.xml.resolver.CatalogManager;

import java.util.List;
import java.util.Vector;

/**
 * A very simple CatalogManager that does not use use property file/system property customization.
 */
class SimpleCatalogManager extends CatalogManager {
  private final Vector<String> catalogUris;

  SimpleCatalogManager(List<String> catalogUris) {
    this.catalogUris = new Vector<String>();
    this.catalogUris.addAll(catalogUris);
    // disable printing to System.out
    setVerbosity(0);
  }

  public Vector<String> getCatalogFiles() {
    return catalogUris;
  }

  public boolean getRelativeCatalogs() {
    return false;
  }

  public boolean getPreferPublic() {
    return true;
  }

  public boolean getIgnoreMissingProperties() {
    return true;
  }

  public boolean getAllowOasisXMLCatalogPI() {
    return false;
  }

  public boolean getUseStaticCatalog() {
    return false;
  }

  public String getCatalogClassName() {
    return null;
  }
}
