package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.output.OutputDirectory;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

class DtdOutput {
  private String sourceUri;
  private Writer writer;
  private final String lineSep;
  StringBuffer buf = new StringBuffer();
  List elementQueue = new Vector();
  List requiredParamEntities = new Vector();
  List externallyRequiredParamEntities = new Vector();
  Set doneParamEntities = new HashSet();
  Set doneIncludes = new HashSet();
  Set pendingIncludes = new HashSet();
  private Analysis analysis;
  private GrammarPart part;
  private OutputDirectory od;
  private ErrorReporter er;

  PatternVisitor topLevelContentModelOutput = new TopLevelContentModelOutput();
  PatternVisitor nestedContentModelOutput = new ContentModelOutput();
  PatternVisitor innerElementClassOutput = new InnerElementClassOutput();
  AttributeOutput attributeOutput = new AttributeOutput();
  AttributeOutput optionalAttributeOutput = new OptionalAttributeOutput();
  PatternVisitor topLevelSimpleTypeOutput = new TopLevelSimpleTypeOutput();
  PatternVisitor nestedSimpleTypeOutput = new SimpleTypeOutput();
  GrammarOutput grammarOutput = new GrammarOutput();

  static private final String COMPATIBILITY_ANNOTATIONS = "http://relaxng.org/ns/compatibility/annotations/1.0";

  private DtdOutput(String sourceUri, Analysis analysis, OutputDirectory od, ErrorReporter er) {
    this.sourceUri = sourceUri;
    this.analysis = analysis;
    this.od = od;
    this.er = er;
    this.part = analysis.getGrammarPart(sourceUri);
    try {
      this.writer = od.open(sourceUri);
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
    this.lineSep = od.getLineSeparator();
  }

  class ContentModelOutput extends AbstractVisitor {
    public Object visitName(NameNameClass nc) {
      buf.append(nc.getLocalName());
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
      List list = nc.getChildren();
      boolean needSep = false;
      for (int i = 0, len = list.size(); i < len; i++) {
        if (needSep)
          buf.append('|');
        else
          needSep = true;
        ((NameClass)list.get(i)).accept(this);
      }
      return null;
    }

    public Object visitElement(ElementPattern p) {
      p.getNameClass().accept(this);
      elementQueue.add(p);
      return null;
    }

    public Object visitRef(RefPattern p) {
      Pattern def = getBody(p.getName());
      if (getContentType(def) == ContentType.DIRECT_SINGLE_ELEMENT)
        ((ElementPattern)def).getNameClass().accept(this);
      else
        paramEntityRef(p);
      return null;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      buf.append('(');
      p.getChild().accept(nestedContentModelOutput);
      buf.append(')');
      buf.append('*');
      return null;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      buf.append('(');
      p.getChild().accept(nestedContentModelOutput);
      buf.append(')');
      buf.append('+');
      return null;
    }

    public Object visitOptional(OptionalPattern p) {
      buf.append('(');
      p.getChild().accept(nestedContentModelOutput);
      buf.append(')');
      buf.append('?');
      return null;
    }

    public Object visitText(TextPattern p) {
      buf.append("#PCDATA");
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      buf.append("#PCDATA");
      return null;
    }

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        ContentType t = getContentType(member);
        if (!t.isA(ContentType.EMPTY)) {
          if (needSep)
            buf.append(',');
          else
            needSep = true;
          buf.append('(');
          member.accept(nestedContentModelOutput);
          buf.append(')');
        }
      }
      return null;
    }

    public Object visitInterleave(InterleavePattern p) {
      final List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        ContentType t = getContentType(member);
        if (!t.isA(ContentType.EMPTY))
          member.accept(this);
      }
      return null;
    }

    public Object visitChoice(ChoicePattern p) {
      List list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      if (getContentType(p).isA(ContentType.MIXED_ELEMENT_CLASS)) {
        for (int i = 0; i < len; i++) {
          Pattern member = (Pattern)list.get(i);
          if (getContentType(member).isA(ContentType.MIXED_ELEMENT_CLASS)) {
            member.accept(nestedContentModelOutput);
            needSep = true;
            break;
          }
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        ContentType t = getContentType(member);
        if (t != ContentType.NOT_ALLOWED && t != ContentType.EMPTY && !t.isA(ContentType.MIXED_ELEMENT_CLASS)) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          boolean needParen = !t.isA(ContentType.ELEMENT_CLASS);
          if (needParen)
            buf.append('(');
          member.accept(nestedContentModelOutput);
          if (needParen)
            buf.append(')');
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        ContentType t = getContentType(member);
        if (t == ContentType.NOT_ALLOWED) {
          if (needSep)
            buf.append(' ');
          else
            needSep = true;
          member.accept(nestedContentModelOutput);
        }
      }
      return null;
    }

    public Object visitGrammar(GrammarPattern p) {
      return getBody(DefineComponent.START).accept(this);
    }
  }

  class TopLevelContentModelOutput extends ContentModelOutput {
    public Object visitElement(ElementPattern p) {
      buf.append('(');
      super.visitElement(p);
      buf.append(')');
      return null;
    }

    public Object visitRef(RefPattern p) {
      ContentType t = getContentType(p);
      if (t == ContentType.MIXED_MODEL)
        super.visitRef(p);
      else {
        buf.append('(');
        super.visitRef(p);
        buf.append(')');
      }
      return null;
    }

    public Object visitChoice(ChoicePattern p) {
      buf.append('(');
      p.accept(nestedContentModelOutput);
      buf.append(')');
      return null;
    }

    public Object visitText(TextPattern p) {
      buf.append('(');
      p.accept(nestedContentModelOutput);
      buf.append(')');
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      buf.append('(');
      if (getContentType(p.getChild()) == ContentType.EMPTY)
        buf.append("#PCDATA)");
      else {
        buf.append("#PCDATA|");
        p.getChild().accept(innerElementClassOutput);
        buf.append(')');
        buf.append('*');
      }
      return null;
    }

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      Pattern main = null;
      for (int i = 0, len = list.size(); i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        if (!getContentType(member).isA(ContentType.EMPTY)) {
          if (main == null)
            main = member;
          else {
            buf.append('(');
            nestedContentModelOutput.visitGroup(p);
            buf.append(')');
            return null;
          }
        }
      }
      if (main != null)
        main.accept(this);
      return null;
    }
  }

  class InnerElementClassOutput extends AbstractVisitor {
    public Object visitRef(RefPattern p) {
      getBody(p.getName()).accept(this);
      return null;
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        if (getContentType(member) != ContentType.EMPTY) {
          member.accept(this);
          break;
        }
      }
      return null;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      p.getChild().accept(nestedContentModelOutput);
      return null;
    }
  }

  class AttributeOutput extends AbstractVisitor {
    void output(Pattern p) {
      if (getAttributeType(p) != AttributeType.EMPTY)
        p.accept(this);
    }

    void indent() {
      buf.append(lineSep);
      buf.append("  ");
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        output((Pattern)list.get(i));
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      output(p.getChild());
      return null;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      output(p.getChild());
      return null;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      if (getAttributeType(p) != AttributeType.SINGLE)
        er.warning("attribute_occur_approx", p.getSourceLocation());
      optionalAttributeOutput.output(p.getChild());
      return null;
    }

    public Object visitRef(RefPattern p) {
      ContentType t = getContentType(p);
      if (t.isA(ContentType.EMPTY) && isRequired()) {
        if (analysis.getParamEntityElementName(p.getName()) == null) {
          indent();
          paramEntityRef(p);
        }
      }
      else
        output(getBody(p.getName()));
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      indent();
      NameNameClass nnc = (NameNameClass)p.getNameClass();
      String ns = nnc.getNamespaceUri();
      String prefix = null;
      if (!ns.equals("") && ns != NameClass.INHERIT_NS) {
        prefix = analysis.getPrefixForNamespaceUri(ns);
        buf.append(prefix);
        buf.append(':');
      }
      buf.append(nnc.getLocalName());
      buf.append(" ");
      p.getChild().accept(topLevelSimpleTypeOutput);
      if (isRequired())
        buf.append(" #REQUIRED");
      else {
        String dv = getDefaultValue(p);
        if (dv == null)
          buf.append(" #IMPLIED");
        else {
          buf.append(' ');
          attributeValueLiteral(dv);
        }
      }
      if (prefix != null && !prefix.equals("xml")) {
        indent();
        buf.append("xmlns:");
        buf.append(prefix);
        buf.append(" CDATA #FIXED ");
        attributeValueLiteral(ns);
      }
      return null;
    }

    boolean isRequired() {
      return true;
    }

    public Object visitChoice(ChoicePattern p) {
      if (getAttributeType(p) != AttributeType.EMPTY)
        er.warning("attribute_occur_approx", p.getSourceLocation());
      // XXX may get duplicate attributes
      optionalAttributeOutput.visitComposite(p);
      return null;
    }

    public Object visitOptional(OptionalPattern p) {
      if (getAttributeType(p) != AttributeType.SINGLE)
        er.warning("attribute_occur_approx", p.getSourceLocation());
      optionalAttributeOutput.output(p.getChild());
      return null;
    }
  }

  class OptionalAttributeOutput extends AttributeOutput {
    boolean isRequired() {
      return false;
    }
  }

  class SimpleTypeOutput extends AbstractVisitor {
    public Object visitText(TextPattern p) {
      buf.append("CDATA");
      return null;
    }

    public Object visitValue(ValuePattern p) {
      buf.append(p.getValue());
      return null;
    }

    public Object visitRef(RefPattern p) {
      paramEntityRef(p);
      return null;
    }

    public Object visitData(DataPattern p) {
      String type = p.getType();
      if (p.getDatatypeLibrary().equals("")
          || type.equals("string"))
        type = "CDATA";
      buf.append(type);
      return null;
    }

    public Object visitChoice(ChoicePattern p) {
      List list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        ContentType t = getContentType(member);
        if (t != ContentType.NOT_ALLOWED) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          member.accept(this);
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        ContentType t = getContentType(member);
        if (t == ContentType.NOT_ALLOWED) {
          if (needSep)
            buf.append(' ');
          else
            needSep = true;
          member.accept(this);
        }
      }
      return null;
    }

  }

  class TopLevelSimpleTypeOutput extends SimpleTypeOutput {
    public Object visitValue(ValuePattern p) {
      buf.append('(');
      super.visitValue(p);
      buf.append(')');
      return null;
    }

    public Object visitChoice(ChoicePattern p) {
      if (getContentType(p) == ContentType.ENUM) {
        buf.append('(');
        nestedSimpleTypeOutput.visitChoice(p);
        buf.append(')');
      }
      else
        super.visitChoice(p);
      return null;
    }

    public Object visitRef(RefPattern p) {
      ContentType t = getContentType(p);
      if (t == ContentType.ENUM) {
        buf.append('(');
        super.visitRef(p);
        buf.append(')');
      }
      else if (t == ContentType.TEXT)
        buf.append("CDATA");
      else
        super.visitRef(p);
      return null;
    }

  }

  class GrammarOutput extends AbstractVisitor {
    public void visitContainer(Container c) {
      final List list = c.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Component)list.get(i)).accept(this);
    }

    public Object visitDiv(DivComponent c) {
      visitContainer(c);
      return null;
    }

    public Object visitDefine(DefineComponent c) {
      if (c.getName() == DefineComponent.START) {
        if (analysis.getPattern() == analysis.getGrammarPattern())
          c.getBody().accept(nestedContentModelOutput);
      }
      else {
        if (getContentType(c.getBody()) == ContentType.DIRECT_SINGLE_ELEMENT)
          outputElement((ElementPattern)c.getBody());
        else
          outputParamEntity(c.getName(), c.getBody());
      }
      outputQueuedElements();
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      outputInclude(c);
      return null;
    }
  }

  void outputQueuedElements() {
    for (int i = 0; i < elementQueue.size(); i++)
      outputElement((ElementPattern)elementQueue.get(i));
    elementQueue.clear();
  }

  static void output(Analysis analysis, OutputDirectory od, ErrorReporter er) throws IOException {
    try {
      new DtdOutput(od.MAIN, analysis, od, er).topLevelOutput();
    }
    catch (WrappedIOException e) {
      throw e.cause;
    }
  }

  void topLevelOutput() {
    GrammarPattern grammarPattern = analysis.getGrammarPattern();
    xmlDecl();
    Pattern p = analysis.getPattern();
    if (p != grammarPattern) {
      p.accept(nestedContentModelOutput);
      outputQueuedElements();
    }
    if (grammarPattern != null)
      grammarOutput.visitContainer(grammarPattern);
    close();
  }

  void subOutput(GrammarPattern grammarPattern) {
    xmlDecl();
    grammarOutput.visitContainer(grammarPattern);
    close();
  }

  void xmlDecl() {
    write("<?xml encoding=\"");
    write(od.getEncoding());
    write("\"?>");
    newline();
  }

  ContentType getContentType(Pattern p) {
    return analysis.getContentType(p);
  }

  AttributeType getAttributeType(Pattern p) {
    return analysis.getAttributeType(p);
  }

  Pattern getBody(String name) {
    return analysis.getBody(name);
  }

  void paramEntityRef(RefPattern p) {
    String name = p.getName();
    buf.append('%');
    buf.append(name);
    buf.append(';');
    requiredParamEntities.add(name);
  }

  void attributeValueLiteral(String value) {
    buf.append('\'');
    for (int i = 0, len = value.length(); i < len; i++) {
      char c = value.charAt(i);
      switch (c) {
      case '<':
        buf.append("&lt;");
        break;
      case '&':
        buf.append("&amp;");
        break;
      case '\'':
        buf.append("&apos;");
        break;
      case '"':
        buf.append("&quot;");
        break;
      default:
        buf.append(c);
        break;
      }
    }
    buf.append('\'');
  }

  void outputRequiredComponents() {
    for (int i = 0; i < requiredParamEntities.size(); i++) {
      String name = (String)requiredParamEntities.get(i);
      Component c = part.getWhereProvided(name);
      if (c == null)
        externallyRequiredParamEntities.add(name);
      else if (c instanceof DefineComponent)
        outputParamEntity(name, getBody(name));
      else
        outputInclude((IncludeComponent)c);
    }
    requiredParamEntities.clear();
  }

  void outputInclude(IncludeComponent inc) {
    String href = inc.getHref();
    if (doneIncludes.contains(href))
      return;
    if (pendingIncludes.contains(href)) {
      er.error("sorry_include_depend", inc.getSourceLocation());
      return;
    }
    pendingIncludes.add(href);
    DtdOutput sub = new DtdOutput(href, analysis, od, er);
    GrammarPattern g = (GrammarPattern)analysis.getSchema(href);
    sub.subOutput(g);
    requiredParamEntities.addAll(sub.externallyRequiredParamEntities);
    outputRequiredComponents();
    String entityName = genEntityName(href);
    newline();
    write("<!ENTITY % ");
    write(entityName);
    write(" SYSTEM ");
    write('"');
    // XXX deal with " in filename (is it allowed by URI syntax?)
    write(od.reference(sourceUri, href));
    write('"');
    write('>');
    newline();
    write('%');
    write(entityName);
    write(';');
    newline();
    doneIncludes.add(href);
    pendingIncludes.remove(href);
  }

  String genEntityName(String uri) {
    // XXX make this bulletproof and customizable
    int slash = uri.lastIndexOf('/');
    if (slash >= 0)
      uri = uri.substring(slash + 1);
    int dot = uri.lastIndexOf('.');
    if (dot > 0)
      uri = uri.substring(0, dot);
    return uri;
  }

  void outputParamEntity(String name, Pattern body) {
    if (doneParamEntities.contains(name))
      return;
    doneParamEntities.add(name);
    ContentType t = getContentType(body);
    buf.setLength(0);
    if (t.isA(ContentType.MODEL_GROUP) || t.isA(ContentType.NOT_ALLOWED) || t.isA(ContentType.MIXED_ELEMENT_CLASS))
      body.accept(nestedContentModelOutput);
    else if (t == ContentType.MIXED_MODEL)
      body.accept(topLevelContentModelOutput);
    else if (t.isA(ContentType.EMPTY))
      attributeOutput.output(body);
    else if (t.isA(ContentType.ENUM))
      body.accept(nestedSimpleTypeOutput);
    else if (t.isA(ContentType.SIMPLE_TYPE))
      body.accept(topLevelSimpleTypeOutput);
    String replacement = buf.toString();
    outputRequiredComponents();
    String elementName = analysis.getParamEntityElementName(name);
    if (elementName != null) {
      if (replacement.length() > 0) {
        newline();
        write("<!ATTLIST ");
        write(elementName);
        write(replacement);
        write('>');
        newline();
      }
    }
    else {
      newline();
      write("<!ENTITY % ");
      write(name);
      write(' ');
      write('"');
      write(replacement);
      write('"');
      write('>');
      newline();
    }
  }

  static class NameClassWalker extends AbstractVisitor {
     public Object visitChoice(ChoiceNameClass nc) {
        for (Iterator iter = nc.getChildren().iterator(); iter.hasNext();)
          ((NameClass)iter.next()).accept(this);
        return null;
      }
  }

  void outputElement(ElementPattern p) {
    buf.setLength(0);
    Pattern content = p.getChild();
    ContentType ct = getContentType(content);
    if (ct == ContentType.EMPTY)
      ;
    else if (ct == ContentType.MIXED_ELEMENT_CLASS) {
      er.warning("mixed_choice_approx", p.getSourceLocation());
      buf.append("(");
      content.accept(nestedContentModelOutput);
      buf.append(")*");
    }
    else if (ct.isA(ContentType.SIMPLE_TYPE)) {
      er.warning("data_content_approx", p.getSourceLocation());
      buf.append("(#PCDATA)");
    }
    else if (ct == ContentType.NOT_ALLOWED)
      return; // leave it undefined
    else
      content.accept(topLevelContentModelOutput);
    final String contentModel = buf.length() == 0 ? "EMPTY" : buf.toString();
    buf.setLength(0);
    attributeOutput.output(content);
    final String atts = buf.toString();
    outputRequiredComponents();
    final NameClass nc = p.getNameClass();
    nc.accept(new NameClassWalker() {
      public Object visitName(NameNameClass nc) {
        String ns = nc.getNamespaceUri();
        String name;
        String prefix;
        if (ns.equals("") || ns.equals(analysis.getDefaultNamespaceUri()) || ns == NameClass.INHERIT_NS) {
          name = nc.getLocalName();
          prefix = null;
        }
        else {
          prefix = analysis.getPrefixForNamespaceUri(ns);
          name = prefix + ":" + nc.getLocalName();
        }
        newline();
        write("<!ELEMENT ");
        write(name);
        write(' ');
        write(contentModel);
        write('>');
        newline();
        if (atts.length() != 0 || ns != NameClass.INHERIT_NS) {
          write("<!ATTLIST ");
          write(name);
          if (ns != NameClass.INHERIT_NS) {
            newline();
            write("  ");
            if (prefix != null) {
              write("xmlns:");
              write(prefix);
            }
            else
              write("xmlns");
            write(" CDATA #FIXED ");
            buf.setLength(0);
            attributeValueLiteral(ns);
            write(buf.toString());
          }
          write(atts);
          write('>');
          newline();
        }
        return null;
      }
    });
  }

  void newline() {
    write(lineSep);
  }

  static class WrappedIOException extends RuntimeException {
    IOException cause;

    WrappedIOException(IOException cause) {
      this.cause = cause;
    }

    Throwable getCause() {
      return cause;
    }
  }

  void write(String s) {
    try {
      writer.write(s);
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
  }

  void write(char c) {
    try {
      writer.write(c);
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
  }

  void close() {
    try {
      writer.close();
    }
    catch (IOException e) {
      throw new WrappedIOException(e);
    }
  }

  private static String getDefaultValue(AttributePattern p) {
    List list = p.getAttributeAnnotations();
    for (int i = 0, len = list.size(); i < len; i++) {
      AttributeAnnotation att = (AttributeAnnotation)list.get(i);
      if (att.getLocalName().equals("defaultValue")
          && att.getNamespaceUri().equals(COMPATIBILITY_ANNOTATIONS))
        return att.getValue();
    }
    return null;
  }
}
