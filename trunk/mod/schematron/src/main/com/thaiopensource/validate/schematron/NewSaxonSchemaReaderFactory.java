package com.thaiopensource.validate.schematron;

import net.sf.saxon.lib.FeatureKeys;
import net.sf.saxon.TransformerFactoryImpl;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

public class NewSaxonSchemaReaderFactory extends SchematronSchemaReaderFactory {
  public SAXTransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }

  public void initTransformerFactory(TransformerFactory factory) {
    try {
      factory.setAttribute(FeatureKeys.XSLT_VERSION, "2.0");
    } catch (IllegalArgumentException e) {
      // The old Saxon 9 (pre HE/PE/EE) throws this exception.
    }
    factory.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
    factory.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
  }
}
