package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.SchemaTransformer;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.Facet;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.Occurs;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;

import java.util.List;
import java.util.Iterator;
import java.util.Vector;

class Transformer extends SchemaTransformer {
  Transformer(Schema schema) {
    super(schema);
  }

  public Object visitUnion(SimpleTypeUnion t) {
    List list = transformSimpleTypeList(t.getChildren());
    SimpleType combined = combineEnumeration(t, list);
    if (combined != null)
      return combined;
    return new SimpleTypeUnion(t.getLocation(), list);
  }

  private SimpleType combineEnumeration(SimpleTypeUnion orig, List transformedChildren) {
    if (transformedChildren.size() < 2)
      return null;
    Object first = transformedChildren.get(0);
    if (!(first instanceof SimpleTypeRestriction))
      return null;
    String builtinTypeName = ((SimpleTypeRestriction)first).getName();
    List facets = new Vector();
    for (Iterator iter = transformedChildren.iterator(); iter.hasNext();) {
      Object obj = iter.next();
      if (!(obj instanceof SimpleTypeRestriction))
        return null;
      SimpleTypeRestriction restriction = (SimpleTypeRestriction)obj;
      if (!restriction.getName().equals(builtinTypeName))
        return null;
      if (restriction.getFacets().isEmpty())
        return null;
      for (Iterator facetIter = restriction.getFacets().iterator(); facetIter.hasNext();) {
        Facet facet = (Facet)facetIter.next();
        if (!facet.getName().equals("enumeration"))
          return null;
        facets.add(facet);
      }
    }
    return new SimpleTypeRestriction(orig.getLocation(), builtinTypeName, facets);
  }

  class SequenceDetector implements ParticleVisitor {
    public Object visitElement(Element p) {
      return Boolean.FALSE;
    }

    public Object visitSequence(ParticleSequence p) {
      return Boolean.TRUE;
    }

    public Object visitGroupRef(GroupRef p) {
      return getSchema().getGroup(p.getName()).getParticle().accept(this);
    }

    public Object visitAll(ParticleAll p) {
      return Boolean.FALSE;
    }

    public Object visitRepeat(ParticleRepeat p) {
      return p.getChild().accept(this);
    }

    public Object visitChoice(ParticleChoice p) {
      for (Iterator iter = p.getChildren().iterator(); iter.hasNext();)
        if (((Particle)iter.next()).accept(this) == Boolean.TRUE)
          return Boolean.TRUE;
      return Boolean.FALSE;
    }
  }

  class AllBodyTransformer extends SchemaTransformer {
    public AllBodyTransformer(Schema schema) {
      super(schema);
    }

    public Object visitGroupRef(GroupRef p) {
      if (new SequenceDetector().visitGroupRef(p) == Boolean.FALSE)
        return p;
      return getSchema().getGroup(p.getName()).getParticle().accept(this);
    }

    public Object visitSequence(ParticleSequence p) {
      return new ParticleChoice(p.getLocation(), transformParticleList(p.getChildren()));
    }

    public Object visitRepeat(ParticleRepeat p) {
      return p.getChild().accept(this);
    }
  }


  public Object visitAll(ParticleAll p) {
    return new ParticleRepeat(p.getLocation(),
                              new ParticleChoice(p.getLocation(),
                                                 new AllBodyTransformer(getSchema()).transformParticleList(transformParticleList(p.getChildren()))),
                              Occurs.ZERO_OR_MORE);

  }
}
