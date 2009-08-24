package com.thaiopensource.resolver.catalog;

import com.thaiopensource.resolver.ResolverException;

import java.io.IOException;

/**
 * A wrapper for a ResolverException to allow it to be passed up by the catalog parser.
 */
public class ResolverIOException extends IOException {
  private final ResolverException resolverException;

  public ResolverIOException(ResolverException resolverException) {
    this.resolverException = resolverException;
  }

  public ResolverException getResolverException() {
    return resolverException;
  }
}
