package com.thaiopensource.relaxng.output.xsd.basic;

public interface ParticleVisitor {
  Object visitElement(Element p);
  Object visitRepeat(ParticleRepeat p);
  Object visitSequence(ParticleSequence p);
  Object visitChoice(ParticleChoice p);
  Object visitGroupRef(GroupRef p);
}
