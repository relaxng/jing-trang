package com.thaiopensource.relaxng.sax;

import com.thaiopensource.relaxng.match.MatchContext;
import com.thaiopensource.relaxng.parse.sax.DtdContext;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.xml.sax.SAXException;

public class Context extends DtdContext implements MatchContext {
  protected PrefixMapping prefixMapping = new PrefixMapping("xml", WellKnownNamespaces.XML, null);

  public Context() {
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    prefixMapping = new PrefixMapping(prefix, "".equals(uri) ? null : uri, prefixMapping);
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    prefixMapping = prefixMapping.getPrevious();
  }

  public String getBaseUri() {
    return null;
  }

  protected static final class PrefixMapping {
    private final String prefix;
    // null for undeclaring
    private final String namespaceURI;
    private final PrefixMapping previous;

    PrefixMapping(String prefix, String namespaceURI, PrefixMapping prev) {
      this.prefix = prefix;
      this.namespaceURI = namespaceURI;
      this.previous = prev;
    }

    PrefixMapping getPrevious() {
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

  public String getPrefix(String namespaceURI) {
    PrefixMapping tem = prefixMapping;
    do {
      if (namespaceURI.equals(tem.namespaceURI) &&
          tem.namespaceURI == resolveNamespacePrefix(tem.prefix))
        return tem.prefix;
      tem = tem.previous;
    } while (tem != null);
    return null;
  }
}
