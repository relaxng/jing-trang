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
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUseChoice;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.SingleAttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.OptionalAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUseVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroup;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.AbstractAttributeUseVisitor;
import com.thaiopensource.relaxng.output.common.Name;

import java.util.List;
import java.util.Iterator;
import java.util.Vector;
import java.util.Map;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;

class Transformer extends SchemaTransformer {
  private final AttributeMapper attributeMapper = new AttributeMapper();
  private Set transformedAttributeGroups = new HashSet();

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

  public Object visitAttributeUseChoice(AttributeUseChoice a) {
    List children = transformAttributeUseList(a.getChildren());
    Map[] maps = new Map[children.size()];
    for (int i = 0; i < maps.length; i++)
      maps[i] = attributeMapper.getAttributeMap((AttributeUse)children.get(i));
    Set common = new HashSet(maps[0].keySet());
    Set union = new HashSet(maps[0].keySet());
    for (int i = 1; i < maps.length; i++) {
      common.retainAll(maps[i].keySet());
      union.addAll(maps[i].keySet());
    }
    Set[] retainAttributeNames = new Set[children.size()];
    for (int i = 0; i < retainAttributeNames.length; i++)
      retainAttributeNames[i] = new HashSet();
    List newChildren = new Vector();
    for (Iterator iter = union.iterator(); iter.hasNext();) {
      Name name = (Name)iter.next();
      SingleAttributeUse[] uses = new SingleAttributeUse[maps.length];
      int useIndex = -1;
      for (int i = 0; i < maps.length; i++) {
        uses[i] = (SingleAttributeUse)maps[i].get(name);
        if (uses[i] != null) {
          if (useIndex >= 0)
            useIndex = -2;
          else if (useIndex == -1)
            useIndex = i;
        }
      }
      if (useIndex < 0)
        useIndex = chooseUseIndex(uses);
      if (useIndex >= 0)
        retainAttributeNames[useIndex].add(name);
      else {
        List choices = new Vector();
        for (int i = 0; i < uses.length; i++)
          choices.add(uses[i].getType());
        Attribute tem = new Attribute(a.getLocation(),
                                      name,
                                      (SimpleType)new SimpleTypeUnion(a.getLocation(), choices).accept(this));
        if (common.contains(name))
          newChildren.add(tem);
        else
          newChildren.add(new OptionalAttribute(a.getLocation(), tem));
      }
    }
    for (int i = 0; i < retainAttributeNames.length; i++) {
      Object tem = ((AttributeUse)children.get(i)).accept(new AttributeTransformer(retainAttributeNames[i], common));
      if (!tem.equals(AttributeGroup.EMPTY))
        newChildren.add(tem);
    }
    return new AttributeGroup(a.getLocation(), newChildren);

  }

  private int chooseUseIndex(SingleAttributeUse[] uses) {
    for (int i = 0; i < uses.length; i++)
      if (uses[i].getType() == null)
        return i;
    int firstIndex = -1;
    for (int i = 0; i < uses.length; i++) {
      if (uses[i] != null) {
        if (firstIndex == -1)
          firstIndex = i;
        else if (!uses[i].equals(uses[firstIndex]))
          return -1;
      }
    }
    return firstIndex;
  }

  class AttributeMapper extends AbstractAttributeUseVisitor {
    private final Map cache = new HashMap();

    Map getAttributeMap(AttributeUse a) {
      Map map = (Map)cache.get(a);
      if (map == null) {
        map = (Map)a.accept(this);
        cache.put(a, map);
      }
      return map;
    }

    public Object visitAttribute(Attribute a) {
      Map map = new HashMap();
      map.put(a.getName(), a);
      return map;
    }

    public Object visitAttributeGroup(AttributeGroup a) {
      Map map = new HashMap();
      for (Iterator iter = a.getChildren().iterator(); iter.hasNext();)
        map.putAll(getAttributeMap((AttributeUse)iter.next()));
      return map;
    }

    public Object visitOptionalAttribute(OptionalAttribute a) {
      Map map = new HashMap();
      map.put(a.getAttribute().getName(), a);
      return map;
    }

    public Object visitAttributeGroupRef(AttributeGroupRef a) {
      return getAttributeMap(getTransformedAttributeGroup(a.getName()));
    }
  }

  class AttributeTransformer extends AbstractAttributeUseVisitor {
    private final Set retainNames;
    private final Set requiredNames;

    public AttributeTransformer(Set retainNames, Set requiredNames) {
      this.retainNames = retainNames;
      this.requiredNames = requiredNames;
    }

    public Object visitAttribute(Attribute a) {
      if (!retainNames.contains(a.getName()))
        return AttributeGroup.EMPTY;
      if (!requiredNames.contains(a.getName()))
        return new OptionalAttribute(a.getLocation(), a);
      return a;
    }

    public Object visitOptionalAttribute(OptionalAttribute a) {
      if (!retainNames.contains(a.getName()))
        return AttributeGroup.EMPTY;
      return a;
    }

    public Object visitAttributeGroupRef(AttributeGroupRef a) {
      AttributeUse refed = getTransformedAttributeGroup(a.getName());
      if (isOk(attributeMapper.getAttributeMap(refed)))
        return a;
      return refed.accept(this);
    }

    private boolean isOk(Map map) {
      for (Iterator iter = map.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        Name name = (Name)entry.getKey();
        SingleAttributeUse use = (SingleAttributeUse)entry.getValue();
        if (!retainNames.contains(name))
          return false;
        if (!use.isOptional() && !requiredNames.contains(name))
          return false;
      }
      return true;
    }

    public Object visitAttributeGroup(AttributeGroup a) {
      List children = a.getChildren();
      List transformedChildren = null;
      for (int i = 0, len = children.size(); i < len; i++) {
        Object obj = ((AttributeUse)children.get(i)).accept(this);
        if (transformedChildren != null) {
          if (!obj.equals(AttributeGroup.EMPTY))
            transformedChildren.add(obj);
        }
        else if (obj != children.get(i)) {
          transformedChildren = new Vector();
          for (int j = 0; j < i; j++)
            transformedChildren.add(children.get(j));
          if (!obj.equals(AttributeGroup.EMPTY))
            transformedChildren.add(obj);
        }
      }
      if (transformedChildren == null)
        return a;
      return new AttributeGroup(a.getLocation(), transformedChildren);
    }
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    def.setAttributeUses(getTransformedAttributeGroup(def.getName()));
  }

  AttributeUse getTransformedAttributeGroup(String name) {
    AttributeGroupDefinition def = getSchema().getAttributeGroup(name);
    if (!transformedAttributeGroups.contains(name)) {
      def.setAttributeUses((AttributeUse)def.getAttributeUses().accept(this));
      transformedAttributeGroups.add(name);
    }
    return def.getAttributeUses();
  }

}
