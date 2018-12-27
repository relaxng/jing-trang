package com.thaiopensource.validate.schematron;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.FeatureKeys;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

public class OldSaxonSchemaReaderFactory extends SchematronSchemaReaderFactory {
  public SAXTransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }

  public void initTransformerFactory(TransformerFactoryImpl factory) {
    factory.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
  }
}
