package com.thaiopensource.relaxng.parse;

import com.thaiopensource.resolver.ResolverException;
import org.xml.sax.SAXException;

public class BuildException extends RuntimeException {
  private final Throwable cause;
  public BuildException(Throwable cause) {
    if (cause == null)
      throw new NullPointerException("null cause");
    this.cause = cause;
  }

  public Throwable getCause() {
    return cause;
  }

  public static BuildException fromSAXException(SAXException e) {
    Exception inner = e.getException();
    if (inner instanceof BuildException)
      return (BuildException)inner;
    return new BuildException(e);
  }

  public static BuildException fromResolverException(ResolverException e) {
    if (e.getMessage() == null) {
      Throwable t = e.unwrap();
      if (t != null) {
        if (t instanceof BuildException)
          throw (BuildException)t;
        throw new BuildException(t);
      }
    }
    throw new BuildException(e);
  }
}
