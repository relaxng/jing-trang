package com.thaiopensource.resolver.xml;

/**
 *
 */
public class ExternalDTDSubsetIdentifier extends ExternalIdentifier {
  private final String doctypeName;

  public ExternalDTDSubsetIdentifier(String href, String base, String publicId, String doctypeName) {
    super(href, base, publicId);
    this.doctypeName = doctypeName;
  }

  public String getDoctypeName() {
    return doctypeName;
  }
}
