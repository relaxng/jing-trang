package com.thaiopensource.relaxng.javax;

import com.thaiopensource.xml.sax.BasicResolver;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.StringReader;

class Resolver extends BasicResolver {
  private final LSResourceResolver resourceResolver;

  static private final String XML_ENTITY_LS_RESOURCE_TYPE = "http://www.w3.org/TR/REC-xml";

  Resolver(LSResourceResolver resourceResolver) {
    super();
    this.resourceResolver = resourceResolver;
  }

  public SAXSource resolve(String href, String base) throws IOException, SAXException {
    LSInput input = resourceResolver.resolveResource(WellKnownNamespaces.RELAX_NG,
                                                     null, // namespaceURI
                                                     null, // publicId
                                                     href,
                                                     base);
    if (input != null)
      return new SAXSource(toInputSource(input));
    return super.resolve(href, base);
  }

  public XMLReader createXMLReader() throws SAXException {
    XMLReader xr = super.createXMLReader();
    xr.setEntityResolver(new EntityResolver() {
      public InputSource resolveEntity(String publicId, String systemId){
        LSInput input = resourceResolver.resolveResource(XML_ENTITY_LS_RESOURCE_TYPE,
                                                         null, // namespaceURI
                                                         publicId,
                                                         systemId,
                                                         null);
        if (input == null)
          return null;
        return toInputSource(input);
      }
    });
    return xr;
  }

  static private InputSource toInputSource(LSInput input) {
    InputSource inputSource = new InputSource();
    inputSource.setCharacterStream(input.getCharacterStream());
    inputSource.setByteStream(input.getByteStream());
    String str = input.getStringData();
    if (str != null && inputSource.getCharacterStream() == null)
      inputSource.setCharacterStream(new StringReader(str));
    inputSource.setEncoding(input.getEncoding());
    inputSource.setPublicId(input.getPublicId());
    inputSource.setSystemId(input.getSystemId());
    return inputSource;
  }
}
