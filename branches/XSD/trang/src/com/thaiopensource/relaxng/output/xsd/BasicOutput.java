package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.common.XmlWriter;
import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRef;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeList;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.Occurs;
import com.thaiopensource.relaxng.output.xsd.basic.Facet;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleRepeat;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleSequence;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleChoice;
import com.thaiopensource.relaxng.output.xsd.basic.ParticleAll;
import com.thaiopensource.relaxng.output.xsd.basic.GroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUseVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupRef;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeUse;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeComplexContent;
import com.thaiopensource.relaxng.output.xsd.basic.ComplexTypeSimpleContent;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.AbstractSchemaVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.GroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.AttributeGroupDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeDefinition;
import com.thaiopensource.relaxng.output.xsd.basic.RootDeclaration;
import com.thaiopensource.relaxng.output.xsd.basic.StructureVisitor;
import com.thaiopensource.relaxng.output.xsd.basic.Structure;
import com.thaiopensource.relaxng.output.xsd.basic.OptionalAttribute;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaWalker;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.io.IOException;

public class BasicOutput {
  private XmlWriter xw;
  static final String xsURI = "http://www.w3.org/2001/XMLSchema";
  private final Schema schema;
  private final SimpleTypeOutput simpleTypeOutput = new SimpleTypeOutput();
  private final ComplexTypeVisitor complexTypeOutput = new ComplexTypeOutput();
  private final AttributeUseOutput attributeUseOutput = new AttributeUseOutput();
  private final ParticleOutput particleOutput = new ParticleOutput();
  private final ParticleVisitor globalElementOutput = new GlobalElementOutput();
  private final GlobalAttributeOutput globalAttributeOutput = new GlobalAttributeOutput();
  private final SchemaVisitor schemaOutput = new SchemaOutput();
  private final StructureVisitor movedStructureOutput = new MovedStructureOutput();
  private final SimpleTypeVisitor simpleTypeNamer = new SimpleTypeNamer();
  private final NamespaceManager nsm;
  private final PrefixManager pm;
  private final String targetNamespace;
  private final OutputDirectory od;
  private final String sourceUri;
  private final Set globalElementsDefined;
  private final Set globalAttributesDefined;
  private final String xsPrefix;

  class SimpleTypeOutput implements SimpleTypeVisitor {
    public Object visitRestriction(SimpleTypeRestriction t) {
      xw.startElement(xs("restriction"));
      xw.attribute("base", xs(t.getName()));
      for (Iterator iter = t.getFacets().iterator(); iter.hasNext();) {
        Facet facet = (Facet)iter.next();
        xw.startElement(xs(facet.getName()));
        xw.attribute("value", facet.getValue());
        xw.endElement();
      }
      xw.endElement();
      return null;
    }

    public Object visitRef(SimpleTypeRef t) {
      xw.startElement(xs("restriction"));
      xw.attribute("base", qualifyRef(schema.getSimpleType(t.getName()).getParentSchema().getUri(),
                                      t.getName()));
      xw.endElement();
      return null;
    }

    public Object visitUnion(SimpleTypeUnion t) {
      xw.startElement(xs("union"));
      StringBuffer buf = new StringBuffer();
      for (Iterator iter = t.getChildren().iterator(); iter.hasNext();) {
        String typeName = (String)((SimpleType)iter.next()).accept(simpleTypeNamer);
        if (typeName != null) {
          if (buf.length() != 0)
            buf.append(' ');
          buf.append(typeName);
        }
      }
      if (buf.length() != 0)
        xw.attribute("memberTypes", buf.toString());
      for (Iterator iter = t.getChildren().iterator(); iter.hasNext();) {
        SimpleType simpleType = (SimpleType)iter.next();
        if (simpleType.accept(simpleTypeNamer) == null)
          outputWrap(simpleType);
      }
      xw.endElement();
      return null;
    }

    public Object visitList(SimpleTypeList t) {
      Occurs occ = t.getOccurs();
      if (!occ.equals(Occurs.ZERO_OR_MORE)) {
        xw.startElement(xs("restriction"));
        xw.startElement(xs("simpleType"));
      }
      xw.startElement(xs("list"));
      outputWrap(t.getItemType(), "itemType");
      xw.endElement();
      if (!occ.equals(Occurs.ZERO_OR_MORE)) {
        xw.endElement();
        if (occ.getMin() == occ.getMax()) {
          xw.startElement("length");
          xw.attribute("value", Integer.toString(occ.getMin()));
          xw.endElement();
        }
        else {
          if (occ.getMin() != 0) {
            xw.startElement(xs("minLength"));
            xw.attribute("value", Integer.toString(occ.getMin()));
            xw.endElement();
          }
          if (occ.getMax() != Occurs.UNBOUNDED) {
            xw.startElement(xs("maxLength"));
            xw.attribute("value", Integer.toString(occ.getMax()));
            xw.endElement();
          }
        }
        xw.endElement();
      }
      return null;
    }

    void outputWrap(SimpleType t) {
      outputWrap(t, "type");
    }

    void outputWrap(SimpleType t, String attributeName) {
      String typeName = (String)t.accept(simpleTypeNamer);
      if (typeName != null)
        xw.attribute(attributeName, typeName);
      else {
        xw.startElement(xs("simpleType"));
        t.accept(this);
        xw.endElement();
      }
    }
  }

  class SimpleTypeNamer extends SchemaWalker {
    public Object visitRestriction(SimpleTypeRestriction t) {
      if (t.getFacets().size() > 0)
        return null;
      return xs(t.getName());
    }

    public Object visitRef(SimpleTypeRef t) {
      return qualifyRef(schema.getSimpleType(t.getName()).getParentSchema().getUri(),
                        t.getName());
    }
  }

  private static final int NORMAL_CONTEXT = 0;
  private static final int COMPLEX_TYPE_CONTEXT = 1;
  private static final int NAMED_GROUP_CONTEXT = 2;

  class ParticleOutput implements ParticleVisitor {
    private Occurs occ = Occurs.EXACTLY_ONE;
    private int context = NORMAL_CONTEXT;

    private boolean startWrapperForElement() {
      boolean needWrapper = context >= COMPLEX_TYPE_CONTEXT;
      context = NORMAL_CONTEXT;
      if (needWrapper)
        xw.startElement(xs("sequence"));
      xw.startElement(xs("element"));
      outputOccurAttributes();
      return needWrapper;
    }

    private boolean startWrapperForGroupRef() {
      boolean needWrapper = context == NAMED_GROUP_CONTEXT;
      context = NORMAL_CONTEXT;
      if (needWrapper)
        xw.startElement(xs("sequence"));
      xw.startElement(xs("group"));
      outputOccurAttributes();
      return needWrapper;
    }

    private boolean startWrapperForGroup(String groupType) {
      boolean needWrapper = context == NAMED_GROUP_CONTEXT && !occ.equals(Occurs.EXACTLY_ONE);
      context = NORMAL_CONTEXT;
      if (needWrapper)
        xw.startElement(xs("sequence"));
      xw.startElement(xs(groupType));
      outputOccurAttributes();
      return needWrapper;
    }

    private void endWrapper(boolean extra) {
      xw.endElement();
      if (extra)
        xw.endElement();
    }

    public Object visitElement(Element p) {
      boolean usedWrapper;
      if (nsm.isGlobal(p)) {
        usedWrapper = startWrapperForElement();
        xw.attribute("ref", qualifyName(p.getName()));
      }
      else if (!namespaceIsLocal(p.getName().getNamespaceUri())) {
        usedWrapper = startWrapperForGroupRef();
        xw.attribute("ref", qualifyName(p.getName().getNamespaceUri(),
                                        nsm.getProxyName(p)));
      }
      else {
        usedWrapper = startWrapperForElement();
        xw.attribute("name", p.getName().getLocalName());
        if (!p.getName().getNamespaceUri().equals(targetNamespace))
          xw.attribute("form", "unqualified");
        p.getComplexType().accept(complexTypeOutput);
      }
      endWrapper(usedWrapper);
      return null;
    }

    public Object visitRepeat(ParticleRepeat p) {
      occ = Occurs.multiply(occ, p.getOccurs());
      p.getChild().accept(this);
      return null;
    }

    public Object visitSequence(ParticleSequence p) {
      boolean usedWrapper = startWrapperForGroup("sequence");
      outputParticles(p.getChildren());
      endWrapper(usedWrapper);
      return null;
    }

    public Object visitChoice(ParticleChoice p) {
      boolean usedWrapper = startWrapperForGroup("choice");
      outputParticles(p.getChildren());
      endWrapper(usedWrapper);
      return null;
    }

    public Object visitAll(ParticleAll p) {
      boolean usedWrapper = startWrapperForGroup("all");
      outputParticles(p.getChildren());
      endWrapper(usedWrapper);
      return null;
    }

    private void outputParticles(List particles) {
      for (Iterator iter = particles.iterator(); iter.hasNext();)
        ((Particle)iter.next()).accept(this);
    }

    public Object visitGroupRef(GroupRef p) {
      String name = p.getName();
      GroupDefinition def = schema.getGroup(name);
      Particle particle = def.getParticle();
      boolean usedWrapper;
      if (particle instanceof Element && nsm.isGlobal((Element)particle)) {
        usedWrapper = startWrapperForElement();
        xw.attribute("ref", qualifyName(((Element)particle).getName()));
      }
      else {
        usedWrapper = startWrapperForGroupRef();
        xw.attribute("ref", qualifyRef(def.getParentSchema().getUri(), name));
      }
      endWrapper(usedWrapper);
      return null;
    }

    void outputOccurAttributes() {
      if (occ.getMin() != 1)
        xw.attribute("minOccurs", Integer.toString(occ.getMin()));
      if (occ.getMax() != 1)
        xw.attribute("maxOccurs",
                     occ.getMax() == Occurs.UNBOUNDED ? "unbounded" : Integer.toString(occ.getMax()));
      occ = Occurs.EXACTLY_ONE;
    }
  }

  class ComplexTypeOutput implements ComplexTypeVisitor {
    public Object visitComplexContent(ComplexTypeComplexContent t) {
      xw.startElement(xs("complexType"));
      if (t.isMixed())
        xw.attribute("mixed", "true");
      if (t.getParticle() != null) {
        particleOutput.context = COMPLEX_TYPE_CONTEXT;
        t.getParticle().accept(particleOutput);
      }
      attributeUseOutput.outputList(t.getAttributeUses());
      xw.endElement();
      return null;
    }

    public Object visitSimpleContent(ComplexTypeSimpleContent t) {
      List attributeUses = t.getAttributeUses();
      if (attributeUses.size() == 0)
        simpleTypeOutput.outputWrap(t.getSimpleType());
      else {
        xw.startElement(xs("complexType"));
        xw.startElement(xs("simpleContent"));
        String typeName = (String)t.getSimpleType().accept(simpleTypeNamer);
        if (typeName != null) {
          xw.startElement(xs("extension"));
          xw.attribute("base", typeName);
        }
        else {
          xw.startElement(xs("restriction"));
          xw.attribute("base", xs("anyType"));
          simpleTypeOutput.outputWrap(t.getSimpleType());
        }
        attributeUseOutput.outputList(attributeUses);
        xw.endElement();
        xw.endElement();
        xw.endElement();
      }
      return null;
    }
  }

  class AttributeUseOutput implements AttributeUseVisitor {
    boolean isOptional = false;

    public Object visitOptionalAttribute(OptionalAttribute a) {
      isOptional = true;
      a.getAttribute().accept(this);
      isOptional = false;
      return null;
    }

    public Object visitAttribute(Attribute a) {
      if (nsm.isGlobal(a)) {
        xw.startElement(xs("attribute"));
        xw.attribute("ref", qualifyName(a.getName()));
        if (!isOptional)
          xw.attribute("use", "required");
        xw.endElement();
      }
      else if (namespaceIsLocal(a.getName().getNamespaceUri())) {
        xw.startElement(xs("attribute"));
        xw.attribute("name", a.getName().getLocalName());
        if (!isOptional)
          xw.attribute("use", "required");
        if (!a.getName().getNamespaceUri().equals(""))
          xw.attribute("form", "qualified");
        if (a.getType() != null)
          simpleTypeOutput.outputWrap(a.getType());
        xw.endElement();
      }
      else {
        xw.startElement(xs("attributeGroup"));
        xw.attribute("ref",
                     qualifyName(a.getName().getNamespaceUri(),
                                 nsm.getProxyName(a)));
        xw.endElement();
      }
      return null;
    }

    public Object visitAttributeGroupRef(AttributeGroupRef a) {
      xw.startElement(xs("attributeGroup"));
      String name = a.getName();
      xw.attribute("ref",
                   qualifyRef(schema.getAttributeGroup(name).getParentSchema().getUri(), name));
      xw.endElement();
      return null;
    }

    void outputList(List list) {
      for (Iterator iter = list.iterator(); iter.hasNext();)
        ((AttributeUse)iter.next()).accept(this);
    }
  }

  class GlobalElementOutput implements ParticleVisitor, ComplexTypeVisitor {
    public Object visitElement(Element p) {
      Name name = p.getName();
      if (nsm.isGlobal(p)
          && name.getNamespaceUri().equals(targetNamespace)
          && !globalElementsDefined.contains(name)) {
        globalElementsDefined.add(name);
        xw.startElement(xs("element"));
        xw.attribute("name", name.getLocalName());
        p.getComplexType().accept(complexTypeOutput);
        xw.endElement();
      }
      return p.getComplexType().accept(this);
    }

    public Object visitRepeat(ParticleRepeat p) {
      return p.getChild().accept(this);
    }

    void visitList(List list) {
      for (Iterator iter = list.iterator(); iter.hasNext();)
        ((Particle)iter.next()).accept(this);
    }

    public Object visitSequence(ParticleSequence p) {
      visitList(p.getChildren());
      return null;
    }

    public Object visitChoice(ParticleChoice p) {
      visitList(p.getChildren());
      return null;
    }

    public Object visitAll(ParticleAll p) {
      visitList(p.getChildren());
      return null;
    }

    public Object visitGroupRef(GroupRef p) {
      return null;
    }

    public Object visitComplexContent(ComplexTypeComplexContent t) {
      if (t.getParticle() == null)
        return null;
      return t.getParticle().accept(this);
    }

    public Object visitSimpleContent(ComplexTypeSimpleContent t) {
      return null;
    }
  }

  class GlobalAttributeOutput implements AttributeUseVisitor {
    void outputList(List list) {
      for (Iterator iter = list.iterator(); iter.hasNext();)
        ((AttributeUse)iter.next()).accept(this);
    }

    public Object visitAttribute(Attribute a) {
      Name name = a.getName();
      if (nsm.isGlobal(a)
          && name.getNamespaceUri().equals(targetNamespace)
          && !globalAttributesDefined.contains(name)) {
        globalAttributesDefined.add(name);
        xw.startElement(xs("attribute"));
        xw.attribute("name", name.getLocalName());
        if (a.getType() != null)
          simpleTypeOutput.outputWrap(a.getType());
        xw.endElement();
      }
      return null;
    }

    public Object visitOptionalAttribute(OptionalAttribute a) {
      return a.getAttribute().accept(this);
    }

    public Object visitAttributeGroupRef(AttributeGroupRef a) {
      return null;
    }
  }

  class SchemaOutput extends AbstractSchemaVisitor {
    public void visitGroup(GroupDefinition def) {
      Particle particle = def.getParticle();
      if (!(particle instanceof Element) || !nsm.isGlobal((Element)particle)) {
        xw.startElement(xs("group"));
        xw.attribute("name", def.getName());
        particleOutput.context = NAMED_GROUP_CONTEXT;
        particle.accept(particleOutput);
        xw.endElement();
      }
      particle.accept(globalElementOutput);
    }

    public void visitSimpleType(SimpleTypeDefinition def) {
      xw.startElement(xs("simpleType"));
      xw.attribute("name", def.getName());
      def.getSimpleType().accept(simpleTypeOutput);
      xw.endElement();
    }

    public void visitAttributeGroup(AttributeGroupDefinition def) {
      xw.startElement(xs("attributeGroup"));
      xw.attribute("name", def.getName());
      attributeUseOutput.outputList(def.getAttributeUses());
      xw.endElement();
      globalAttributeOutput.outputList(def.getAttributeUses());
    }

    public void visitRoot(RootDeclaration decl) {
      decl.getParticle().accept(globalElementOutput);
    }
  }

  class MovedStructureOutput implements StructureVisitor {
    public Object visitElement(Element element) {
      if (!nsm.isGlobal(element)) {
        xw.startElement(xs("group"));
        xw.attribute("name", nsm.getProxyName(element));
        particleOutput.context = NAMED_GROUP_CONTEXT;
        particleOutput.visitElement(element);
        xw.endElement();
      }
      globalElementOutput.visitElement(element);
      return null;
    }

    public Object visitAttribute(Attribute attribute) {
      if (!nsm.isGlobal(attribute)) {
        xw.startElement(xs("attributeGroup"));
        xw.attribute("name", nsm.getProxyName(attribute));
        attributeUseOutput.visitAttribute(attribute);
        xw.endElement();
      }
      globalAttributeOutput.visitAttribute(attribute);
      return null;
    }
  }

  static void output(Schema schema, PrefixManager pm, OutputDirectory od, ErrorReporter er) throws IOException {
    NamespaceManager nsm = new NamespaceManager(schema, pm);
    Set globalElementsDefined = new HashSet();
    Set globalAttributesDefined = new HashSet();
    try {
      for (Iterator iter = schema.getSubSchemas().iterator(); iter.hasNext();)
        new BasicOutput((Schema)iter.next(), er, od, nsm, pm,
                        globalElementsDefined, globalAttributesDefined).output();
    }
    catch (XmlWriter.WrappedException e) {
      throw e.getIOException();
    }
  }

  public BasicOutput(Schema schema, ErrorReporter er, OutputDirectory od,
                     NamespaceManager nsm, PrefixManager pm,
                     Set globalElementsDefined, Set globalAttributesDefined) throws IOException {
    this.schema = schema;
    this.nsm = nsm;
    this.pm = pm;
    this.globalElementsDefined = globalElementsDefined;
    this.globalAttributesDefined = globalAttributesDefined;
    this.sourceUri = schema.getUri();
    this.od = od;
    this.targetNamespace = nsm.getTargetNamespace(schema.getUri());
    this.xsPrefix = pm.getPrefix(xsURI);
    xw = new XmlWriter(od.getLineSeparator(),
                       od.open(schema.getUri()),
                       new String[0],
                       od.getEncoding());
  }

  void output() {
    xw.startElement(xs("schema"));
    xw.attribute("xmlns:" + xsPrefix, xsURI);
    xw.attribute("elementFormDefault", "qualified");
    xw.attribute("version", "1.0");
    if (!targetNamespace.equals(""))
      xw.attribute("targetNamespace", targetNamespace);
    for (Iterator iter = nsm.getTargetNamespaces().iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      if (!ns.equals("")) {
        String prefix = pm.getPrefix(ns);
        if (!prefix.equals("xml"))
          xw.attribute("xmlns:" + pm.getPrefix(ns), ns);
      }
    }
    for (Iterator iter = nsm.effectiveIncludes(schema.getUri()).iterator(); iter.hasNext();)
      outputInclude((String)iter.next());
    for (Iterator iter = nsm.getTargetNamespaces().iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      if (!ns.equals(targetNamespace))
        outputImport(ns, nsm.getRootSchema(ns));
    }
    schema.accept(schemaOutput);

    for (Iterator iter = nsm.getMovedStructures(targetNamespace).iterator(); iter.hasNext();)
      ((Structure)iter.next()).accept(movedStructureOutput);
    xw.endElement();
    xw.close();
  }

  private String xs(String name) {
    return xsPrefix + ":" + name;
  }

  private boolean namespaceIsLocal(String ns) {
    return ns.equals(targetNamespace) || ns.equals("");
  }

  private String qualifyRef(String schemaUri, String localName) {
    return qualifyName(nsm.getTargetNamespace(schemaUri), localName);
  }

  private String qualifyName(Name name) {
    return qualifyName(name.getNamespaceUri(), name.getLocalName());
  }

  private String qualifyName(String ns, String localName) {
    if (ns.equals(""))
      return localName;
    return pm.getPrefix(ns) + ":" + localName;
  }

  void outputInclude(String href) {
    xw.startElement(xs("include"));
    xw.attribute("schemaLocation", od.reference(sourceUri, href));
    xw.endElement();
  }

  void outputImport(String ns, String href) {
    xw.startElement(xs("import"));
    if (!ns.equals(""))
      xw.attribute("namespace", ns);
    xw.attribute("schemaLocation", od.reference(sourceUri, href));
    xw.endElement();
  }
}
