package com.thaiopensource.xml.em;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.xml.ExternalEntityIdentifier;

/**
 * Extends the UriEntityManager to use a Resolver.
 */
public class ResolverUriEntityManager extends UriEntityManager {
  private final Resolver resolver;

  public ResolverUriEntityManager(Resolver resolver) {
    this.resolver = resolver;
  }

  public OpenEntity open(String systemId) throws IOException {
    Input input = new Input();
    input.setUri(systemId);
    try {
      return open(input);
    }
    catch (ResolverException e) {
      throw toIOException(e);
    }
  }

  public OpenEntity open(ExternalId xid, boolean isParameterEntity, String entityName) throws IOException {
    Input input = new Input();
    String resolverEntityName = entityName;
    if (isParameterEntity)
      resolverEntityName = "%" + entityName;
    try {
      resolver.resolve(new ExternalEntityIdentifier(xid.getSystemId(),
                                                    xid.getBaseUri(),
                                                    xid.getPublicId(),
                                                    resolverEntityName),
                       input);
      if (input.isResolved())
        return open(input);
      else 
        return super.open(xid, isParameterEntity, entityName);
    }
    catch (ResolverException e) {
      throw toIOException(e);
    }
  }

  private OpenEntity open(Input input) throws ResolverException, IOException {
    resolver.open(input);
    if (!input.isOpen())
      throw new ResolverException("could not open input");
    Reader reader = input.getCharacterStream();
    String encoding = input.getEncoding();
    String systemId = input.getUri();
    if (reader != null) {
      if (encoding == null)
        encoding = "UTF-8"; // XXX not sure if it's safe to pass null here
      return new OpenEntity(reader, systemId, systemId, encoding);
    }
    InputStream in = input.getByteStream();
    if (encoding != null)
      return new OpenEntity(new InputStreamReader(in, encoding),
                            systemId, systemId, encoding);
    return detectEncoding(in, systemId);
  }

  private static IOException toIOException(ResolverException e) {
    String message = e.getMessage();
    Throwable cause = e.getCause();
    if (message == null) {
      if (cause instanceof IOException)
        return (IOException)cause;
      // Avoid IOException(Throwable) because it's 1.6
      return new IOException(cause.getMessage());
    }
    // Avoid IOException(String, Throwable) because it's 1.6
    return new IOException(message);
  }
}
