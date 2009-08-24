package com.thaiopensource.resolver.xml;

import com.thaiopensource.resolver.Identifier;

/**
 * An Identifier for an XML document.
 */
public class XMLDocumentIdentifier extends Identifier {
  private final String namespaceUri;
  public static final String MEDIA_TYPE = "application/xml";

  /**
   *
   * @param href
   * @param base
   * @param namespaceUri the expected namespace URI of the root element of the XML document
   */
  public XMLDocumentIdentifier(String href, String base, String namespaceUri) {
    super(href, base);
    this.namespaceUri = namespaceUri;
  }

  /**
   *
   * @return the expected namespace name of root element; "" if no namespace is expected;
   * null if no information is available about the expected namespace name.
   */
  public String getNamespaceUri() {
    return namespaceUri;
  }

  public String getMediaType() {
    return MEDIA_TYPE;
  }
}
