package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class RootDeclaration extends TopLevel {
  private final Particle particle;

  public RootDeclaration(SourceLocation location, Schema parentSchema, Particle particle) {
    super(location, parentSchema);
    this.particle = particle;
  }

  public Particle getParticle() {
    return particle;
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitRoot(this);
  }
}
