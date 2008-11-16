package com.thaiopensource.relaxng.jaxp;

import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

/**
 * A SchemaFactory that supports RELAX NG Compact Syntax.
 */
public class CompactSyntaxSchemaFactory extends SchemaFactoryImpl {
  /**
   * The String that is used to identify the schema language, when the schema language is RELAX NG Compact Syntax.
   * Usually XML schema languages use an XML syntax, and the schema language can be identified by the namespace URI,
   * but this is not the case with RELAX NG Compact Syntax.  RELAX NG Compact syntax has a registered MIME type,
   * which is application/relax-ng-compact-syntax, so we use the URI corresponding to that.
   * @see javax.xml.validation.SchemaFactory#isSchemaLanguageSupported
   * @see javax.xml.validation.SchemaFactory#newInstance(String)
   */
  static final public String SCHEMA_LANGUAGE = "http://www.iana.org/assignments/media-types/application/relax-ng-compact-syntax";

  public boolean isSchemaLanguageSupported(String schemaLanguage) {
    return schemaLanguage.equals(SCHEMA_LANGUAGE);
  }

  protected Parseable createParseable(Source source, SAXResolver saxResolver, ErrorHandler eh) {
    InputSource inputSource = SAXSource.sourceToInputSource(source);
    if (inputSource == null)
      throw new IllegalArgumentException("unsupported type of Source for RELAX NG compact syntax schema");
    return new CompactParseable(SAX.createInput(inputSource), saxResolver.getResolver(), eh);
  }
}
