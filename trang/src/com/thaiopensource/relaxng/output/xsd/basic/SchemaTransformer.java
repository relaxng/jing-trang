package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;
import java.util.Vector;

public class SchemaTransformer implements SchemaVisitor, ParticleVisitor, ComplexTypeVisitor, AttributeUseVisitor, SimpleTypeVisitor {
  private final Schema schema;

  public SchemaTransformer(Schema schema) {
    this.schema = schema;
  }

  public Schema getSchema() {
    return schema;
  }

  public void transform() {
    schema.accept(this);
  }

  public void visitGroup(GroupDefinition def) {
    def.setParticle((Particle)def.getParticle().accept(this));
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    def.setAttributeUses(transformAttributeUseList(def.getAttributeUses()));
  }

  public void visitSimpleType(SimpleTypeDefinition def) {
    def.setSimpleType((SimpleType)def.getSimpleType().accept(this));
  }

  public void visitRoot(RootDeclaration decl) {
    decl.setParticle((Particle)decl.getParticle().accept(this));
  }

  public void visitInclude(Include include) {
    include.getIncludedSchema().accept(this);
  }

  public Object visitRepeat(ParticleRepeat p) {
    Particle child = (Particle)p.getChild().accept(this);
    if (child == p.getChild())
      return p;
    return new ParticleRepeat(p.getLocation(), child, p.getOccurs());
  }

  public Object visitGroupRef(GroupRef p) {
    return p;
  }

  public Object visitElement(Element p) {
    ComplexType ct = (ComplexType)p.getComplexType().accept(this);
    if (ct == p.getComplexType())
      return p;
    return new Element(p.getLocation(), p.getName(), ct);
  }

  public Object visitSequence(ParticleSequence p) {
    List children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    return new ParticleSequence(p.getLocation(), children);
  }

  public Object visitChoice(ParticleChoice p) {
    List children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    return new ParticleChoice(p.getLocation(), children);
  }

  public Object visitAll(ParticleAll p) {
    List children = transformParticleList(p.getChildren());
    if (children == p.getChildren())
      return p;
    return new ParticleAll(p.getLocation(), children);
  }

  public Object visitComplexContent(ComplexTypeComplexContent t) {
    Particle particle = t.getParticle();
    List attributeUses = transformAttributeUseList(t.getAttributeUses());
    if (particle != null)
      particle = (Particle)particle.accept(this);
    if (particle == t.getParticle() && attributeUses == t.getAttributeUses())
      return t;
    return new ComplexTypeComplexContent(attributeUses, particle, t.isMixed());
  }

  public Object visitSimpleContent(ComplexTypeSimpleContent t) {
    SimpleType simpleType = (SimpleType)t.getSimpleType().accept(this);
    List attributeUses = transformAttributeUseList(t.getAttributeUses());
    if (simpleType == t.getSimpleType() && attributeUses == t.getAttributeUses())
      return t;
    return new ComplexTypeSimpleContent(attributeUses, simpleType);
  }

  public Object visitAttribute(Attribute a) {
    SimpleType type = a.getType();
    if (type != null) {
      type = (SimpleType)type.accept(this);
      if (type == null || type != a.getType())
        return new Attribute(a.getLocation(), a.getName(), type);
    }
    return a;
  }

  public Object visitAttributeGroupRef(AttributeGroupRef a) {
    return a;
  }

  public Object visitOptionalAttribute(OptionalAttribute a) {
    Attribute attribute = (Attribute)a.getAttribute().accept(this);
    if (attribute == a.getAttribute())
      return a;
    return new OptionalAttribute(a.getLocation(), attribute);
  }

  public Object visitRestriction(SimpleTypeRestriction t) {
    return t;
  }

  public Object visitUnion(SimpleTypeUnion t) {
    List children = transformSimpleTypeList(t.getChildren());
    if (children == t.getChildren())
      return t;
    return new SimpleTypeUnion(t.getLocation(), children);
  }

  public Object visitList(SimpleTypeList t) {
    SimpleType itemType = (SimpleType)t.getItemType().accept(this);
    if (itemType == t.getItemType())
      return t;
    return new SimpleTypeList(t.getLocation(), itemType, t.getOccurs());
  }

  public Object visitRef(SimpleTypeRef t) {
    return t;
  }

  public List transformAttributeUseList(List list) {
    List transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      Object obj = ((AttributeUse)list.get(i)).accept(this);
      if (transformed != null)
        transformed.add(obj);
      else if (obj != list.get(i)) {
        transformed = new Vector();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        transformed.add(obj);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }

  public List transformParticleList(List list) {
    List transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      Object obj = ((Particle)list.get(i)).accept(this);
      if (transformed != null)
        transformed.add(obj);
      else if (obj != list.get(i)) {
        transformed = new Vector();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        transformed.add(obj);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }

  public List transformSimpleTypeList(List list) {
    List transformed = null;
    for (int i = 0, len = list.size(); i < len; i++) {
      Object obj = ((SimpleType)list.get(i)).accept(this);
      if (transformed != null)
        transformed.add(obj);
      else if (obj != list.get(i)) {
        transformed = new Vector();
        for (int j = 0; j < i; j++)
          transformed.add(list.get(j));
        transformed.add(obj);
      }
    }
    if (transformed == null)
      return list;
    return transformed;
  }
}
