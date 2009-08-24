package com.thaiopensource.resolver.xml;

import com.thaiopensource.resolver.Identifier;

/**
 *
 */
public class ExternalIdentifier extends Identifier {
  private final String publicId;

  public ExternalIdentifier(String href, String base, String publicId) {
    super(href, base);
    this.publicId = publicId;
  }

  public String getPublicId() {
    return publicId;
  }
}
