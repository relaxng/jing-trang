package com.thaiopensource.resolver;

/**
 *
 */
public class Identifier {
  private final String href;
  private final String base;

  public Identifier(String href, String base) {
    if (href == null)
      throw new NullPointerException();
    this.href = href;
    this.base = base;
  }

  public Identifier(String href) {
    this(href, null);
  }

  /**
   * Must return non-null
   * @return
   */
  public String getUriReference() {
    return href;
  }

  /**
   *
   * @return maybe null
   */
  public String getBase() {
    return base;
  }

  /**
   * Return a canonical media type for what's expected. Never null.
   */
  public String getMediaType() {
    return "*/*";
  }
}
