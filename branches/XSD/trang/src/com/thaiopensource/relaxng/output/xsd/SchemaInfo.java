package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.output.common.ErrorReporter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class SchemaInfo {
  private final SchemaCollection sc;
  private GrammarPattern grammar;
  private final ErrorReporter er;
  private final Map childTypeMap = new HashMap();
  private final Map defineMap = new HashMap();
  private final PatternVisitor childTypeVisitor = new ChildTypeVisitor();

  abstract class PatternAnalysisVisitor extends AbstractVisitor {
    abstract Object get(Pattern p);
    abstract Object choice(Object o1, Object o2);
    abstract Object group(Object o1, Object o2);
    Object interleave(Object o1, Object o2) {
      return group(o1, o2);
    }
    Object ref(Object obj) {
      return obj;
    }
    Object oneOrMore(Object obj) {
      return group(obj, obj);
    }
    abstract Object empty();
    abstract Object text();
    abstract Object data();
    abstract Object notAllowed();
    Object list(Object obj) {
      return data();
    }

    public Object visitChoice(ChoicePattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = choice(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = group(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitInterleave(InterleavePattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = interleave(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return choice(empty(), oneOrMore(get(p.getChild())));
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return oneOrMore(get(p.getChild()));
    }

    public Object visitOptional(OptionalPattern p) {
      return choice(empty(), get(p.getChild()));
    }

    public Object visitEmpty(EmptyPattern p) {
      return empty();
    }

    public Object visitRef(RefPattern p) {
      return ref(get(getBody(p)));
    }

    public Object visitMixed(MixedPattern p) {
      return interleave(text(), get(p.getChild()));
    }

    public Object visitText(TextPattern p) {
      return text();
    }

    public Object visitData(DataPattern p) {
      return data();
    }

    public Object visitValue(ValuePattern p) {
      return data();
    }

    public Object visitList(ListPattern p) {
      return list(get(p.getChild()));
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return notAllowed();
    }
  }

  class ChildTypeVisitor extends PatternAnalysisVisitor {
    Object get(Pattern p) {
      return getChildType(p);
    }

    Object empty() {
      return ChildType.EMPTY;
    }

    Object text() {
      return ChildType.TEXT;
    }

    Object data() {
      return ChildType.DATA;
    }

    Object notAllowed() {
      return ChildType.NOT_ALLOWED;
    }

    Object list(Object obj) {
      if (obj.equals(ChildType.NOT_ALLOWED))
        return obj;
      return data();
    }

    Object choice(Object o1, Object o2) {
      return ChildType.choice((ChildType)o1, (ChildType)o2);
    }

    Object group(Object o1, Object o2) {
      return ChildType.group((ChildType)o1, (ChildType)o2);
    }

    public Object visitElement(ElementPattern p) {
      return ChildType.ELEMENT;
    }

    public Object visitAttribute(AttributePattern p) {
      if (getChildType(p.getChild()).equals(ChildType.NOT_ALLOWED))
        return ChildType.NOT_ALLOWED;
      return ChildType.ATTRIBUTE;
    }
  }

  class GrammarVisitor extends AbstractVisitor {
    public Object visitDefine(DefineComponent c) {
      if (c.getName() != DefineComponent.START)
        defineMap.put(c.getName(), c.getBody());
      return null;
    }

    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      getSchema(c.getHref()).componentsAccept(this);
      return null;
    }
  }


  static class NamespaceUsage {
    int elementCount;
    int attributeCount;
    static boolean isBetter(NamespaceUsage n1, NamespaceUsage n2) {
      return (n1.elementCount > n2.elementCount
              || (n1.elementCount == n2.elementCount
                  && n1.attributeCount > n2.attributeCount));
    }
  }

  static class PrefixUsage {
    int count;
  }



  SchemaInfo(SchemaCollection sc, ErrorReporter er) {
    this.sc = sc;
    this.er = er;
    forceGrammar();
    grammar = (GrammarPattern)sc.getMainSchema();
    grammar.componentsAccept(new GrammarVisitor());
  }

  void forceGrammar() {
    sc.setMainSchema(convertToGrammar(sc.getMainSchema()));
    // TODO convert other schemas
  }


  GrammarPattern convertToGrammar(Pattern p) {
    if (p instanceof GrammarPattern)
      return (GrammarPattern)p;
    GrammarPattern g = new GrammarPattern();
    g.setSourceLocation(p.getSourceLocation());
    g.setContext(p.getContext());
    DefineComponent dc = new DefineComponent(DefineComponent.START, p);
    dc.setSourceLocation(p.getSourceLocation());
    g.getComponents().add(dc);
    return g;
  }

  GrammarPattern getGrammar() {
    return grammar;
  }

  GrammarPattern getSchema(String sourceUri) {
    return (GrammarPattern)sc.getSchemas().get(sourceUri);
  }

  Set getSourceUris() {
    return sc.getSchemas().keySet();
  }

  ChildType getChildType(Pattern p) {
    ChildType ct = (ChildType)childTypeMap.get(p);
    if (ct == null) {
      ct = (ChildType)p.accept(childTypeVisitor);
      childTypeMap.put(p, ct);
    }
    return ct;
  }

  Pattern getBody(RefPattern p) {
    return (Pattern)defineMap.get(p.getName());
  }

}