package com.thaiopensource.resolver.xml.transform;

import com.thaiopensource.resolver.AbstractResolver;
import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.sax.SAXInput;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

/**
 *
 */
public class Transform {
  private Transform() { }

  /**
   * Creates a URIResolver that returns a SAXSource.
   * @param resolver
   * @return
   */
  public static URIResolver createSAXURIResolver(Resolver resolver) {
    final SAXResolver saxResolver = new SAXResolver(resolver);
    return new URIResolver() {
      public Source resolve(String href, String base) throws TransformerException {
        try {
          return saxResolver.resolve(href, base);
        }
        catch (SAXException e) {
          throw toTransformerException(e);
        }
        catch (IOException e) {
          throw new TransformerException(e);
        }
      }
    };
  }

  public static Resolver createResolver(final URIResolver uriResolver) {
    return new AbstractResolver() {
      public void resolve(Identifier id, Input input) throws IOException, ResolverException {
        if (input.isResolved())
          return;
        Source source;
        try {
          source = uriResolver.resolve(id.getUriReference(), id.getBase());
        }
        catch (TransformerException e) {
          throw toResolverException(e);
        }
        if (source == null)
          return;
        if (source instanceof SAXSource) {
          setInput(input, (SAXSource)source);
          return;
        }
        InputSource in = SAXSource.sourceToInputSource(source);
        if (in != null) {
          SAX.setInput(input, in);
          return;
        }
        // XXX handle StAXSource
        throw new ResolverException("URIResolver returned unsupported subclass of Source");
      }
    };
  }

  private static void setInput(Input input, SAXSource source) {
    XMLReader reader = source.getXMLReader();
    if (reader != null) {
      if (input instanceof SAXInput)
        ((SAXInput)input).setXMLReader(reader);
    }
    InputSource in = source.getInputSource();
    if (in != null)
      SAX.setInput(input, in);
  }

  private static TransformerException toTransformerException(SAXException e) {
    Exception wrapped = SAX.getWrappedException(e);
    if (wrapped != null) {
      if (wrapped instanceof TransformerException)
        return (TransformerException)wrapped;
      return new TransformerException(wrapped);
    }
    return new TransformerException(e);
  }

  private static ResolverException toResolverException(TransformerException e) {
    Throwable wrapped = getWrappedException(e);
    if (wrapped != null) {
      if (wrapped instanceof ResolverException)
        return (ResolverException)wrapped;
      return new ResolverException(wrapped);
    }
    return new ResolverException(e);
  }

  private static Throwable getWrappedException(TransformerException e) {
    Throwable wrapped = e.getException();
    if (wrapped == null)
      return null;
    String message = e.getMessage();
    if (message != null && !message.equals(wrapped.getMessage()))
      return null;
    return wrapped;
  }
}
