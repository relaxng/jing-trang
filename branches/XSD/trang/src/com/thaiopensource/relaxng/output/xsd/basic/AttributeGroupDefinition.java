package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Collections;

public class AttributeGroupDefinition extends Definition {
  private List attributeUses;

  public AttributeGroupDefinition(SourceLocation location, Schema parentSchema, String name, List attributeUses) {
    super(location, parentSchema, name);
    this.attributeUses = Collections.unmodifiableList(attributeUses);
  }

  public List getAttributeUses() {
    return attributeUses;
  }

  public void setAttributeUses(List attributeUses) {
    this.attributeUses = Collections.unmodifiableList(attributeUses);
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitAttributeGroup(this);
  }
}
