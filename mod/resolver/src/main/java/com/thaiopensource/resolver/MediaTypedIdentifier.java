package com.thaiopensource.resolver;

/**
 *
 */
public class MediaTypedIdentifier extends Identifier {
  private final String mediaType;

  public MediaTypedIdentifier(String href, String base, String mediaType) {
    super(href, base);
    this.mediaType = mediaType;
  }

  public String getMediaType() {
    return mediaType;
  }
}
