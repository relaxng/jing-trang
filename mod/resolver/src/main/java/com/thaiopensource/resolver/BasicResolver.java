package com.thaiopensource.resolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.io.IOException;

/**
 *
 */
public class BasicResolver implements Resolver {
  static private final BasicResolver theInstance = new BasicResolver();

  protected BasicResolver() {
  }

  public static BasicResolver getInstance() {
    return theInstance;
  }

  public void resolve(Identifier id, Input input) throws IOException, ResolverException {
    if (!input.isResolved())
      input.setUri(resolveUri(id));
  }

  public void open(Input input) throws IOException, ResolverException {
    if (!input.isUriDefinitive())
      return;
    URI uri;
    try {
      uri = new URI(input.getUri());
    }
    catch (URISyntaxException e) {
      throw new ResolverException(e);
    }
    if (!uri.isAbsolute())
      throw new ResolverException("cannot open relative URI: " + uri);
    URL url = new URL(uri.toASCIIString());
    // XXX should set the encoding properly
    // XXX if this is HTTP and we've been redirected, should do input.setURI with the new URI
    input.setByteStream(url.openStream());
  }

  public static String resolveUri(Identifier id) throws ResolverException {
    try {
      final String uriRef = id.getUriReference();
      URI uri = new URI(uriRef);
      if (!uri.isAbsolute()) {
        String base = id.getBase();
        if (base != null)
          return new URI(base).resolve(uri).toString();
      }
      return uriRef;
    }
    catch (URISyntaxException e) {
      throw new ResolverException(e);
    }
  }
}
