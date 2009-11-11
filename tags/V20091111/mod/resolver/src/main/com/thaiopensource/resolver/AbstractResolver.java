package com.thaiopensource.resolver;

import java.io.IOException;

/**
 *
 */
public class AbstractResolver implements Resolver {
  public void resolve(Identifier id, Input input) throws IOException, ResolverException {
    // do nothing
  }

  public void open(Input input) throws IOException, ResolverException {
    // do nothing
  }
}
