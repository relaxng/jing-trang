package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.output.common.Name;

public abstract class SingleAttributeUse extends AttributeUse {
  public SingleAttributeUse(SourceLocation location) {
    super(location);
  }
  public abstract Name getName();
  public abstract SimpleType getType();
  public abstract boolean isOptional();
}
