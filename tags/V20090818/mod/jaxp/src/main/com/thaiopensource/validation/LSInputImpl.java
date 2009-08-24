package com.thaiopensource.validation;

import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.Reader;

/**
 * A straightforward default implementation of LSInput.
 * @see LSInput
 */
public class LSInputImpl implements LSInput {
  private Reader characterStream;
  private InputStream byteStream;
  private String systemId;
  private String publicId;
  private String baseURI;
  private String encoding;
  private boolean certifiedText;
  private String stringData;

  public Reader getCharacterStream() {
    return characterStream;
  }

  public void setCharacterStream(Reader characterStream) {
    this.characterStream = characterStream;
  }

  public InputStream getByteStream() {
    return byteStream;
  }

  public void setByteStream(InputStream byteStream) {
    this.byteStream = byteStream;
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public String getPublicId() {
    return publicId;
  }

  public void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  public String getBaseURI() {
    return baseURI;
  }

  public void setBaseURI(String baseURI) {
    this.baseURI = baseURI;
  }

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public boolean getCertifiedText() {
    return certifiedText;
  }

  public void setCertifiedText(boolean certifiedText) {
    this.certifiedText = certifiedText;
  }

  public String getStringData() {
    return stringData;
  }

  public void setStringData(String stringData) {
    this.stringData = stringData;
  }
}
