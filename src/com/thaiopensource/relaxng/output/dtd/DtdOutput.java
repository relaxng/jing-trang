package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.AttributeAnnotation;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import org.xml.sax.ErrorHandler;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
/* Tasks:
Check for bad recursion
Check single element type
Warning when approximating datatypes
Avoid duplicate namespace declarations for global attributes
Option to generate namespace declarations only on start elements
Recognize #FIXED attributes
Include
Support duplicate definitions with combine="interleave" for attlists
Generate parameter entities to allow change of namespace prefix
nested grammars
a:documentation
Check for non-deterministic content models
option to protect element declarations with included section
allow mixed(repeat(NOT_ALLOWED))
Avoid unnecessary parentheses around group members
Pretty-print content models to avoid excessively long lines
Prettier formatting of ATTLISTs
Handle DTD compatibility ID/IDREF/IDREFS
Proper error reporting
Approximate data in content by #PCDATA
*/
class DtdOutput {
  private ErrorHandler eh;
  private Writer writer;
  private final String lineSep = System.getProperty("line.separator");
  private boolean hadError = false;
  private Grammar grammar = null;
  private GrammarPattern grammarPattern;
  private Type startType = Type.ERROR;
  StringBuffer buf = new StringBuffer();
  List elementQueue = new Vector();
  List requiredParamEntities = new Vector();
  Set doneParamEntitySet = new HashSet();
  Map patternTypes = new HashMap();
  private Map seenTable = new HashMap();
  private Map elementToAttlistMap = new HashMap();
  private Map paramEntityToElementMap = new HashMap();
  private String defaultNamespaceUri = null;
  // map namespace URIs to non-empty prefix
  private Map namespaceUriMap = new HashMap();

  PatternVisitor topLevelContentModelOutput = new TopLevelContentModelOutput();
  PatternVisitor nestedContentModelOutput = new ContentModelOutput();
  PatternVisitor attributeOutput = new AttributeOutput();
  AttributeOutput optionalAttributeOutput = new OptionalAttributeOutput();
  PatternVisitor topLevelAttributeTypeOutput = new TopLevelAttributeTypeOutput();
  PatternVisitor nestedAttributeTypeOutput = new AttributeTypeOutput();
  GrammarOutput grammarOutput = new GrammarOutput();

  static private final String XSD = "http://www.w3.org/2001/XMLSchema-datatypes";
  static private final String COMPATIBILITY_ANNOTATIONS = "http://relaxng.org/ns/compatibility/annotations/1.0";

  static final String[] compatibleTypes = {
    "ENTITIES",
    "ENTITY",
    "ID",
    "IDREF",
    "IDREFS",
    "NMTOKEN",
    "NMTOKENS"
  };

  static final String[] stringTypes = {
    "anyURI",
    "normalizedString",
    "base64Binary"
  };

  public DtdOutput(ErrorHandler eh, Writer writer) {
    this.eh = eh;
    this.writer = writer;
  }

  class Grammar implements ComponentVisitor {
    private HashMap defines = new HashMap();

    Grammar(GrammarPattern p) {
      visitContainer(p);
    }

    Pattern getBody(String name) {
      return (Pattern)defines.get(name);
    }

    public Object visitContainer(Container c) {
      List list = c.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Component)list.get(i)).accept(this);
      return null;
    }

    public Object visitDiv(DivComponent c) {
      return visitContainer(c);
    }

    public Object visitDefine(DefineComponent c) {
      if (defines.get(c.getName()) != null)
        error("sorry_multiple", c.getSourceLocation());
      else
        defines.put(c.getName(), c.getBody());
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      error("sorry_include", c.getSourceLocation());
      return null;
    }
  }

  class Analyzer implements PatternVisitor, ComponentVisitor, NameClassVisitor {
    private ElementPattern ancestorPattern;

    public Analyzer() {
    }

    public Analyzer(ElementPattern ancestorPattern) {
      this.ancestorPattern = ancestorPattern;
    }

    public Object visitEmpty(EmptyPattern p) {
      return Type.EMPTY;
    }

    public Object visitData(DataPattern p) {
      String lib = p.getDatatypeLibrary();
      if (lib.equals(XSD)) {
        String type = p.getType();
        for (int i = 0; i < compatibleTypes.length; i++)
          if (type.equals(compatibleTypes[i]))
            return Type.ATTRIBUTE_TYPE;
        for (int i = 0; i < stringTypes.length; i++)
          if (type.equals(stringTypes[i])) {
            p.setType("string");
            return Type.ATTRIBUTE_TYPE;
          }
        if (!type.equals("string"))
          p.setType("NMTOKEN");
      }
      else if (!lib.equals("")) {
        error("unrecognized_datatype_library", p.getSourceLocation());
        return Type.ERROR;
      }
      // XXX check datatypes
      return Type.ATTRIBUTE_TYPE;
    }

    public Object visitValue(ValuePattern p) {
      // XXX check that NMTOKENS
      return Type.ENUM;
    }

    public Object visitElement(ElementPattern p) {
      Object ret = p.getNameClass().accept(this);
      if (!seen(p.getChild())) {
        Type t = analyzeType(new Analyzer(p), p.getChild());
        if (!t.isA(Type.COMPLEX_TYPE) && t != Type.ERROR)
          error("bad_element_type", p.getSourceLocation());
      }
      return ret;
    }

    public Object visitAttribute(AttributePattern p) {
      if (p.getNameClass() instanceof NameNameClass) {
        NameNameClass nc = (NameNameClass)p.getNameClass();
        noteName(nc, false);
      }
      else
        error("sorry_attribute_name_class", p.getNameClass().getSourceLocation());
      Type t = analyzeType(this, p.getChild());
      if (!t.isA(Type.ATTRIBUTE_TYPE) && t != Type.DIRECT_TEXT && t != Type.ERROR)
        error("sorry_attribute_type", p.getSourceLocation());
      if (ancestorPattern != null)
        noteAttribute(ancestorPattern);
      return Type.DIRECT_SINGLE_ATTRIBUTE;
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return Type.NOT_ALLOWED;
    }

    public Object visitText(TextPattern p) {
      return Type.DIRECT_TEXT;
    }

    public Object visitList(ListPattern p) {
      error("sorry_list", p.getSourceLocation());
      return Type.ATTRIBUTE_TYPE;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return checkType("sorry_one_or_more", Type.oneOrMore(analyzeType(this, p.getChild())), p);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return checkType("sorry_zero_or_more", Type.zeroOrMore(analyzeType(this, p.getChild())), p);
    }

    public Object visitChoice(ChoicePattern p) {
      List children = p.getChildren();
      Type tem = analyzeType(this, (Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_choice", Type.choice(tem, analyzeType(this, (Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitInterleave(InterleavePattern p) {
      List children = p.getChildren();
      Type tem = analyzeType(this, (Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_interleave", Type.interleave(tem, analyzeType(this, (Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitGroup(GroupPattern p) {
      List children = p.getChildren();
      Type tem = analyzeType(this, (Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_group", Type.group(tem, analyzeType(this, (Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitRef(RefPattern p) {
      Pattern def = grammar.getBody(p.getName());
      if (def == null) {
        error("undefined_ref", p.getSourceLocation());
        return Type.ERROR;
      }
      Type t = Type.ref(analyzeType(new Analyzer(null), def));
      if (t.isA(Type.ATTRIBUTE_GROUP))
        noteAttributeGroupRef(ancestorPattern, p.getName());
      return Type.ref(t);
    }

    public Object visitParentRef(ParentRefPattern p) {
      error("sorry_parent_ref", p.getSourceLocation());
      return null;
    }

    public Object visitGrammar(GrammarPattern p) {
      if (grammar != null) {
        error("sorry_nested_grammar", p.getSourceLocation());
        return Type.ERROR;
      }
      grammar = new Grammar(p);
      grammarPattern = p;
      visitContainer(p);
      return startType;
    }

    public Object visitExternalRef(ExternalRefPattern p) {
      error("sorry_external_ref", p.getSourceLocation());
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      return checkType("sorry_mixed", Type.mixed(analyzeType(this, p.getChild())), p);
    }

    public Object visitOptional(OptionalPattern p) {
      return checkType("sorry_optional", Type.optional(analyzeType(this, p.getChild())), p);
    }

    public Object visitContainer(Container c) {
      List list = c.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Component)list.get(i)).accept(this);
      return null;
    }

    public Object visitDiv(DivComponent c) {
      return visitContainer(c);
    }

    public Object visitDefine(DefineComponent c) {
      if (c.getName() == DefineComponent.START) {
        startType = analyzeType(this, c.getBody());
      }
      else {
        Type t = analyzeType(new Analyzer(), c.getBody());
        if (t == Type.COMPLEX_TYPE || t == Type.COMPLEX_TYPE_MODEL_GROUP)
          error("sorry_complex_type_define", c.getSourceLocation());
      }
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
      List list = nc.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((NameClass)list.get(i)).accept(this);
      return Type.DIRECT_MULTI_ELEMENT;
    }

    public Object visitAnyName(AnyNameNameClass nc) {
      error("sorry_wildcard", nc.getSourceLocation());
      return Type.ERROR;
    }

    public Object visitNsName(NsNameNameClass nc) {
      error("sorry_wildcard", nc.getSourceLocation());
      return Type.ERROR;
    }

    public Object visitName(NameNameClass nc) {
      String ns = nc.getNamespaceUri();
      noteName(nc, true);
      return Type.DIRECT_SINGLE_ELEMENT;
    }
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
      Pattern def = grammar.getBody(p.getName());
      if (getType(def) == Type.DIRECT_SINGLE_ELEMENT)
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

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        Type t = getType(member);
        if (!t.isA(Type.ATTRIBUTE_GROUP)) {
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
        Type t = getType(member);
        if (!t.isA(Type.ATTRIBUTE_GROUP))
          member.accept(this);
      }
      return null;
    }

    public Object visitChoice(ChoicePattern p) {
      List list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      if (getType(p) == Type.MIXED_ELEMENT_CLASS) {
        for (int i = 0; i < len; i++) {
          Pattern member = (Pattern)list.get(i);
          if (getType(member).isA(Type.MIXED_ELEMENT_CLASS)) {
            member.accept(nestedContentModelOutput);
            needSep = true;
            break;
          }
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        Type t = getType(member);
        if (t != Type.NOT_ALLOWED && !t.isA(Type.MIXED_ELEMENT_CLASS)) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          boolean needParen = !t.isA(Type.ELEMENT_CLASS);
          if (needParen)
            buf.append('(');
          member.accept(nestedContentModelOutput);
          if (needParen)
            buf.append(')');
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        Type t = getType(member);
        if (t == Type.NOT_ALLOWED) {
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
      return grammar.getBody(DefineComponent.START).accept(this);
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
      if (getType(p) == Type.MIXED_MODEL)
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
      buf.append("#PCDATA|");
      ((ZeroOrMorePattern)p.getChild()).getChild().accept(nestedContentModelOutput);
      buf.append(')');
      buf.append('*');
      return null;
    }

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      Pattern main = null;
      for (int i = 0, len = list.size(); i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        if (!getType(member).isA(Type.ATTRIBUTE_GROUP)) {
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

  class AttributeOutput extends AbstractVisitor {
    void indent() {
      buf.append(lineSep);
      buf.append("  ");
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Pattern)list.get(i)).accept(this);
      return null;
    }

    public Object visitRef(RefPattern p) {
      if (getType(p).isA(Type.ATTRIBUTE_GROUP) && getParamEntityElementName(p.getName()) == null) {
        indent();
        paramEntityRef(p);
      }
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      indent();
      NameNameClass nnc = (NameNameClass)p.getNameClass();
      String ns = nnc.getNamespaceUri();
      String prefix = null;
      if (!ns.equals("") && ns != NameClass.INHERIT_NS) {
        prefix = (String)namespaceUriMap.get(ns);
        buf.append(prefix);
        buf.append(':');
      }
      buf.append(nnc.getLocalName());
      buf.append(" ");
      p.getChild().accept(topLevelAttributeTypeOutput);
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
      if (prefix != null) {
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

    public Object visitChoice(CompositePattern p) {
      if (getType(p) == Type.OPTIONAL_ATTRIBUTE)
        optionalAttributeOutput.visitComposite(p);
      else
        visitPattern(p);
      return null;
    }

    public Object visitOptional(OptionalPattern p) {
      return p.getChild().accept(optionalAttributeOutput);
    }
  }

  class OptionalAttributeOutput extends AttributeOutput {
    boolean isRequired() {
      return false;
    }
  }

  class AttributeTypeOutput extends AbstractVisitor {
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
        Type t = getType(member);
        if (t != Type.NOT_ALLOWED) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          member.accept(this);
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        Type t = getType(member);
        if (t == Type.NOT_ALLOWED) {
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

  class TopLevelAttributeTypeOutput extends AttributeTypeOutput {
    public Object visitValue(ValuePattern p) {
      buf.append('(');
      super.visitValue(p);
      buf.append(')');
      return null;
    }

    public Object visitChoice(ChoicePattern p) {
      if (getType(p) == Type.ENUM) {
        buf.append('(');
        nestedAttributeTypeOutput.visitChoice(p);
        buf.append(')');
      }
      else
        super.visitChoice(p);
      return null;
    }

    public Object visitRef(RefPattern p) {
      if (getType(p) == Type.ENUM) {
        buf.append('(');
        super.visitRef(p);
        buf.append(')');
      }
      else
        super.visitRef(p);
      return null;
    }

  }

  class GrammarOutput extends AbstractVisitor {
    boolean includeStart;

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
        if (includeStart)
          c.getBody().accept(nestedContentModelOutput);
      }
      else {
        if (getType(c.getBody()) == Type.DIRECT_SINGLE_ELEMENT)
          outputElement((ElementPattern)c.getBody());
        else
          outputParamEntity(c.getName(), c.getBody());
      }
      outputQueuedElements();
      return null;
    }
  }

  class Simplifier extends AbstractVisitor {
    public Object visitGrammar(GrammarPattern p) {
      return visitContainer(p);
    }

    public Object visitContainer(Container c) {
      List list = c.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Component)list.get(i)).accept(this);
      return c;
    }


    public Object visitInclude(IncludeComponent c) {
      return visitContainer(c);
    }

    public Object visitDiv(DivComponent c) {
      return visitContainer(c);
    }

    public Object visitDefine(DefineComponent c) {
      c.setBody((Pattern)c.getBody().accept(this));
      return c;
    }

    public Object visitChoice(ChoicePattern p) {
      boolean hadEmpty = false;
      List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        list.set(i, ((Pattern)list.get(i)).accept(this));
      for (Iterator iter = list.iterator(); iter.hasNext();) {
        Pattern child = (Pattern)iter.next();
        if (child instanceof NotAllowedPattern)
          iter.remove();
        else if (child instanceof EmptyPattern) {
          hadEmpty = true;
          iter.remove();
        }
      }
      if (list.size() == 0)
        return copy(new NotAllowedPattern(), p);
      Pattern tem;
      if (list.size() == 1)
        tem = (Pattern)list.get(0);
      else
        tem = p;
      if (hadEmpty && !(tem instanceof OptionalPattern) && !(tem instanceof ZeroOrMorePattern)) {
        if (tem instanceof OneOrMorePattern)
          tem = new ZeroOrMorePattern(((OneOrMorePattern)tem).getChild());
        else
          tem = new OptionalPattern(tem);
        copy(tem, p);
      }
      return tem;
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        list.set(i, ((Pattern)list.get(i)).accept(this));
      for (Iterator iter = list.iterator(); iter.hasNext();) {
        Pattern child = (Pattern)iter.next();
        if (child instanceof EmptyPattern)
          iter.remove();
      }
      if (list.size() == 0)
        return copy(new EmptyPattern(), p);
      if (list.size() == 1)
        return (Pattern)p.getChildren().get(0);
      return p;
    }


    public Object visitInterleave(InterleavePattern p) {
      boolean hadText = false;
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        Pattern child = (Pattern)iter.next();
        if (child instanceof TextPattern) {
          iter.remove();
          hadText = true;
        }
      }
      if (!hadText)
        return visitComposite(p);
      return copy(new MixedPattern((Pattern)visitComposite(p)), p);
    }

    public Object visitUnary(UnaryPattern p) {
      p.setChild((Pattern)p.getChild().accept(this));
      return p;
    }

    private Annotated copy(Annotated to, Annotated from) {
      to.setSourceLocation(from.getSourceLocation());
      return to;
    }

    public Object visitPattern(Pattern p) {
      return p;
    }
  }

  void outputQueuedElements() {
    for (int i = 0; i < elementQueue.size(); i++)
      outputElement((ElementPattern)elementQueue.get(i));
    elementQueue.clear();
  }

  void output(Pattern p) throws IOException {
    try {
      p = (Pattern)p.accept(new Simplifier());
      analyzeType(new Analyzer(), p);
      if (!hadError) {
        assignPrefixes();
        if (p == grammarPattern)
          grammarOutput.includeStart = true;
        else {
          p.accept(nestedContentModelOutput);
          outputQueuedElements();
          grammarOutput.includeStart = false;
        }
        if (grammarPattern != null)
          grammarOutput.visitContainer(grammarPattern);
      }
    }
    catch (WrappedIOException e) {
      throw e.cause;
    }
    writer.flush();
  }

  boolean seen(Pattern p) {
    if (seenTable.get(p) != null)
      return true;
    seenTable.put(p, p);
    return false;
  }

  Type analyzeType(Analyzer a, Pattern p) {
    Type t = (Type)patternTypes.get(p);
    if (t == null) {
      t = (Type)p.accept(a);
      patternTypes.put(p, t);
    }
    return t;
  }

  Type checkType(String key, Type t, Pattern p) {
    if (t != null)
      return t;
    error(key, p.getSourceLocation());
    return Type.ERROR;
  }

  Type getType(Pattern p) {
    return (Type)patternTypes.get(p);
  }

  void noteAttribute(ElementPattern e) {
    elementToAttlistMap.put(e, Boolean.FALSE);
  }

  void noteAttributeGroupRef(ElementPattern e, String paramEntityName) {
    if (e != null) {
      if (elementToAttlistMap.get(e) != null)
        elementToAttlistMap.put(e, Boolean.FALSE);
      else
        elementToAttlistMap.put(e, paramEntityName);
    }
    if (e == null || paramEntityToElementMap.get(paramEntityName) != null)
      paramEntityToElementMap.put(paramEntityName, Boolean.FALSE);
    else
      paramEntityToElementMap.put(paramEntityName, e);
  }

  String getParamEntityElementName(String name) {
    Object elem = paramEntityToElementMap.get(name);
    if (elem == null || elem == Boolean.FALSE)
      return null;
    Object tem = elementToAttlistMap.get(elem);
    if (!name.equals(tem))
      return null;
    NameClass nc = ((ElementPattern)elem).getNameClass();
    if (!(nc instanceof NameNameClass))
      return null;
    return ((NameNameClass)nc).getLocalName();
  }

  private Set usedPrefixes = new HashSet();
  private Set unassignedNamespaceUris = new HashSet();

  void noteName(NameNameClass nc, boolean defaultable) {
    String ns = nc.getNamespaceUri();
    if (ns.equals("") || ns == NameClass.INHERIT_NS) {
      if (defaultable)
        defaultNamespaceUri = "";
      return;
    }
    String assignedPrefix = (String)namespaceUriMap.get(ns);
    if (assignedPrefix != null)
      return;
    String prefix = nc.getPrefix();
    if (prefix == null) {
      if (defaultNamespaceUri == null && defaultable)
        defaultNamespaceUri = ns;
      unassignedNamespaceUris.add(ns);
    }
    else {
      if (usedPrefixes.contains(prefix))
        unassignedNamespaceUris.add(ns);
      else {
        usedPrefixes.add(prefix);
        namespaceUriMap.put(ns, prefix);
        unassignedNamespaceUris.remove(ns);
      }
    }
  }

  void assignPrefixes() {
    if (defaultNamespaceUri == null)
      defaultNamespaceUri = "";
    int n = 0;
    for (Iterator iter = unassignedNamespaceUris.iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      for (;;) {
        ++n;
        String prefix = "ns" + Integer.toString(n);
        if (!usedPrefixes.contains(prefix)) {
          namespaceUriMap.put(ns, prefix);
          break;
        }
      }
    }
  }

  void paramEntityRef(RefPattern p) {
    String name = p.getName();
    buf.append('%');
    buf.append(name);
    buf.append(';');
    if (!doneParamEntitySet.contains(name))
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

  void outputRequiredParamEntities() {
    for (int i = 0; i < requiredParamEntities.size(); i++) {
      String name = (String)requiredParamEntities.get(i);
      outputParamEntity(name, grammar.getBody(name));
    }
    requiredParamEntities.clear();
  }

  void outputParamEntity(String name, Pattern body) {
    if (doneParamEntitySet.contains(name))
      return;
    doneParamEntitySet.add(name);
    Type t = getType(body);
    buf.setLength(0);
    if (t.isA(Type.MODEL_GROUP) || t.isA(Type.NOT_ALLOWED) || t.isA(Type.MIXED_ELEMENT_CLASS))
      body.accept(nestedContentModelOutput);
    else if (t == Type.MIXED_MODEL)
      body.accept(topLevelContentModelOutput);
    else if (t.isA(Type.ATTRIBUTE_GROUP))
      body.accept(attributeOutput);
    else if (t.isA(Type.ENUM))
      body.accept(nestedAttributeTypeOutput);
    else if (t.isA(Type.ATTRIBUTE_TYPE))
      body.accept(topLevelAttributeTypeOutput);
    String replacement = buf.toString();
    outputRequiredParamEntities();
    String elementName = getParamEntityElementName(name);
    if (elementName != null) {
      write("<!ATTLIST ");
      write(elementName);
      write(replacement);
      write('>');
    }
    else {
      write("<!ENTITY % ");
      write(name);
      write(' ');
      write('"');
      write(replacement);
      write('"');
      write('>');
    }
    newline();
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
    if (!getType(content).isA(Type.ATTRIBUTE_GROUP))
     content.accept(topLevelContentModelOutput);
    final String contentModel = buf.length() == 0 ? "EMPTY" : buf.toString();
    buf.setLength(0);
    content.accept(attributeOutput);
    final String atts = buf.toString();
    outputRequiredParamEntities();
    final NameClass nc = p.getNameClass();
    nc.accept(new NameClassWalker() {
      public Object visitName(NameNameClass nc) {
        String ns = nc.getNamespaceUri();
        String name;
        String prefix;
        if (ns.equals("") || ns.equals(defaultNamespaceUri) || ns == NameClass.INHERIT_NS) {
          name = nc.getLocalName();
          prefix = null;
        }
        else {
          prefix = (String)namespaceUriMap.get(ns);
          name = prefix + ":" + nc.getLocalName();
        }
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


  void error(String key, SourceLocation loc) {
    hadError = true;
    System.err.println(loc.getLineNumber() + ":" + key);
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
