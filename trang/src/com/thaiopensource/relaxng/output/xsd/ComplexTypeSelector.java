package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.SchemaWalker;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.GroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleGroup;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeList;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

class ComplexTypeSelector extends SchemaWalker {
  static class Refs {
    Set referencingElements = new HashSet();
    Set referencingDefinitions = new HashSet();
    boolean nonTypeReference = false;
    boolean extensionReference = false;
  }

  private final Map groupMap = new HashMap();
  private final Map attributeGroupMap = new HashMap();
  private final Map simpleTypeMap = new HashMap();
  private String parentDefinition;
  private Element parentElement;
  private int nonTypeReference = 0;
  private int extensionReference = 0;

  public void visitGroup(GroupDefinition def) {
    parentDefinition = def.getName();
    def.getParticle().accept(this);
    parentDefinition = null;
  }

  public void visitSimpleType(SimpleTypeDefinition def) {
    parentDefinition = def.getName();
    def.getSimpleType().accept(this);
    parentDefinition = null;
  }

  public void visitAttributeGroup(AttributeGroupDefinition def) {
    parentDefinition = def.getName();
    def.getAttributeUses().accept(this);
    parentDefinition = null;
  }

  public Object visitElement(Element p) {
    Element oldParentElement = parentElement;
    int oldNonTypeReference = nonTypeReference;
    int oldExtensionReference = extensionReference;
    parentElement = p;
    nonTypeReference = 0;
    extensionReference = 0;
    p.getComplexType().accept(this);
    extensionReference = oldExtensionReference;
    nonTypeReference = oldNonTypeReference;
    parentElement = oldParentElement;
    return null;
  }

  public Object visitSequence(ParticleSequence p) {
    Iterator iter = p.getChildren().iterator();
    extensionReference++;
    ((Particle)iter.next()).accept(this);
    extensionReference--;
    nonTypeReference++;
    while (iter.hasNext())
      ((Particle)iter.next()).accept(this);
    nonTypeReference--;
    return null;
  }

  public Object visitChoice(ParticleChoice p) {
    nonTypeReference++;
    super.visitChoice(p);
    nonTypeReference--;
    return null;
  }

  public Object visitAll(ParticleAll p) {
    nonTypeReference++;
    super.visitAll(p);
    nonTypeReference--;
    return null;
  }

  public Object visitRepeat(ParticleRepeat p) {
    nonTypeReference++;
    super.visitRepeat(p);
    nonTypeReference--;
    return null;
  }

  public Object visitAttribute(Attribute a) {
    nonTypeReference++;
    SimpleType t = a.getType();
    if (t != null)
      t.accept(this);
    nonTypeReference--;
    return null;
  }

  public Object visitUnion(SimpleTypeUnion t) {
    nonTypeReference++;
    super.visitUnion(t);
    nonTypeReference--;
    return null;
  }

  public Object visitList(SimpleTypeList t) {
    nonTypeReference++;
    super.visitList(t);
    nonTypeReference--;
    return null;
  }

  public Object visitGroupRef(GroupRef p) {
    noteRef(groupMap, p.getName());
    return null;
  }

  public Object visitAttributeGroupRef(AttributeGroupRef a) {
    noteRef(attributeGroupMap, a.getName());
    return null;
  }

  public Object visitRef(SimpleTypeRef t) {
    // Don't make it a complex type unless there are attributes
    extensionReference++;
    noteRef(simpleTypeMap, t.getName());
    extensionReference--;
    return null;
  }

  private void noteRef(Map map, String name) {
    Refs refs = lookupRefs(map, name);
    if (nonTypeReference > 0)
      refs.nonTypeReference = true;
    else if (parentElement != null)
      refs.referencingElements.add(parentElement);
    else if (parentDefinition != null)
      refs.referencingDefinitions.add(parentDefinition);
    if (extensionReference > 0)
      refs.extensionReference = true;
  }

  static private Refs lookupRefs(Map map, String name) {
    Refs refs = (Refs)map.get(name);
    if (refs == null) {
      refs = new Refs();
      map.put(name, refs);
    }
    return refs;
  }

  private Set complexTypeGroups = new HashSet();
  private Set complexTypeSimpleTypes = new HashSet();

  void assignComplexTypeGroups(Schema schema) {
    schema.accept(this);
    chooseComplexTypes(complexTypeGroups, groupMap);
    describeComplexTypes("Groups to be made complex types:", complexTypeGroups);
    chooseComplexTypes(complexTypeSimpleTypes, simpleTypeMap);
    describeComplexTypes("Simple types to be made complex types:", complexTypeSimpleTypes);
  }

  static private void describeComplexTypes(String message, Set complexTypeNames) {
    System.err.print(message);
    for (Iterator iter = complexTypeNames.iterator(); iter.hasNext();) {
      System.err.print(" ");
      System.err.print((String)iter.next());
    }
    System.err.println();
  }

  void chooseComplexTypes(Set complexTypeNames, Map definitionMap) {
    for (;;) {
      boolean foundOne = false;
      for (Iterator iter = definitionMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String name = (String)entry.getKey();
        if (!complexTypeNames.contains(name)
            && isPossibleComplexType((Refs)entry.getValue(),
                                     (Refs)attributeGroupMap.get(name),
                                     complexTypeNames)) {
          foundOne = true;
          complexTypeNames.add(name);
        }
      }
      if (!foundOne)
        break;
    }
  }

  boolean isPossibleComplexType(Refs childRefs, Refs attributeGroupRefs, Set complexTypeNames) {
    if (childRefs.nonTypeReference)
      return false;
    if (attributeGroupRefs == null) {
      if (childRefs.extensionReference)
        return false;
    }
    else if (!attributeGroupRefs.referencingDefinitions.equals(childRefs.referencingDefinitions)
             || !attributeGroupRefs.referencingElements.equals(childRefs.referencingElements))
      return false;
    for (Iterator iter = childRefs.referencingDefinitions.iterator(); iter.hasNext();)
      if (!complexTypeNames.contains(iter.next()))
        return false;
    return true;
  }
}
