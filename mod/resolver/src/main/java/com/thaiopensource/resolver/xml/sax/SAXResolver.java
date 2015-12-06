package com.thaiopensource.resolver.xml.sax;

import com.thaiopensource.resolver.BasicResolver;
import com.thaiopensource.resolver.Identifier;
import com.thaiopensource.resolver.Input;
import com.thaiopensource.resolver.Resolver;
import com.thaiopensource.resolver.ResolverException;
import com.thaiopensource.resolver.SequenceResolver;
import com.thaiopensource.resolver.xml.XMLDocumentIdentifier;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

/**
 *
 */
public class SAXResolver {
  private final Resolver resolver;
  private final SAXParserFactory parserFactory;

  public SAXResolver(Resolver resolver) {
    Resolver tem = BasicResolver.getInstance();
    if (resolver != null)
      tem = new SequenceResolver(resolver, tem);
    this.resolver = tem;
    parserFactory = SAXParserFactory.newInstance();
    parserFactory.setNamespaceAware(true);
    parserFactory.setValidating(false);
  }

  public SAXResolver() {
    this(null);
  }

  public Resolver getResolver() {
    return resolver;
  }

  public SAXSource resolve(String href, String base) throws SAXException, IOException {
    return resolve(new Identifier(href, base));
  }

  public SAXSource resolve(String href, String base, String rootNamespaceUri) throws SAXException, IOException {
    return resolve(new XMLDocumentIdentifier(href, base, rootNamespaceUri));
  }
  
  public SAXSource resolve(Identifier id) throws SAXException, IOException {
    SAXInput input = new SAXInput();
    try {
      resolver.resolve(id, input);
      if (!input.isResolved())
        input.setUri(BasicResolver.resolveUri(id));
      InputSource inputSource = SAX.createInputSource(id, input);
      XMLReader xr = input.getXMLReader();
      if (xr == null)
        xr = createXMLReader();
      return new SAXSource(xr, inputSource);
    }
    catch (ResolverException e) {
      throw SAX.toSAXException(e);
    }
  }

  public SAXSource createSAXSource(Input input) throws SAXException {
    InputSource inputSource = SAX.createInputSource(input);
    XMLReader xr = null;
    if (input instanceof SAXInput)
      xr = ((SAXInput)input).getXMLReader();
    if (xr == null)
      xr = createXMLReader();
    return new SAXSource(xr, inputSource);
  }

  public XMLReader createXMLReader() throws SAXException {
    XMLReader xr = createXMLReaderWithoutResolver();
    xr.setEntityResolver(SAX.createEntityResolver(resolver));
    return xr;
  }

  protected XMLReader createXMLReaderWithoutResolver() throws SAXException {
   try {
      return parserFactory.newSAXParser().getXMLReader();
    }
    catch (ParserConfigurationException e) {
      throw new SAXException(e);
    }
  }

  public InputSource open(InputSource inputSource) throws SAXException, IOException {
    if (inputSource.getByteStream() != null || inputSource.getCharacterStream() != null)
      return inputSource;
    Input input = SAX.createInput(inputSource);
    try {
      resolver.open(input);
    }
    catch (ResolverException e) {
      throw SAX.toSAXException(e);
    }
    String publicId = inputSource.getPublicId();
    inputSource = SAX.createInputSource(input);
    inputSource.setPublicId(publicId);
    return inputSource;
  }
}
