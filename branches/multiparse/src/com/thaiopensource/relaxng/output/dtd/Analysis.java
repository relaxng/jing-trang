package com.thaiopensource.relaxng.output.dtd;

import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.output.OutputDirectory;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

class Analysis {
  private NamespaceManager nsm = new NamespaceManager();
  private AttlistMapper am = new AttlistMapper();
  private ErrorReporter er;
  private Map patternTypes = new HashMap();
  private Map defines = null;
  private Map parts = new HashMap();
  private Map seenTable = new HashMap();
  private Type startType = Type.ERROR;
  private GrammarPart mainPart;
  private SchemaCollection schemas;
  private GrammarPattern grammarPattern;
  private Pattern pattern;

  static private final String XSD = "http://www.w3.org/2001/XMLSchema-datatypes";

  static private final String[] compatibleTypes = {
    "ENTITIES",
    "ENTITY",
    "ID",
    "IDREF",
    "IDREFS",
    "NMTOKEN",
    "NMTOKENS"
  };

  static private final String[] stringTypes = {
    "anyURI",
    "normalizedString",
    "base64Binary"
  };

  private class Analyzer implements PatternVisitor, ComponentVisitor, NameClassVisitor {
    private ElementPattern ancestorPattern;
    private Set pendingRefs;

    public Analyzer() {
      pendingRefs = new HashSet();
    }

    private Analyzer(ElementPattern ancestorPattern) {
      this.ancestorPattern = ancestorPattern;
      pendingRefs = new HashSet();
    }

    private Analyzer(Set pendingRefs) {
      this.pendingRefs = pendingRefs;
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
        er.error("unrecognized_datatype_library", p.getSourceLocation());
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
        Type t = new Analyzer(p).analyzeType(p.getChild());
        if (!t.isA(Type.COMPLEX_TYPE) && t != Type.ERROR)
          er.error("bad_element_type", p.getSourceLocation());
      }
      return ret;
    }

    public Object visitAttribute(AttributePattern p) {
      if (p.getNameClass() instanceof NameNameClass) {
        NameNameClass nc = (NameNameClass)p.getNameClass();
        nsm.noteName(nc, false);
      }
      else
        er.error("sorry_attribute_name_class", p.getNameClass().getSourceLocation());
      Type t = analyzeType(p.getChild());
      if (!t.isA(Type.ATTRIBUTE_TYPE) && t != Type.DIRECT_TEXT && t != Type.ERROR)
        er.error("sorry_attribute_type", p.getSourceLocation());
      if (ancestorPattern != null)
        am.noteAttribute(ancestorPattern);
      return Type.DIRECT_SINGLE_ATTRIBUTE;
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return Type.NOT_ALLOWED;
    }

    public Object visitText(TextPattern p) {
      return Type.DIRECT_TEXT;
    }

    public Object visitList(ListPattern p) {
      er.error("sorry_list", p.getSourceLocation());
      return Type.ATTRIBUTE_TYPE;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return checkType("sorry_one_or_more", Type.oneOrMore(analyzeType(p.getChild())), p);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return checkType("sorry_zero_or_more", Type.zeroOrMore(analyzeType(p.getChild())), p);
    }

    public Object visitChoice(ChoicePattern p) {
      List children = p.getChildren();
      Type tem = analyzeType((Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_choice", Type.choice(tem, analyzeType((Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitInterleave(InterleavePattern p) {
      List children = p.getChildren();
      Type tem = analyzeType((Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_interleave", Type.interleave(tem, analyzeType((Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitGroup(GroupPattern p) {
      List children = p.getChildren();
      Type tem = analyzeType((Pattern)children.get(0));
      for (int i = 1, len = children.size(); i < len; i++)
        tem = checkType("sorry_group", Type.group(tem, analyzeType((Pattern)children.get(i))), p);
      return tem;
    }

    public Object visitRef(RefPattern p) {
      String name = p.getName();
      Pattern def = getBody(name);
      if (def == null) {
        er.error("undefined_ref", p.getSourceLocation());
        return Type.ERROR;
      }
      if (pendingRefs.contains(name)) {
        er.error("ref_loop", p.getSourceLocation());
        return Type.ERROR;
      }
      pendingRefs.add(name);
      Type t = Type.ref(new Analyzer(pendingRefs).analyzeType(def));
      pendingRefs.remove(name);
      if (t.isA(Type.ATTRIBUTE_GROUP))
        am.noteAttributeGroupRef(ancestorPattern, p.getName());
      return Type.ref(t);
    }

    public Object visitParentRef(ParentRefPattern p) {
      er.error("sorry_parent_ref", p.getSourceLocation());
      return null;
    }

    public Object visitGrammar(GrammarPattern p) {
      if (defines != null) {
        er.error("sorry_nested_grammar", p.getSourceLocation());
        return Type.ERROR;
      }
      defines = new HashMap();
      try {
        mainPart = new GrammarPart(er, defines, schemas, parts, p);
      }
      catch (GrammarPart.IncludeLoopException e) {
        er.error("include_loop", e.getInclude().getSourceLocation());
        return Type.ERROR;
      }
      grammarPattern = p;
      visitContainer(p);
      return startType;
    }

    public Object visitExternalRef(ExternalRefPattern p) {
      er.error("sorry_external_ref", p.getSourceLocation());
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      return checkType("sorry_mixed", Type.mixed(analyzeType(p.getChild())), p);
    }

    public Object visitOptional(OptionalPattern p) {
      return checkType("sorry_optional", Type.optional(analyzeType(p.getChild())), p);
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
        startType = analyzeType(c.getBody());
      }
      else {
        Type t = new Analyzer().analyzeType(c.getBody());
        if (t == Type.COMPLEX_TYPE || t == Type.COMPLEX_TYPE_MODEL_GROUP)
          er.error("sorry_complex_type_define", c.getSourceLocation());
      }
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      visitContainer((GrammarPattern)schemas.getSchemas().get(c.getHref()));
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
      List list = nc.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((NameClass)list.get(i)).accept(this);
      return Type.DIRECT_MULTI_ELEMENT;
    }

    public Object visitAnyName(AnyNameNameClass nc) {
      er.error("sorry_wildcard", nc.getSourceLocation());
      return Type.ERROR;
    }

    public Object visitNsName(NsNameNameClass nc) {
      er.error("sorry_wildcard", nc.getSourceLocation());
      return Type.ERROR;
    }

    public Object visitName(NameNameClass nc) {
      String ns = nc.getNamespaceUri();
      nsm.noteName(nc, true);
      return Type.DIRECT_SINGLE_ELEMENT;
    }

    Type checkType(String key, Type t, Pattern p) {
      if (t != null)
        return t;
      er.error(key, p.getSourceLocation());
      return Type.ERROR;
    }

    Type analyzeType(Pattern p) {
      Type t = (Type)patternTypes.get(p);
      if (t == null) {
        t = (Type)p.accept(this);
        patternTypes.put(p, t);
      }
      return t;
    }

  }

  private boolean seen(Pattern p) {
    if (seenTable.get(p) != null)
      return true;
    seenTable.put(p, p);
    return false;
  }

  Analysis(SchemaCollection schemas, ErrorReporter er) {
    this.schemas = schemas;
    this.er = er;
    new Analyzer().analyzeType(schemas.getMainSchema());
    if (!er.hadError)
      nsm.assignPrefixes();
  }

  Pattern getPattern() {
    return schemas.getMainSchema();
  }

  String getPrefixForNamespaceUri(String ns) {
    return nsm.getPrefixForNamespaceUri(ns);
  }

  String getDefaultNamespaceUri() {
    return nsm.getDefaultNamespaceUri();
  }

  String getParamEntityElementName(String name) {
    return am.getParamEntityElementName(name);
  }

  Type getType(Pattern p) {
    return (Type)patternTypes.get(p);
  }

  Pattern getBody(String name) {
    return (Pattern)defines.get(name);
  }

  GrammarPattern getGrammarPattern() {
    return grammarPattern;
  }

  GrammarPart getGrammarPart(String sourceUri) {
    if (sourceUri == OutputDirectory.MAIN)
      return mainPart;
    else
      return (GrammarPart)parts.get(sourceUri);
  }

  Pattern getSchema(String sourceUri) {
    return (Pattern)schemas.getSchemas().get(sourceUri);
  }
}
