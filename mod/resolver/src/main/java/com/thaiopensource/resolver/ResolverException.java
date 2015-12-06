package com.thaiopensource.resolver;

/**
 *
 */
public class ResolverException extends Exception {
  public ResolverException(Throwable t) {
    super(t);
  }
  public ResolverException(String message) {
    super(message);
  }

  public Throwable unwrap() {
    if (getMessage() == null) {
      Throwable t = getCause();
      if (t != null)
        return t;
    }
    return this;
  }
}
