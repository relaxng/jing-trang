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
import com.thaiopensource.relaxng.parse.nonxml.NonXmlParseable;
import com.thaiopensource.relaxng.util.DraconianErrorHandler;
import com.thaiopensource.relaxng.util.ValidationEngine;
import org.relaxng.datatype.helpers.DatatypeLibraryLoader;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
/*

Tasks:
Check single element type
Handle name class choice
Non-local namespaces
Include
combine
catch bad recursion
nested grammars
*/
public class DtdOutput {
  private ErrorHandler eh;
  private boolean hadError = false;

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
  // <empty/> not via a ref
  static Type DIRECT_EMPTY = new Type(EMPTY);
  static Type DIRECT_NOT_ALLOWED = new Type(NOT_ALLOWED);
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
    private final Grammar grammar;
    private Type startType = ERROR;

    Analyzer() {
      grammar = null;
    }

    Analyzer(Grammar grammar) {
      this.grammar = grammar;
    }

    public Object visitEmpty(EmptyPattern p) {
      return DIRECT_EMPTY;
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
      return DIRECT_NOT_ALLOWED;
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
      return checkType("sorry_ref", ref(analyzeType(this, def)), p);
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
      Analyzer a = new Analyzer(new Grammar(p));
      a.visitContainer(p);
      return a.startType;
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

  HashMap patternTypes = new HashMap();

  void analyze(Pattern p) {
    analyzeType(new Analyzer(), p);
    if (!hadError)
      System.err.println("OK");
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
    if (t1 == DIRECT_EMPTY)
      return t2;
    if (t2 == DIRECT_EMPTY)
      return t1;
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
    if (t1 == DIRECT_EMPTY)
      return t2;
    if (t2 == DIRECT_EMPTY)
      return t1;
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
    if (t == DIRECT_EMPTY)
      return DIRECT_EMPTY;
    if (t == NOT_ALLOWED)
      return MODEL_GROUP;
    return null;
  }


  private static Type choice(Type t1, Type t2) {
    if (t1 == ERROR || t2 == ERROR)
      return ERROR;
    if (t1 == DIRECT_EMPTY)
      return optional(t2);
    if (t1 == DIRECT_NOT_ALLOWED)
      return t2;
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
    if (t2 == DIRECT_EMPTY || t2 == DIRECT_NOT_ALLOWED || t2 == NOT_ALLOWED)
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
    if (t == DIRECT_EMPTY)
      return EMPTY;
    if (t == DIRECT_NOT_ALLOWED)
      return NOT_ALLOWED;
    if (t == DIRECT_SINGLE_ATTRIBUTE)
      return ATTRIBUTE_GROUP;
    if (t == DIRECT_SINGLE_ELEMENT)
      return ELEMENT_CLASS;
    if (t == COMPLEX_TYPE)
      return null;
    return t;
  }

  void error(String key, SourceLocation loc) {
    hadError = true;
    System.err.println(loc.getLineNumber() + ":" + key);
  }

  static public void output(Pattern p) {
    new DtdOutput(null).analyze(p);
  }

  static public void main(String[] args) throws IncorrectSchemaException, SAXException, IOException {
    SchemaCollection sc = new SchemaCollection();
    Pattern p = SchemaBuilderImpl.parse(new NonXmlParseable(ValidationEngine.fileInputSource(args[0]),
                                                            new DraconianErrorHandler()),
                                        sc,
                                        new DatatypeLibraryLoader());
    output(p);
  }
}
