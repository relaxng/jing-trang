package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class RootDeclaration extends TopLevel {
  private Particle particle;

  RootDeclaration(SourceLocation location, Schema parentSchema, Particle particle) {
    super(location, parentSchema);
    this.particle = particle;
  }

  public Particle getParticle() {
    return particle;
  }

  public void setParticle(Particle particle) {
    this.particle = particle;
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitRoot(this);
  }
}
