package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class GroupDefinition extends Definition {
  private Particle particle;

  public GroupDefinition(SourceLocation location, Schema parentSchema, String name, Particle particle) {
    super(location, parentSchema, name);
    this.particle = particle;
  }

  public Particle getParticle() {
    return particle;
  }

  public void setParticle(Particle particle) {
    this.particle = particle;
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitGroup(this);
  }
}
