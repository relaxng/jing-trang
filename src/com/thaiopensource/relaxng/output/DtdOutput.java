package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.IncorrectSchemaException;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
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
import com.thaiopensource.relaxng.edit.SchemaBuilderImpl;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.parse.nonxml.NonXmlParseable;
import com.thaiopensource.relaxng.util.DraconianErrorHandler;
import com.thaiopensource.relaxng.util.ValidationEngine;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.Iterator;
/*

Tasks:
Order for mixed
Order param entities
Check single element type
Handle name class choice
Non-local namespaces
Include
combine attribute/suppress definition corresponding to ATTLIST
catch bad recursion
nested grammars
a:defaultValue
a:documentation
non-deterministic content models
option to protect element declarations with included section
*/
public class DtdOutput {
  private ErrorHandler eh;
  private boolean hadError = false;
  private Grammar grammar = null;
  private GrammarPattern grammarPattern;
  private Type startType = ERROR;

  public DtdOutput(ErrorHandler eh) {
    this.eh = eh;
  }

  static class Type {
    private final Type parent1;
    private final Type parent2;
    private Type() {
      this.parent1 = null;
      this.parent2 = null;
    }

    public Type(Type parent1) {
      this.parent1 = parent1;
      this.parent2 = null;
    }

    public Type(Type parent1, Type parent2) {
      this.parent1 = parent1;
      this.parent2 = parent2;
    }

    boolean isA(Type t) {
      if (this == t)
        return true;
      if (parent1 != null && parent1.isA(t))
        return true;
      if (parent2 != null && parent2.isA(t))
        return true;
      return false;
    }
  }

  static Type COMPLEX_TYPE = new Type();
  static Type MIXED_ELEMENT_CLASS = new Type();
  static Type NOT_ALLOWED = new Type();
  static Type ATTRIBUTE_TYPE = new Type();
  static Type ATTRIBUTE_GROUP = new Type(COMPLEX_TYPE);
  static Type EMPTY = new Type(ATTRIBUTE_GROUP);
  static Type TEXT = new Type(MIXED_ELEMENT_CLASS, COMPLEX_TYPE);
  static Type DIRECT_TEXT = new Type(TEXT);
  static Type MODEL_GROUP = new Type(COMPLEX_TYPE);
  static Type ELEMENT_CLASS = new Type(MODEL_GROUP);
  static Type DIRECT_SINGLE_ELEMENT = new Type(ELEMENT_CLASS);
  static Type DIRECT_SINGLE_ATTRIBUTE = new Type(ATTRIBUTE_GROUP);
  static Type OPTIONAL_ATTRIBUTE = new Type(ATTRIBUTE_GROUP);
  static Type REPEAT_ELEMENT_CLASS = new Type(MODEL_GROUP);
  static Type ENUM = new Type(ATTRIBUTE_TYPE);
  static Type ERROR = new Type();

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

    public Object visitEmpty(EmptyPattern p) {
      return EMPTY;
    }

    public Object visitData(DataPattern p) {
      // XXX check datatypes
      return ATTRIBUTE_TYPE;
    }

    public Object visitValue(ValuePattern p) {
      // XXX check that NMTOKENS
      return ENUM;
    }

    public Object visitElement(ElementPattern p) {
      p.getNameClass().accept(this);
      if (!seen(p.getChild())) {
        Type t = analyzeType(this, p.getChild());
        if (!t.isA(COMPLEX_TYPE) && t != ERROR)
          error("bad_element_type", p.getSourceLocation());
      }
      return DIRECT_SINGLE_ELEMENT;
    }

    public Object visitAttribute(AttributePattern p) {
      p.getNameClass().accept(this);
      Type t = analyzeType(this, p.getChild());
      if (!t.isA(ATTRIBUTE_TYPE) && t != DIRECT_TEXT && t != ERROR)
        error("sorry_attribute_type", p.getSourceLocation());
      return DIRECT_SINGLE_ATTRIBUTE;
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return NOT_ALLOWED;
    }

    public Object visitText(TextPattern p) {
      return DIRECT_TEXT;
    }

    public Object visitList(ListPattern p) {
      error("sorry_list", p.getSourceLocation());
      return ATTRIBUTE_TYPE;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return checkType("sorry_one_or_more", repeat(analyzeType(this, p.getChild())), p);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return checkType("sorry_zero_or_more", repeat(analyzeType(this, p.getChild())), p);
    }

    public Object visitChoice(ChoicePattern p) {
      List children = p.getChildren();
      Type tem = analyzeType(this, (Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_choice", choice(tem, analyzeType(this, (Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitInterleave(InterleavePattern p) {
      List children = p.getChildren();
      Type tem = analyzeType(this, (Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_interleave", interleave(tem, analyzeType(this, (Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitGroup(GroupPattern p) {
      List children = p.getChildren();
      Type tem = analyzeType(this, (Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_group", group(tem, analyzeType(this, (Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitRef(RefPattern p) {
      Pattern def = grammar.getBody(p.getName());
      if (def == null) {
        error("undefined_ref", p.getSourceLocation());
        return ERROR;
      }
      return ref(analyzeType(this, def));
    }

    public Object visitParentRef(ParentRefPattern p) {
      error("sorry_parent_ref", p.getSourceLocation());
      return null;
    }

    public Object visitGrammar(GrammarPattern p) {
      if (grammar != null) {
        error("sorry_nested_grammar", p.getSourceLocation());
        return ERROR;
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
      return checkType("sorry_mixed", interleave(analyzeType(this, p.getChild()), DIRECT_TEXT), p);
    }

    public Object visitOptional(OptionalPattern p) {
      return checkType("sorry_optional", optional(analyzeType(this, p.getChild())), p);
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
      Type t = analyzeType(this, c.getBody());
      if (c.getName() == DefineComponent.START) {
        startType = t;
      }
      else {
        if (t == COMPLEX_TYPE)
          error("sorry_complex_type_define", c.getSourceLocation());
      }
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
      error("sorry_choice_name_class", nc.getSourceLocation());
      return null;
    }

    public Object visitAnyName(AnyNameNameClass nc) {
      error("sorry_wildcard", nc.getSourceLocation());
      return null;
    }

    public Object visitNsName(NsNameNameClass nc) {
      error("sorry_wildcard", nc.getSourceLocation());
      return null;
    }

    public Object visitName(NameNameClass nc) {
      String ns = nc.getNamespaceUri();
      if (ns != NameClass.INHERIT_NS && ns.length() != 0)
        error("sorry_namespace", nc.getSourceLocation());
      return null;
    }
  }

  StringBuffer buf = new StringBuffer();
  List elementQueue = new Vector();

  PatternVisitor topLevelContentModelOutput = new TopLevelContentModelOutput();
  PatternVisitor nestedContentModelOutput = new ContentModelOutput();
  PatternVisitor attributeOutput = new AttributeOutput();
  AttributeOutput optionalAttributeOutput = new OptionalAttributeOutput();
  PatternVisitor topLevelAttributeTypeOutput = new TopLevelAttributeTypeOutput();
  PatternVisitor nestedAttributeTypeOutput = new AttributeTypeOutput();
  GrammarOutput grammarOutput = new GrammarOutput();

  class ContentModelOutput extends AbstractVisitor {
    public Object visitElement(ElementPattern p) {
      buf.append(((NameNameClass)p.getNameClass()).getLocalName());
      elementQueue.add(p);
      return null;
    }

    public Object visitRef(RefPattern p) {
      Pattern def = grammar.getBody(p.getName());
      if (getType(def) == DIRECT_SINGLE_ELEMENT)
        buf.append(((NameNameClass)((ElementPattern)def).getNameClass()).getLocalName());
      else {
        buf.append('%');
        buf.append(p.getName());
        buf.append(';');
      }
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
        if (!t.isA(ATTRIBUTE_GROUP)) {
          if (needSep)
            buf.append(", ");
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
        if (!t.isA(ATTRIBUTE_GROUP))
          member.accept(this);
      }
      return null;
    }

    public Object visitChoice(ChoicePattern p) {
      // XXX do mixed first
      List list = p.getChildren();
      boolean needSep = false;
      final int len = list.size();
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        Type t = getType(member);
        if (t != NOT_ALLOWED) {
          if (needSep)
            buf.append('|');
          else
            needSep = true;
          // XXX need brackets unless a class
          member.accept(nestedContentModelOutput);
        }
      }
      for (int i = 0; i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        Type t = getType(member);
        if (t == NOT_ALLOWED) {
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
    public Object visitRef(RefPattern p) {
      buf.append('(');
      super.visitRef(p);
      buf.append(')');
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

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      Pattern main = null;
      for (int i = 0, len = list.size(); i < len; i++) {
        Pattern member = (Pattern)list.get(i);
        if (!getType(member).isA(ATTRIBUTE_GROUP)) {
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
      buf.append("\n  ");
    }

    public Object visitComposite(CompositePattern p) {
      List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Pattern)list.get(i)).accept(this);
      return null;
    }

    public Object visitRef(RefPattern p) {
      if (getType(p).isA(ATTRIBUTE_GROUP)) {
        indent();
        buf.append('%');
        buf.append(p.getName());
        buf.append(';');
      }
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      indent();
      buf.append(((NameNameClass)p.getNameClass()).getLocalName());
      buf.append(" ");
      p.getChild().accept(topLevelAttributeTypeOutput);
      buf.append(isRequired() ? " #REQUIRED" : " #IMPLIED");
      return null;
    }

    boolean isRequired() {
      return true;
    }

    public Object visitChoice(CompositePattern p) {
      if (getType(p) == OPTIONAL_ATTRIBUTE)
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
      buf.append('%');
      buf.append(p.getName());
      buf.append(';');
      return null;
    }

    public Object visitData(DataPattern p) {
      String type = p.getType();
      if (type.equals("string"))
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
        if (t != NOT_ALLOWED) {
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
        if (t == NOT_ALLOWED) {
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
      if (getType(p) == ENUM) {
        buf.append('(');
        nestedAttributeTypeOutput.visitChoice(p);
        buf.append(')');
      }
      else
        super.visitChoice(p);
      return null;
    }

    public Object visitRef(RefPattern p) {
      if (getType(p) == ENUM) {
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
        Pattern body = c.getBody();
        Type t = getType(body);
        if (t == DIRECT_SINGLE_ELEMENT) {
          // XXX deal with DIRECT_CHOICE
          outputElement((ElementPattern)body);
        }
        else {
          write("<!ENTITY % ");
          write(c.getName());
          write(' ');
          write('"');
          buf.setLength(0);
          if (t.isA(MODEL_GROUP) || t.isA(NOT_ALLOWED))
            body.accept(nestedContentModelOutput);
          else if (t.isA(ATTRIBUTE_GROUP))
            body.accept(attributeOutput);
          else if (t.isA(ENUM))
            body.accept(nestedAttributeTypeOutput);
          else if (t.isA(ATTRIBUTE_TYPE))
            body.accept(topLevelAttributeTypeOutput);
          write(buf.toString());
          write('"');
          write('>');
          newline();
        }
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
      if (hadEmpty) {
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

  HashMap patternTypes = new HashMap();

  void output(Pattern p) {
    p = (Pattern)p.accept(new Simplifier());
    analyzeType(new Analyzer(), p);
    if (!hadError) {
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

  HashMap seenTable = new HashMap();

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
    return ERROR;
  }

  Type getType(Pattern p) {
    return (Type)patternTypes.get(p);
  }



  void outputElement(ElementPattern p) {
    String name = ((NameNameClass)p.getNameClass()).getLocalName();
    buf.setLength(0);
    Pattern content = p.getChild();
    if (!getType(content).isA(ATTRIBUTE_GROUP))
     content.accept(topLevelContentModelOutput);
    write("<!ELEMENT ");
    write(name);
    write(' ');
    if (buf.length() == 0)
      write("EMPTY");
    else
      write(buf.toString());
    write('>');
    newline();
    buf.setLength(0);
    content.accept(attributeOutput);
    if (buf.length() != 0) {
      write("<!ATTLIST ");
      write(name);
      write(buf.toString());
      write('>');
      newline();
    }
  }

  void newline() {
    System.out.println();
  }

  void write(String s) {
    System.out.print(s);
  }

  void write(char c) {
    System.out.print(c);
  }

  private static Type repeat(Type t) {
    if (t == ERROR)
      return ERROR;
    if (t.isA(ELEMENT_CLASS))
      return REPEAT_ELEMENT_CLASS;
    if (t.isA(MIXED_ELEMENT_CLASS))
      return COMPLEX_TYPE;
    if (t.isA(MODEL_GROUP))
      return MODEL_GROUP;
    return null;
  }

  private static Type group(Type t1, Type t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1.isA(MODEL_GROUP) && t2.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if (t1.isA(EMPTY) && t2.isA(EMPTY))
      return EMPTY;
    if (t1.isA(ATTRIBUTE_GROUP) && t2.isA(ATTRIBUTE_GROUP))
      return ATTRIBUTE_GROUP;
    if ((t1.isA(COMPLEX_TYPE) && t2.isA(ATTRIBUTE_GROUP))
            || t2.isA(COMPLEX_TYPE) && t1.isA(ATTRIBUTE_GROUP))
      return COMPLEX_TYPE;
    return null;
  }

  private static Type interleave(Type t1, Type t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if ((t1 == DIRECT_TEXT && t2.isA(REPEAT_ELEMENT_CLASS))
            || (t2 == DIRECT_TEXT && t1.isA(REPEAT_ELEMENT_CLASS)))
      return COMPLEX_TYPE;
    if (t1.isA(EMPTY) && t2.isA(EMPTY))
      return EMPTY;
    if (t1.isA(ATTRIBUTE_GROUP) && t2.isA(ATTRIBUTE_GROUP))
      return ATTRIBUTE_GROUP;
    if ((t1.isA(COMPLEX_TYPE) && t2.isA(ATTRIBUTE_GROUP))
            || t2.isA(COMPLEX_TYPE) && t1.isA(ATTRIBUTE_GROUP))
      return COMPLEX_TYPE;
    return null;
  }

  private static Type optional(Type t) {
    if (t == ERROR)
      return ERROR;
    if (t == DIRECT_SINGLE_ATTRIBUTE)
      return OPTIONAL_ATTRIBUTE;
    if (t == OPTIONAL_ATTRIBUTE)
      return OPTIONAL_ATTRIBUTE;
    if (t.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if (t.isA(MIXED_ELEMENT_CLASS))
      return MIXED_ELEMENT_CLASS;
    if (t == NOT_ALLOWED)
      return MODEL_GROUP;
    return null;
  }


  private static Type choice(Type t1, Type t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1 == NOT_ALLOWED) {
      if (t2 == NOT_ALLOWED)
        return NOT_ALLOWED;
      if (t2.isA(ELEMENT_CLASS))
        return ELEMENT_CLASS;
      if (t2.isA(MIXED_ELEMENT_CLASS))
        return MIXED_ELEMENT_CLASS;
      if (t2.isA(MODEL_GROUP))
        return MODEL_GROUP;
      if (t2.isA(ENUM))
        return ENUM;
      return null;
    }
    if (t2 == NOT_ALLOWED)
      return choice(t2, t1);
    if (t1.isA(ELEMENT_CLASS) && t2.isA(ELEMENT_CLASS))
      return ELEMENT_CLASS;
    if (t1.isA(MODEL_GROUP) && t2.isA(MODEL_GROUP))
      return MODEL_GROUP;
    if ((t1.isA(MIXED_ELEMENT_CLASS) && t2.isA(ELEMENT_CLASS))
            || (t1.isA(ELEMENT_CLASS) && t2.isA(MIXED_ELEMENT_CLASS)))
      return MIXED_ELEMENT_CLASS;
    if (t1.isA(ENUM) && t2.isA(ENUM))
      return ENUM;
    return null;
  }

  private static Type ref(Type t) {
    if (t == DIRECT_TEXT)
      return TEXT;
    if (t == DIRECT_SINGLE_ATTRIBUTE)
      return ATTRIBUTE_GROUP;
    if (t == DIRECT_SINGLE_ELEMENT)
      return ELEMENT_CLASS;
    if (t == COMPLEX_TYPE)
      return ERROR;
    return t;
  }

  void error(String key, SourceLocation loc) {
    hadError = true;
    System.err.println(loc.getLineNumber() + ":" + key);
  }

  static public void main(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    SchemaCollection sc = new SchemaCollection();
    Pattern p = SchemaBuilderImpl.parse(new NonXmlParseable(ValidationEngine.fileInputSource(args[0]),
                                                            new DraconianErrorHandler()),
                                        sc,
                                        new DatatypeLibraryLoader());
    new DtdOutput(null).output(p);
  }
}
