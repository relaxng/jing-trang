package com.thaiopensource.relaxng.jaxp;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests CompactSyntaxSchemaFactory.
 */
public class CompactSyntaxSchemaFactoryTest extends SchemaFactoryImplTest {  
  public CompactSyntaxSchemaFactoryTest() {
    super(CompactSyntaxSchemaFactory.class);
  }

  @Test
  public void testIsSchemaLanguageSupported() {
    Assert.assertFalse(factory().isSchemaLanguageSupported(XMLSyntaxSchemaFactory.SCHEMA_LANGUAGE));
    Assert.assertTrue(factory().isSchemaLanguageSupported(CompactSyntaxSchemaFactory.SCHEMA_LANGUAGE));
  }

  protected String element(String name, String[] contentPatterns) {
    StringBuilder builder = new StringBuilder();
    builder.append("element ")
            .append(name)
            .append(" {");
    for (int i = 0; i < contentPatterns.length; i++) {
      if (i > 0)
        builder.append(", ");
      builder.append(contentPatterns[i]);
    }
    if (contentPatterns.length == 0)
      builder.append("empty");
    builder.append("}");
    return builder.toString();
  }

  protected String attribute(String name) {
    return "attribute " + name + " { text }";
  }

  protected String externalRef(String uri) {
    return "external \"" + uri + "\"";
  }

  protected String getLSType() {
    return CompactSyntaxSchemaFactory.SCHEMA_LANGUAGE;
  }
}
