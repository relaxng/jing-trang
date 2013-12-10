package com.thaiopensource.xml.infer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.thaiopensource.datatype.xsd.DatatypeLibraryFactoryImpl;
import com.thaiopensource.datatype.xsd.regex.java.RegexEngineImpl;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import com.thaiopensource.xml.util.Name;

/**
 * Test the inference handler
 */
public class TestInferHandler {
  @DataProvider(name = "samples")
  public Object[][] createDateSamples() {
    return new Object[][] {
        {
          "<sample>\n" 
          + "  <element att=\"true\"/>\n"
          + "  <element att=\"false\"/>\n"
          + "  <element att=\"true\"/>\n" 
          + "</sample>", 
          "boolean" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"1\"/>\n"
          + "  <element att=\"2\"/>\n"
          + "  <element att=\"3\"/>\n" 
          + "</sample>", 
          "integer" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"1.1\"/>\n"
          + "  <element att=\"0.3\"/>\n"
          + "  <element att=\"90\"/>\n" 
          + "</sample>", 
          "decimal" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"1.23E100\"/>\n"
          + "  <element att=\"2\"/>\n"
          + "  <element att=\"2.34\"/>\n" 
          + "</sample>", 
          "double" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"test\"/>\n"
          + "  <element att=\"x\"/>\n"
          + "  <element att=\"y\"/>\n" 
          + "</sample>", 
          "NCName" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"12:23:00\"/>\n"
          + "  <element att=\"10:01:11-05:00\"/>\n"
          + "  <element att=\"09:01:07Z\"/>\n" 
          + "</sample>", 
          "time" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"2012-10-09\"/>\n"
          + "  <element att=\"2010-02-01\"/>\n"
          + "  <element att=\"2009-10-10\"/>\n" 
          + "</sample>", 
          "date" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"2010-01-20T08:00:10\"/>\n"
          + "  <element att=\"2012-02-01T10:00:03-05:00\"/>\n"
          + "  <element att=\"2010-01-20T12:00:00Z\"/>\n" 
          + "</sample>", 
          "dateTime" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"P364D\"/>\n"
          + "  <element att=\"P1347Y\"/>\n"
          + "  <element att=\"-P1347M\"/>\n" 
          + "</sample>", 
          "duration" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"0FB70FB70FB70FB70FB70FB70FB70FB70" 
          + "FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70" 
          + "FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70" 
          + "FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70FB70" 
          + "FB7\"/>\n"
          + "  <element att=\"AFB7\"/>\n"
          + "  <element att=\"DF\"/>\n" 
          + "</sample>", 
          "hexBinary" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"x:a\"/>\n"
          + "  <element att=\"_b-c.32\"/>\n"
          + "  <element att=\"test\"/>\n" 
          + "</sample>", 
          "NMTOKEN" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"\n" 
          + "    AABBCCDDEEFFAABBCCDDEEFFAABBCCDDEEFFAABBCCDDEEFF\n" 
          + "    AABBCCDDEEFFAABBCCDDEEFFAABBCCDDEEFFAABBCCDDEEFF\n" 
          + "    AABBCCDDEEFFAABBCCDDEEFFAABBCCDDEEFFAABBCCDDEEFF\"/>\n"
          + "  <element att=\"AABBCCDDEEFFAA==\"/>\n"
          + "  <element att=\"AABBCCDDEEAAAAAA\"/>\n" 
          + "</sample>", 
          "base64Binary" 
        },
        {
          "<sample>\n" 
          + "  <element att=\"http://www.example.com\"/>\n"
          + "  <element att=\"test#a10\"/>\n"
          + "  <element att=\"ftp://server/path/to/file.xml\"/>\n" 
          + "</sample>", 
          "anyURI" 
        }
    };
  }

  @Test(dataProvider = "samples")
  public void testTypeInferenceForAttribute(String xmlSource, String type)
  throws SAXException, IOException {
    InferHandler handler = new InferHandler(new DatatypeLibraryFactoryImpl(new RegexEngineImpl()));
    SAXResolver resolver = new SAXResolver();
    XMLReader xr = resolver.createXMLReader();
    xr.setContentHandler(handler);
    xr.parse(new InputSource(new StringReader(xmlSource)));
    Schema schema = handler.getSchema();
    for (Map.Entry<Name, ElementDecl> entry : schema.getElementDecls()
        .entrySet()) {
      Name name = entry.getKey();
      ElementDecl elementDecl = entry.getValue();
      if ("element".equals(name.getLocalName())) {
        for (Map.Entry<Name, AttributeDecl> attEntry : elementDecl.getAttributeDecls().entrySet()) {
          AttributeDecl att = attEntry.getValue();
          Name attName = attEntry.getKey();
          if ("att".equals(attName.getLocalName())) {
            Name typeName = att.getDatatype();
            Assert.assertEquals(typeName.getLocalName(), type);
          }
        }
      }
    }
  }
}