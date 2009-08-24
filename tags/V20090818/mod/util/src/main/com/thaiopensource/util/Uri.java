package com.thaiopensource.util;

import java.net.URI;
import java.net.URISyntaxException;

public class Uri {
  /**
   * Tests whether a string is a valid URI reference.
   * @param s the String to be tested
   * @return true is s is a valid URI reference, false otherwise
   */
  public static boolean isValid(String s) {
    try {
      new URI(UriEncoder.encode(s));
    }
    catch (URISyntaxException e) {
      return false;
    }
    return true;
  }
  
  public static String escapeDisallowedChars(String s) {
    return UriEncoder.encodeAsAscii(s);
  }

  public static String resolve(String base, String uriReference) {
    if (!isAbsolute(uriReference) && base != null && isAbsolute(base)) {
      try {
        URI baseURI = new URI(UriEncoder.encode(base));
        return baseURI.resolve(new URI(UriEncoder.encode(uriReference))).toString();
      }
      catch (URISyntaxException e) {
        // fall back to returning uriReference
      }
    }
    return uriReference;
  }

  /**
   * Tests whether a valid URI reference has a fragment identifier. It is allowed to pass an invalid URI reference,
   * but the result is not defined.
   * @param uri the URI reference to be tested, must not be null
   * @return true if the URI reference has a fragment identifier, false otherwise
   */
  public static boolean hasFragmentId(String uri) {
    return uri.indexOf('#') >= 0;
  }

  /**
   * Tests whether a valid URI reference is absolute. It is allowed to pass an invalid URI reference,
   * but the result is not defined. It is also allowed to pass a valid URI with leading
   * and trailing whitespace.
   * @param uri the URI to be tested, must not be null
   * @return true if the URI reference is absolute, false otherwise
   */
  public static boolean isAbsolute(String uri) {
    int i = uri.indexOf(':');
    if (i < 0)
      return false;
    while (--i >= 0) {
      switch (uri.charAt(i)) {
      case '#':
      case '/':
      case '?':
	return false;
      }
    }
    return true;
  }
}
