package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class ParticleAll extends ParticleGroup {
  public ParticleAll(SourceLocation location, List children) {
    super(location, children);
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitAll(this);
  }

  public boolean equals(Object obj) {
    return obj instanceof ParticleAll && ((ParticleAll)obj).getChildren().equals(getChildren());
  }
}
