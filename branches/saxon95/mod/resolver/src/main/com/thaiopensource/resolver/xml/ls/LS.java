package com.thaiopensource.resolver.xml.ls;

import com.thaiopensource.resolver.AbstractResolver;
import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.xml.ExternalIdentifier;
import com.thaiopensource.resolver.xml.TargetNamespaceIdentifier;
import com.thaiopensource.resolver.xml.XMLDocumentIdentifier;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 *
 */
public class LS {
  private static final String XML_TYPE = "http://www.w3.org/TR/REC-xml";
  private static final String IANA_MEDIA_TYPE_URI = "http://www.iana.org/assignments/media-types/";

  private LS() {
  }

  public static Resolver createResolver(final LSResourceResolver resourceResolver) {
    return new AbstractResolver() {
      public void resolve(Identifier id, Input input) throws IOException, ResolverException {
        if (input.isResolved())
          return;
        String base = id.getBase();
        String publicId = null;
        String type = null;
        if (id instanceof ExternalIdentifier) {
          publicId = ((ExternalIdentifier)id).getPublicId();
          type = XML_TYPE;
        }
        else if (id instanceof XMLDocumentIdentifier)
          type = ((XMLDocumentIdentifier)id).getNamespaceUri();
        if (type == null) {
          String mediaType = id.getMediaType();
          if (mediaType.indexOf('*') < 0)
            type = IANA_MEDIA_TYPE_URI + mediaType;
        }
        String targetNamespace = null;
        if (id instanceof TargetNamespaceIdentifier)
          targetNamespace = ((TargetNamespaceIdentifier)id).getTargetNamespace();
        LSInput lsInput = resourceResolver.resolveResource(type, targetNamespace, publicId, id.getUriReference(), base);
        if (lsInput == null)
          return;
        input.setEncoding(lsInput.getEncoding());
        input.setUri(lsInput.getSystemId());
        final Reader characterStream = lsInput.getCharacterStream();
        if (characterStream != null) {
          input.setCharacterStream(characterStream);
          return;
        }
        final InputStream byteStream = lsInput.getByteStream();
        if (byteStream != null) {
          input.setByteStream(byteStream);
          return;
        }
        final String stringData = lsInput.getStringData();
        if (stringData != null) {
          input.setCharacterStream(new StringReader(stringData));
          return;
        }
        // we don't support redirecting to a public ID
      }
    };
  }
}
