package com.thaiopensource.relaxng.sax;

import com.thaiopensource.relaxng.parse.sax.DtdContext;
import com.thaiopensource.xml.util.WellKnownNamespaces;

public class Context extends DtdContext {
  protected PrefixMapping prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);

  public Context() {
  }

  public void startPrefixMapping(String prefix, String uri) {
    prefixMapping = new PrefixMapping(prefix, uri, prefixMapping);
  }

  public void endPrefixMapping(String prefix) {
    prefixMapping = prefixMapping.getPrevious();
  }

  public String getBaseUri() {
    return null;
  }

  protected static final class PrefixMapping {
    private final String prefix;
    private final String namespaceURI;
    private final PatternValidator.PrefixMapping previous;

    PrefixMapping(String prefix, String namespaceURI, PatternValidator.PrefixMapping prev) {
      this.prefix = prefix;
      this.namespaceURI = namespaceURI;
      this.previous = prev;
    }

    PatternValidator.PrefixMapping getPrevious() {
      return previous;
    }
  }

  public String resolveNamespacePrefix(String prefix) {
    PrefixMapping tem = prefixMapping;
    do {
      if (tem.prefix.equals(prefix))
        return tem.namespaceURI;
      tem = tem.previous;
    } while (tem != null);
    return null;
  }

  public void reset() {
    prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);
    clearDtdContext();
  }
}
