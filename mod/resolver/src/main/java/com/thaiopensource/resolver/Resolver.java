package com.thaiopensource.resolver;

import java.io.IOException;

public interface Resolver {
  void resolve(Identifier id, Input input) throws IOException, ResolverException;
  void open(Input input) throws IOException, ResolverException;
}
