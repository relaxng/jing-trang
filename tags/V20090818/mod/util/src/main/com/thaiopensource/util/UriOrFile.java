package com.thaiopensource.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public class UriOrFile {
  private UriOrFile() {
  }

  public static String toUri(String uriOrFile) {
    String scheme = getScheme(uriOrFile);
    // if it has a schema that isn't a single letter treat is as a URI,
    // otherwise treat it as a file and convert it to a URI
    return scheme != null && scheme.length() > 1 ? uriOrFile : fileToUri(uriOrFile);
  }

  private static String getScheme(String str) {
    int len = str.length();
    if (len == 0)
      return null;
    if (!isAlpha(str.charAt(0)))
      return null;
    for (int i = 1; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case ':':
	return str.substring(0, i);
      case '+':
      case '-':
	break;
      default:
	if (!isAlnum(c))
	  return null;
	break;
      }
    }
    return null;
  }

  private static boolean isAlpha(char c) {
    return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
  }

  private static boolean isAlnum(char c) {
    return isAlpha(c) || ('0' <= c && c <= '9');
  }

  public static String fileToUri(String file) {
    return fileToUri(new File(file));
  }

  public static String fileToUri(File file) {
    return file.toURI().toString();
  }

  public static String uriToUriOrFile(String uri) {
    // try to avoid throwing an exception in the common case
    if ("file".equalsIgnoreCase(getScheme(uri))) {
      try {
        return new File(new URI(uri)).toString();
      }
      // not a valid URI
      catch (URISyntaxException e) { }
      // not a valid file URI
      catch (IllegalArgumentException e) { }
    }
    return uri;
  }
}
