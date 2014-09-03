package com.thaiopensource.resolver.load;

import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.SequenceResolver;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.transform.Transform;
import org.xml.sax.EntityResolver;

import javax.xml.transform.URIResolver;

/**
 * XXX maybe get rid of this
 */
public class ResolverLoader {
  public static Resolver loadResolver(String className, ClassLoader loader) throws ResolverLoadException {
    Object obj;
    try {
      if (loader == null) {
        loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
          loader = ClassLoader.getSystemClassLoader();
          if (loader == null)
            throw new ResolverLoadException("no class loader");
        }
      }
      obj = loader.loadClass(className).newInstance();
    }
    catch (Exception e) {
      throw new ResolverLoadException(e);
    }
    if (obj instanceof Resolver)
      return (Resolver)obj;
    Resolver entityResolver = null;
    Resolver uriResolver = null;
    if (obj instanceof URIResolver)
      uriResolver = Transform.createResolver((URIResolver)obj);
    if (obj instanceof EntityResolver)
      entityResolver = SAX.createResolver((EntityResolver)obj, uriResolver == null);
    if (uriResolver == null) {
      if (entityResolver == null)
        throw new ResolverLoadException(className +
                                        " not an instance of javax.xml.transform.URIResolver or org.xml.sax.EntityResolver");
      return entityResolver;
    }
    if (entityResolver == null)
      return uriResolver;
    // do the entityResolver first so that it has first go at ExternalIdentifier
    return new SequenceResolver(entityResolver, uriResolver);
  }
}
