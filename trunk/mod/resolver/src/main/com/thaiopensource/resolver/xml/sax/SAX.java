package com.thaiopensource.resolver.xml.sax;

import com.thaiopensource.resolver.AbstractResolver;
import com.thaiopensource.resolver.BasicResolver;
import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.xml.ExternalDTDSubsetIdentifier;
import com.thaiopensource.resolver.xml.ExternalEntityIdentifier;
import com.thaiopensource.resolver.xml.ExternalIdentifier;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;

import java.io.IOException;

/**
 *
 */
public class SAX {
  private SAX() {
  }

  private static final class EntityResolverWrapper extends AbstractResolver {
    private final EntityResolver entityResolver;
    private final EntityResolver2 entityResolver2;
    private final boolean promiscuous;

    private EntityResolverWrapper(EntityResolver entityResolver, boolean promiscuous) {
      this.entityResolver = entityResolver;
      if (entityResolver instanceof EntityResolver2)
        entityResolver2 = (EntityResolver2)entityResolver;
      else
        entityResolver2 = null;
      this.promiscuous = promiscuous;
    }

    public void resolve(Identifier id, Input input) throws IOException, ResolverException {
      if (input.isResolved())
        return;
      String publicId;
      String entityName = null;
      if (id instanceof ExternalIdentifier) {
        publicId = ((ExternalIdentifier)id).getPublicId();
        if (id instanceof ExternalEntityIdentifier)
          entityName = ((ExternalEntityIdentifier)id).getEntityName();
        else if (id instanceof ExternalDTDSubsetIdentifier)
          entityName = "[dtd]";
      }
      else {
        if (!promiscuous)
          return;
        publicId = null;
      }
      try {
        InputSource inputSource;
        if (entityName != null && entityResolver2 != null)
          inputSource = entityResolver2.resolveEntity(entityName,
                                                      publicId,
                                                      id.getBase(),
                                                      id.getUriReference());
        else
          inputSource = entityResolver.resolveEntity(publicId, getSystemId(id));
        if (inputSource != null)
          setInput(input, inputSource);
      }
      catch (SAXException e) {
        throw toResolverException(e);
      }
    }
  }

  public static Resolver createResolver(final EntityResolver entityResolver, boolean promiscuous) {
    return new EntityResolverWrapper(entityResolver, promiscuous);
  }

  public static EntityResolver2 createEntityResolver(Resolver resolver) {
    if (resolver == null)
      throw new NullPointerException();
    return new EntityResolverImpl(resolver);
  }

  public static Input createInput(InputSource inputSource) {
    Input input = new Input();
    setInput(input, inputSource);
    return input;
  }
  // public because needed by transform package
  public static void setInput(Input input, InputSource inputSource) {
    input.setByteStream(inputSource.getByteStream());
    input.setCharacterStream(inputSource.getCharacterStream());
    input.setUri(inputSource.getSystemId());
    input.setEncoding(inputSource.getEncoding());
  }

  public static Exception getWrappedException(SAXException e) {
    // not purely a wrapper
    if (e.getMessage() != null)
      return null;
    return e.getException();
  }

  public static ResolverException toResolverException(SAXException e) {
    Exception wrapped = getWrappedException(e);
    if (wrapped != null) {
      if (wrapped instanceof ResolverException)
        return (ResolverException)wrapped;
      return new ResolverException(wrapped);
    }
    return new ResolverException(e);
  }
  
  public static SAXException toSAXException(ResolverException e) {
    Throwable cause = e.getCause();
    if (cause != null && cause instanceof SAXException)
      return (SAXException)cause;
    return new SAXException(e);
  }

  static InputSource createInputSource(Input input) {
    InputSource inputSource = new InputSource();
    inputSource.setByteStream(input.getByteStream());
    inputSource.setCharacterStream(input.getCharacterStream());
    inputSource.setEncoding(input.getEncoding());
    inputSource.setSystemId(input.getUri());
    return inputSource;
  }

  static String getSystemId(Identifier id) {
    try {
      return BasicResolver.resolveUri(id);
    }
    catch (ResolverException e) { }
    return id.getUriReference();
  }

  // precondition: input.isResolved()
  static InputSource createInputSource(Identifier id, Input input) {
    InputSource inputSource = createInputSource(input);
    if (id instanceof ExternalIdentifier)
      inputSource.setPublicId(((ExternalIdentifier)id).getPublicId());
    if (inputSource.getSystemId() == null)
      inputSource.setSystemId(getSystemId(id));
    return inputSource;
  }

  static private class EntityResolverImpl implements EntityResolver2 {
    private final Resolver resolver;

    private EntityResolverImpl(Resolver resolver) {
      this.resolver = resolver;
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
      if (systemId == null)
        return null;
      ExternalIdentifier id = new ExternalIdentifier(systemId, null, publicId);
      Input input = new Input();
      try {
        resolver.resolve(id, input);
      }
      catch (ResolverException e) {
        throw toSAXException(e);
      }
      if (input.isResolved())
        return createInputSource(id, input);
      return null;
    }

    public InputSource resolveEntity(String name, String publicId, String base, String systemId) throws SAXException, IOException {
      if (systemId == null)
        return null;
      ExternalIdentifier id;
      if ("[doc]".equals(name))
        id = new ExternalDTDSubsetIdentifier(systemId, base, publicId, null);
      else if (name == null || name.indexOf('[') >= 0 || name.indexOf('#') >= 0)
        id = new ExternalIdentifier(systemId, base, publicId);
      else
        id = new ExternalEntityIdentifier(systemId, base, publicId, name);
      Input input = new Input();
      try {
        resolver.resolve(id, input);
      }
      catch (ResolverException e) {
        throw toSAXException(e);
      }
      if (input.isResolved())
        return createInputSource(id, input);
      return null;
    }

    public InputSource getExternalSubset(String name, String base) throws SAXException, IOException {
      return null;
    }
  }
}
