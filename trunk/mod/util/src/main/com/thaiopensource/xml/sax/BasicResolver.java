package com.thaiopensource.xml.sax;

import com.thaiopensource.util.Uri;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;

public class BasicResolver implements XMLReaderCreator {
  protected final XMLReaderCreator xrc;

  public BasicResolver() {
    this(null);
  }

  public BasicResolver(XMLReaderCreator xrc) {
    this.xrc = xrc == null ? new Jaxp11XMLReaderCreator() : xrc;
  }

  public XMLReader createXMLReader() throws SAXException {
    return xrc.createXMLReader();
  }

  public InputSource open(InputSource in) throws IOException, SAXException {
    if (in.getCharacterStream() != null || in.getByteStream() != null)
      return in;
    String systemId = in.getSystemId();
    if (systemId == null)
      throw new IllegalArgumentException("byteStream, charStream and systemId of the InputSource are all null");
    String uri = Uri.escapeDisallowedChars(systemId);
    URL url = new URL(uri);
    InputSource opened = new InputSource(systemId);
    opened.setPublicId(in.getPublicId());
    opened.setEncoding(in.getEncoding());
    // XXX if encoding is null, should use charset parameter of content-type to set encoding in text/xml case
    opened.setByteStream(url.openStream());
    return opened;
  }

  public SAXSource resolve(String href, String base) throws IOException, SAXException {
    try {
      href = new URI(base).resolve(href).toString();
    }
    catch (URISyntaxException e) { }
    return new SAXSource(new InputSource(href));
  }

}
