package com.thaiopensource.validate.schematron;

import org.apache.xalan.processor.TransformerFactoryImpl;

import javax.xml.transform.TransformerFactory;

public class XalanSchemaReaderFactory extends SchematronSchemaReaderFactory {
  public TransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }
}
