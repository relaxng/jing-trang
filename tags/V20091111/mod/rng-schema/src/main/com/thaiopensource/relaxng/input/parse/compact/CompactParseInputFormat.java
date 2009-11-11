package com.thaiopensource.relaxng.input.parse.compact;

import com.thaiopensource.relaxng.input.parse.ParseInputFormat;
import com.thaiopensource.relaxng.input.parse.ElementAnnotationBuilderImpl;
import com.thaiopensource.relaxng.input.parse.CommentListImpl;
import com.thaiopensource.relaxng.input.parse.AnnotationsImpl;
import com.thaiopensource.relaxng.parse.Parseable;
import com.thaiopensource.relaxng.parse.compact.CompactParseable;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.resolver.xml.sax.SAX;
import com.thaiopensource.resolver.xml.sax.SAXResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;

public class CompactParseInputFormat extends ParseInputFormat {
  public CompactParseInputFormat() {
    super(false);
  }

  public Parseable<Pattern, NameClass, SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl, AnnotationsImpl> makeParseable(InputSource inputSource, SAXResolver saxResolver, ErrorHandler eh) {
    return new CompactParseable<Pattern, NameClass, SourceLocation, ElementAnnotationBuilderImpl, CommentListImpl, AnnotationsImpl>(SAX.createInput(inputSource), saxResolver.getResolver(), eh);
  }
}
