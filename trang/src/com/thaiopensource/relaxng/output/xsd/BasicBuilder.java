package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.output.xsd.basic.Occurs;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.Facet;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeList;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexType;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeSimpleContent;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.OptionalAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUseChoice;
import com.thaiopensource.relaxng.output.common.NameClassSplitter;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.OutputDirectory;

import java.util.List;
import java.util.Vector;
import java.util.Iterator;
import java.util.Collections;

public class BasicBuilder {
  private static final String XSD_URI = "http://www.w3.org/2001/XMLSchema-datatypes";
  private final PatternVisitor simpleTypeBuilder = new SimpleTypeBuilder();
  private final PatternVisitor attributeUseBuilder = new AttributeUseBuilder();
  private final PatternVisitor optionalAttributeUseBuilder = new OptionalAttributeUseBuilder();
  private final PatternVisitor particleBuilder = new ParticleBuilder();
  private final PatternVisitor occursCalculator = new OccursCalculator();
  private final ComponentVisitor schemaBuilder = new SchemaBuilder();
  private final ErrorReporter er;
  private final String inheritedNamespace;
  private final Schema schema;
  private final SchemaInfo si;


  /**
   * Preconditions for calling visit methods in this class are that the child type
   * - contains DATA
   * - does not contains ELEMENT
   * - does not contain TEXT
   */
  class SimpleTypeBuilder extends AbstractVisitor {
    public Object visitData(DataPattern p) {
      String library = p.getDatatypeLibrary();
      String type = p.getType();
      List facets = new Vector();
      if (!library.equals("") && !library.equals(XSD_URI)) {
        // TODO give an error
        type = "string";
      }
      else {
        for (Iterator iter = p.getParams().iterator(); iter.hasNext();) {
          Param param = (Param)iter.next();
          facets.add(new Facet(param.getSourceLocation(),
                               param.getName(),
                               param.getValue()));
        }
      }
      return new SimpleTypeRestriction(p.getSourceLocation(), type, facets);
    }

    public Object visitValue(ValuePattern p) {
      String library = p.getDatatypeLibrary();
      String type = p.getType();
      if (!library.equals("") && !library.equals(XSD_URI)) {
        // TODO give an error
        type = "string";
      }
      List facets = new Vector();
      SourceLocation location = p.getSourceLocation();
      facets.add(new Facet(location, "enumeration", p.getValue()));
      return new SimpleTypeRestriction(location, type, facets);
    }

    public Object visitComposite(CompositePattern p) {
      List result = new Vector();
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        Pattern child = (Pattern)iter.next();
        if (si.getChildType(child).contains(ChildType.DATA))
          result.add(child.accept(this));
      }
      if (result.size() == 1)
        return result.get(0);
      else
        return new SimpleTypeUnion(p.getSourceLocation(), result);
    }

    public Object visitUnary(UnaryPattern p) {
      return p.getChild().accept(this);
    }

    public Object visitList(ListPattern p) {
      SourceLocation location = p.getSourceLocation();
      Pattern child = p.getChild();
      if (si.getChildType(child).equals(ChildType.EMPTY))
        return makeEmptySimpleType(location);
      return new SimpleTypeList(location,
                                (SimpleType)child.accept(this),
                                (Occurs)child.accept(occursCalculator));
    }

    public Object visitRef(RefPattern p) {
      return new SimpleTypeRef(p.getSourceLocation(), p.getName());
    }
  }

  class OccursCalculator extends AbstractVisitor {
    public Object visitOptional(OptionalPattern p) {
      return new Occurs(0, ((Occurs)p.getChild().accept(this)).getMax());
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return new Occurs(0, Occurs.UNBOUNDED);
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return new Occurs(((Occurs)p.getChild().accept(this)).getMin(), Occurs.UNBOUNDED);
    }

    public Object visitData(DataPattern p) {
      return new Occurs(1, 1);
    }

    public Object visitValue(ValuePattern p) {
      return new Occurs(1, 1);
    }

    public Object visitEmpty(EmptyPattern p) {
      return new Occurs(0, 0);
    }

    private Occurs sum(CompositePattern p) {
      Occurs occ = new Occurs(0, 0);
      List children = p.getChildren();
      for (int i = 0, len = children.size(); i < len; i++)
        occ = Occurs.add(occ, (Occurs)((Pattern)children.get(i)).accept(this));
      return occ;
    }

    public Object visitInterleave(InterleavePattern p) {
      return sum(p);
    }

    public Object visitGroup(GroupPattern p) {
      return sum(p);
    }

    public Object visitChoice(ChoicePattern p) {
      List children = p.getChildren();
      Occurs occ = (Occurs)((Pattern)children.get(0)).accept(this);
      for (int i = 1, len = children.size(); i < len; i++) {
        Occurs tem = (Occurs)((Pattern)children.get(i)).accept(this);
        occ = new Occurs(Math.min(occ.getMin(), tem.getMin()),
                         Math.max(occ.getMax(), tem.getMax()));
      }
      return occ;
    }

    public Object visitRef(RefPattern p) {
      return si.getBody(p).accept(this);
    }
  }

  /**
   * Precondition for calling visit methods in this class is that the child type
   * contains ELEMENT.
   */
  class ParticleBuilder extends AbstractVisitor {
    public Object visitElement(ElementPattern p) {
      ComplexType type;
      Pattern child = p.getChild();
      ChildType ct = si.getChildType(child);
      AttributeUse attributeUses;
      if (ct.contains(ChildType.ATTRIBUTE))
        attributeUses = (AttributeUse)child.accept(attributeUseBuilder);
      else
        attributeUses = AttributeGroup.EMPTY;
      Particle particle = null;
      boolean mixed = false;
      if (ct.contains(ChildType.ELEMENT)) {
        if (ct.contains(ChildType.DATA))
          mixed = true;  // TODO give an error
        particle = (Particle)child.accept(particleBuilder);
      }
      if (ct.contains(ChildType.TEXT))
        mixed = true;
      if (ct.contains(ChildType.DATA) && !mixed && particle == null)
        type = new ComplexTypeSimpleContent(attributeUses,
                                            (SimpleType)child.accept(simpleTypeBuilder));
      else
        type = new ComplexTypeComplexContent(attributeUses, particle, mixed);
      List result = new Vector();
      for (Iterator iter = NameClassSplitter.split(p.getNameClass()).iterator(); iter.hasNext();)
        result.add(new Element(p.getSourceLocation(), makeName((NameNameClass)iter.next()), type));
      if (result.size() == 1)
        return result.get(0);
      return new ParticleChoice(p.getSourceLocation(), result);
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return new ParticleRepeat(p.getSourceLocation(),
                                (Particle)p.getChild().accept(this),
                                Occurs.ONE_OR_MORE);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return new ParticleRepeat(p.getSourceLocation(),
                                (Particle)p.getChild().accept(this),
                                Occurs.ZERO_OR_MORE);

    }

    public Object visitOptional(OptionalPattern p) {
      return new ParticleRepeat(p.getSourceLocation(),
                                (Particle)p.getChild().accept(this),
                                Occurs.OPTIONAL);
    }

    public Object visitChoice(ChoicePattern p) {
      List children = new Vector();
      boolean optional = false;
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        Pattern pattern = (Pattern)iter.next();
        ChildType ct = si.getChildType(pattern);
        if (ct.contains(ChildType.ELEMENT))
          children.add(pattern.accept(this));
        else if (!ct.equals(ChildType.NOT_ALLOWED))
          optional = true;
      }
      Particle result;
      if (children.size() == 1)
        result = (Particle)children.get(0);
      else
        result = new ParticleChoice(p.getSourceLocation(), children);
      if (optional)
        return new ParticleRepeat(p.getSourceLocation(), result, Occurs.OPTIONAL);
      return result;
    }

    public Object visitGroup(GroupPattern p) {
      List children = buildChildren(p);
      if (children.size() == 1)
        return children.get(0);
      else
        return new ParticleSequence(p.getSourceLocation(), children);
    }

    public Object visitInterleave(InterleavePattern p) {
      List children = buildChildren(p);
      if (children.size() == 1)
        return children.get(0);
      else
        return new ParticleAll(p.getSourceLocation(), children);
    }

    private List buildChildren(CompositePattern p) {
      List result = new Vector();
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        Pattern pattern = (Pattern)iter.next();
        if (si.getChildType(pattern).contains(ChildType.ELEMENT))
          result.add(pattern.accept(this));
      }
      return result;
    }

    public Object visitMixed(MixedPattern p) {
      return p.getChild().accept(this);
    }

    public Object visitRef(RefPattern p) {
      return new GroupRef(p.getSourceLocation(), p.getName());
    }
  }


  /**
   * Precondition for visitMethods is that the childType contains ATTRIBUTE
   */
  class OptionalAttributeUseBuilder extends AbstractVisitor {
    public Object visitAttribute(AttributePattern p) {
      SourceLocation location = p.getSourceLocation();
      Pattern child = p.getChild();
      ChildType ct = si.getChildType(child);
      SimpleType value;
      if (ct.contains(ChildType.DATA) && !ct.contains(ChildType.TEXT))
        value = (SimpleType)child.accept(simpleTypeBuilder);
      // TODO handle empty
      else
        value = null;
      List names = NameClassSplitter.split(p.getNameClass());
      List choices = new Vector();
      for (Iterator iter = names.iterator(); iter.hasNext();) {
        Attribute att = new Attribute(location,
                                      makeName(((NameNameClass)iter.next())),
                                      value);
        if (names.size() != 1 || isOptional())
          choices.add(new OptionalAttribute(att.getLocation(), att));
        else
          choices.add(att);
      }
      if (choices.size() == 1)
        return choices.get(0);
      return new AttributeGroup(p.getSourceLocation(), choices);
    }

    boolean isOptional() {
      return true;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return p.getChild().accept(this);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return p.getChild().accept(optionalAttributeUseBuilder);
    }

    public Object visitOptional(OptionalPattern p) {
      return p.getChild().accept(optionalAttributeUseBuilder);
    }

    public Object visitRef(RefPattern p) {
      AttributeUse ref = new AttributeGroupRef(p.getSourceLocation(), p.getName());
      if (!isOptional())
        return ref;
      List choices = new Vector();
      choices.add(ref);
      choices.add(AttributeGroup.EMPTY);
      return new AttributeUseChoice(p.getSourceLocation(), choices);
    }

    public Object visitComposite(CompositePattern p) {
      List uses = new Vector();
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        Pattern child = (Pattern)iter.next();
        if (si.getChildType(child).contains(ChildType.ATTRIBUTE))
          uses.add(child.accept(this));
      }
      if (uses.size() == 0)
        return AttributeGroup.EMPTY;
      if (uses.size() == 1)
        return uses.get(0);
      return new AttributeGroup(p.getSourceLocation(), uses);
    }

    public Object visitChoice(ChoicePattern p) {
      PatternVisitor childVisitor = this;
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        if (!si.getChildType((Pattern)iter.next()).contains(ChildType.ATTRIBUTE)) {
          childVisitor = optionalAttributeUseBuilder;
          break;
        }
      }
      List uses = new Vector();
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();) {
        Pattern child = (Pattern)iter.next();
        if (si.getChildType(child).contains(ChildType.ATTRIBUTE))
          uses.add((AttributeUse)child.accept(childVisitor));
      }
      if (uses.size() == 1)
        return uses.get(0);
      return new AttributeUseChoice(p.getSourceLocation(), uses);
    }
  }

  class AttributeUseBuilder extends OptionalAttributeUseBuilder {
    boolean isOptional() {
      return false;
    }
  }

  class SchemaBuilder extends AbstractVisitor {
    public Object visitDefine(DefineComponent c) {
      String name = c.getName();
      Pattern body = c.getBody();
      ChildType ct = si.getChildType(body);
      SourceLocation location = c.getSourceLocation();
      if (name == DefineComponent.START) {
        if (ct.contains(ChildType.ELEMENT))
          schema.addRoot((Particle)body.accept(particleBuilder),
                         location);
      }
      else {
        if (ct.contains(ChildType.ELEMENT))
          schema.defineGroup(name,
                             (Particle)body.accept(particleBuilder),
                             location);
        else if (ct.contains(ChildType.DATA) && !ct.contains(ChildType.TEXT))
          schema.defineSimpleType(name,
                                  (SimpleType)body.accept(simpleTypeBuilder),
                                  location);
        if (ct.contains(ChildType.ATTRIBUTE))
          schema.defineAttributeGroup(name,
                                      (AttributeUse)body.accept(attributeUseBuilder),
                                      location);
      }
      return null;
    }

    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      String uri = c.getHref();
      Schema sub = schema.addInclude(uri, c.getSourceLocation());
      si.getSchema(uri).componentsAccept(new BasicBuilder(er, si, sub, resolveNamespace(c.getNs())).schemaBuilder);
      return null;
    }
  }

  private BasicBuilder(ErrorReporter er, SchemaInfo si, Schema schema, String inheritedNamespace) {
    this.er = er;
    this.si = si;
    this.schema = schema;
    this.inheritedNamespace = inheritedNamespace;
  }

  static Schema buildBasicSchema(SchemaInfo si, ErrorReporter er) {
    GrammarPattern grammar = si.getGrammar();
    Schema schema = new Schema(grammar.getSourceLocation(), OutputDirectory.MAIN);
    grammar.componentsAccept(new BasicBuilder(er, si, schema, "").schemaBuilder);
    return schema;
  }

  private static SimpleType makeEmptySimpleType(SourceLocation location) {
    List facets = new Vector();
    facets.add(new Facet(location, "length", "0"));
    return new SimpleTypeRestriction(location, "token", facets);
  }

  private Name makeName(NameNameClass nc) {
    return new Name(resolveNamespace(nc.getNamespaceUri()), nc.getLocalName());
  }

  private String resolveNamespace(String ns) {
    return resolveNamespace(ns, inheritedNamespace);
  }

  private static String resolveNamespace(String ns, String inheritedNamespace) {
    if (ns == NameNameClass.INHERIT_NS)
      return inheritedNamespace;
    return ns;
  }

}
