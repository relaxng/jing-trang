package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.xml.util.WellKnownNamespaces;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.xml.XMLConstants;

/**
 *
 */
public class XMLSyntaxSchemaFactoryTest extends SchemaFactoryImplTest {

  private static final String NS = XMLConstants.RELAXNG_NS_URI;

  public XMLSyntaxSchemaFactoryTest() {
    super(XMLSyntaxSchemaFactory.class);
  }

  @Test
  public void testIsSchemaLanguageSupported() {
    Assert.assertTrue(factory().isSchemaLanguageSupported(NS));
    Assert.assertTrue(factory().isSchemaLanguageSupported(XMLSyntaxSchemaFactory.SCHEMA_LANGUAGE));
    Assert.assertTrue(factory().isSchemaLanguageSupported(WellKnownNamespaces.RELAX_NG));
    Assert.assertFalse(factory().isSchemaLanguageSupported(CompactSyntaxSchemaFactory.SCHEMA_LANGUAGE));
  }

  protected String element(String name, String[] contentPatterns) {
    StringBuilder builder = new StringBuilder();
    builder.append("<element xmlns='" + NS + "' name='")
            .append(name)
            .append("'>");
    for (int i = 0; i < contentPatterns.length; i++)
      builder.append(contentPatterns[i]);
    if (contentPatterns.length == 0)
      builder.append("<empty/>");
    builder.append("</element>");
    return builder.toString();
  }

  protected String attribute(String name) {
    return "<attribute name='" + name + "'/>";
  }

  protected String externalRef(String uri) {
    return "<externalRef xmlns='" + NS + "' href='" + uri + "'/>";
  }

  protected String getLSType() {
    return NS;
  }
}
