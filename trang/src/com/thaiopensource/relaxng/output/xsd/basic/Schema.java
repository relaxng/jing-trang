package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Vector;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

public class Schema extends Located {
  private final String uri;
  private Schema parent;
  private List topLevel = new Vector();
  private Map groupMap;
  private Map attributeGroupMap;
  private Map simpleTypeMap;

  public Schema(SourceLocation location, String uri) {
    super(location);
    this.uri = uri;
    this.groupMap = new HashMap();
    this.attributeGroupMap = new HashMap();
    this.simpleTypeMap = new HashMap();
  }

  private Schema(SourceLocation location, String uri, Schema parent) {
    super(location);
    this.parent = parent;
    this.uri = uri;
    this.groupMap = parent.groupMap;
    this.attributeGroupMap = parent.attributeGroupMap;
    this.simpleTypeMap = parent.simpleTypeMap;
  }

  public String getUri() {
    return uri;
  }

  public Schema getParent() {
    return parent;
  }

  public void defineGroup(String name, Particle particle, SourceLocation location) {
    GroupDefinition def = new GroupDefinition(location, this, name, particle);
    topLevel.add(def);
    groupMap.put(name, def);
  }

  public void defineAttributeGroup(String name, List attributeUses, SourceLocation location) {
    AttributeGroupDefinition def = new AttributeGroupDefinition(location, this, name, attributeUses);
    topLevel.add(def);
    attributeGroupMap.put(name, def);
  }

  public void defineSimpleType(String name, SimpleType simpleType, SourceLocation location) {
    SimpleTypeDefinition def = new SimpleTypeDefinition(location, this, name, simpleType);
    topLevel.add(def);
    simpleTypeMap.put(name, def);
  }

  public void addRoot(Particle particle, SourceLocation location) {
    topLevel.add(new RootDeclaration(location, this, particle));
  }

  public Schema addInclude(String uri, SourceLocation location) {
    Schema included = new Schema(location, uri, this);
    topLevel.add(new Include(location, this, included));
    return included;
  }

  public GroupDefinition getGroup(String name) {
    return (GroupDefinition)groupMap.get(name);
  }

  public SimpleTypeDefinition getSimpleType(String name) {
    return (SimpleTypeDefinition)simpleTypeMap.get(name);
  }

  public AttributeGroupDefinition getAttributeGroup(String name) {
    return (AttributeGroupDefinition)attributeGroupMap.get(name);
  }

  public void accept(SchemaVisitor visitor) {
    for (Iterator iter = topLevel.iterator(); iter.hasNext();)
      ((TopLevel)iter.next()).accept(visitor);
  }

  public Set subSchemas() {
    return null;
  }
}
