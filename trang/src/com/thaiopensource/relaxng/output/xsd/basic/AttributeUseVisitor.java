package com.thaiopensource.relaxng.output.xsd.basic;

public interface AttributeUseVisitor {
  Object visitAttribute(Attribute a);
  Object visitAttributeGroupRef(AttributeGroupRef a);
}
