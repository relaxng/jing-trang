package com.thaiopensource.validate.schematron;

import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;

import net.sf.saxon.Configuration;
import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.FeatureKeys;

import com.thaiopensource.validate.schematron.SchematronSchemaReaderFactory;
import com.thaiopensource.validate.schematron.extfn.ColumnNumberFunctionDefinition;
import com.thaiopensource.validate.schematron.extfn.LineNumberFunctionDefinition;
import com.thaiopensource.validate.schematron.extfn.SystemIdFunctionDefinition;

public class NewSaxonSchemaReaderFactory extends SchematronSchemaReaderFactory {
  @Override
  public SAXTransformerFactory newTransformerFactory() {
    return new TransformerFactoryImpl();
  }

  @Override
  public void initTransformerFactory(TransformerFactory factory) {
    if (factory instanceof TransformerFactoryImpl) {
      Configuration conf = ((TransformerFactoryImpl)factory).getConfiguration();
      // Register extensions functions for determining line and column 
      // information for errors. These will be used as fallback in Saxon 9 HE.
      conf.registerExtensionFunction(new LineNumberFunctionDefinition());
      conf.registerExtensionFunction(new ColumnNumberFunctionDefinition());
      conf.registerExtensionFunction(new SystemIdFunctionDefinition());
    }
    try {
      factory.setAttribute(FeatureKeys.XSLT_VERSION, "2.0");
    } catch (IllegalArgumentException e) {
      // The old Saxon 9 (pre HE/PE/EE) throws this exception.
    }
    factory.setAttribute(FeatureKeys.LINE_NUMBERING, Boolean.TRUE);
    factory.setAttribute(FeatureKeys.VERSION_WARNING, Boolean.FALSE);
  }
}
