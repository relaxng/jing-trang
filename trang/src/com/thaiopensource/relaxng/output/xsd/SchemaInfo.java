package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

class SchemaInfo {
  private final SchemaCollection sc;
  private GrammarPattern grammar;
  private final ErrorReporter er;
  private final Map childTypeMap = new HashMap();
  private final PatternVisitor childTypeVisitor = new ChildTypeVisitor();

  static class ChildType {
    private final String name;
    private ChildType(String name) {
      this.name = name;
    }

    public String toString() {
      return name;
    }

    private static final ChildType SIMPLE = new ChildType("SIMPLE");
    private static final ChildType EMPTY = new ChildType("EMPTY");
    private static final ChildType NOT_ALLOWED = new ChildType("NOT_ALLOWED");
    private static final ChildType COMPLEX = new ChildType("COMPLEX");
  }

  abstract class PatternAnalysisVisitor extends AbstractVisitor {
    abstract Object get(Pattern p);
    abstract Object combine(Object o1, Object o2);
    Object choice(Object o1, Object o2) {
      return combine(o1, o2);
    }
    Object group(Object o1, Object o2) {
      return combine(o1, o2);
    }
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
      return data();
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
      return ChildType.COMPLEX;
    }

    Object data() {
      return ChildType.SIMPLE;
    }

    Object notAllowed() {
      return ChildType.NOT_ALLOWED;
    }

    Object choice(Object o1, Object o2) {
      if (o1 == ChildType.NOT_ALLOWED)
        return o2;
      if (o2 == ChildType.NOT_ALLOWED)
        return o1;
      return combine(o1, o2);
    }

    Object group(Object o1, Object o2) {
      if (o1 == ChildType.NOT_ALLOWED || o2 == ChildType.NOT_ALLOWED)
        return ChildType.NOT_ALLOWED;
      return combine(o1, o2);
    }

    Object combine(Object o1, Object o2) {
      if (o1 == ChildType.EMPTY)
        return o2;
      if (o2 == ChildType.EMPTY)
        return o1;
      if (o1 == ChildType.SIMPLE && o2 == ChildType.SIMPLE)
        return o1;
      return ChildType.COMPLEX;
    }

    public Object visitElement(ElementPattern p) {
      return ChildType.COMPLEX;
    }

    public Object visitAttribute(AttributePattern p) {
      return ChildType.EMPTY;
    }
  }

  SchemaInfo(SchemaCollection sc, ErrorReporter er) {
    this.sc = sc;
    this.er = er;
    Pattern p = sc.getMainSchema();
    if (p instanceof GrammarPattern)
      grammar = (GrammarPattern)p;
    else {
      grammar = new GrammarPattern();
      grammar.setSourceLocation(p.getSourceLocation());
      DefineComponent dc = new DefineComponent(DefineComponent.START, p);
      dc.setSourceLocation(p.getSourceLocation());
      grammar.getComponents().add(dc);
    }
  }

  GrammarPattern getGrammar() {
    return grammar;
  }

  Pattern getSchema(String sourceUri) {
    return (Pattern)sc.getSchemas().get(sourceUri);
  }

  boolean isSimpleType(Pattern p) {
    return getChildType(p) == ChildType.SIMPLE;
  }

  private ChildType getChildType(Pattern p) {
    ChildType ct = (ChildType)childTypeMap.get(p);
    if (ct == null) {
      ct = (ChildType)p.accept(childTypeVisitor);
      childTypeMap.put(p, ct);
    }
    return ct;
  }

  Pattern getBody(RefPattern p) {
    return null;
  }
}