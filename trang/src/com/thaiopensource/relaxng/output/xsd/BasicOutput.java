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
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.Iterator;
import java.util.List;
import java.io.IOException;

public class BasicOutput {
  private XmlWriter xw;
  private final Schema schema;
  private final SimpleTypeOutput simpleTypeOutput = new SimpleTypeOutput();
  private final ComplexTypeVisitor complexTypeOutput = new ComplexTypeOutput();
  private final AttributeUseOutput attributeUseOutput = new AttributeUseOutput();
  private final ParticleVisitor particleOutput = new ParticleOutput();
  private final ParticleVisitor globalElementOutput = new GlobalElementOutput();
  private final SchemaVisitor schemaOutput = new SchemaOutput();
  private final StructureVisitor movedStructureOutput = new MovedStructureOutput();
  private final NamespaceManager nsm;
  private final PrefixManager pm;
  private final String targetNamespace;
  private final OutputDirectory od;
  private final String sourceUri;

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
      for (Iterator iter = t.getChildren().iterator(); iter.hasNext();)
        outputWrap((SimpleType)iter.next());
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
      outputWrap(t.getItemType());
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
      xw.startElement(xs("simpleType"));
      t.accept(this);
      xw.endElement();
    }
  }

  // TODO deal with different particle output in complexType and namedGroup

  class ParticleOutput implements ParticleVisitor {
    private Occurs occ = Occurs.EXACTLY_ONE;

    public Object visitElement(Element p) {
      if (nsm.isGlobal(p)) {
        xw.startElement(xs("element"));
        outputOccurAttributes();
        xw.endElement();
      }
      else if (!namespaceIsLocal(p.getName().getNamespaceUri())) {
        xw.startElement(xs("group"));
        xw.attribute("ref", qualifyName(p.getName().getNamespaceUri(),
                                        nsm.getProxyName(p)));
        outputOccurAttributes();
        xw.endElement();
      }
      else {
        xw.startElement(xs("element"));
        xw.attribute("name", p.getName().getLocalName());
        if (!p.getName().getNamespaceUri().equals(targetNamespace))
          xw.attribute("form", "unqualified");
        outputOccurAttributes();
        p.getComplexType().accept(complexTypeOutput);
        xw.endElement();
      }
      return null;
    }

    public Object visitRepeat(ParticleRepeat p) {
      occ = Occurs.multiply(occ, p.getOcccurs());
      p.getChild().accept(this);
      return null;
    }

    public Object visitSequence(ParticleSequence p) {
      xw.startElement(xs("sequence"));
      outputParticles(p.getChildren());
      xw.endElement();
      return null;
    }

    public Object visitChoice(ParticleChoice p) {
      xw.startElement(xs("choice"));
      outputParticles(p.getChildren());
      xw.endElement();
      return null;
    }

    public Object visitAll(ParticleAll p) {
      xw.startElement(xs("all"));
      outputParticles(p.getChildren());
      xw.endElement();
      return null;
    }

    private void outputParticles(List particles) {
      outputOccurAttributes();
      for (Iterator iter = particles.iterator(); iter.hasNext();)
        ((Particle)iter.next()).accept(this);
    }

    public Object visitGroupRef(GroupRef p) {
      String name = p.getName();
      GroupDefinition def = schema.getGroup(name);
      Particle particle = def.getParticle();
      if (particle instanceof Element && nsm.isGlobal((Element)particle)) {
        xw.startElement(xs("element"));
        xw.attribute("ref", qualifyName(((Element)particle).getName()));
        outputOccurAttributes();
        xw.endElement();
      }
      else {
        xw.startElement(xs("group"));
        xw.attribute("ref", qualifyRef(def.getParentSchema().getUri(), name));
        outputOccurAttributes();
        xw.endElement();
      }
      return null;
    }

    void outputOccurAttributes() {
      if (occ.getMin() != 1)
        xw.attribute("minOccurs", Integer.toString(occ.getMin()));
      else if (occ.getMax() != 1)
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
      attributeUseOutput.outputList(t.getAttributeUses());
      if (t.getParticle() != null)
        t.getParticle().accept(particleOutput);
      xw.endElement();
      return null;
    }

    public Object visitSimpleContent(ComplexTypeSimpleContent t) {
      xw.startElement(xs("complexType"));
      xw.startElement(xs("simpleContent"));
      xw.startElement(xs("restriction"));
      xw.attribute("base", xs("anyType"));
      simpleTypeOutput.outputWrap(t.getSimpleType());
      attributeUseOutput.outputList(t.getAttributeUses());
      xw.endElement();
      xw.endElement();
      xw.endElement();
      return null;
    }
  }

  class AttributeUseOutput implements AttributeUseVisitor {
    public Object visitAttribute(Attribute a) {
      if (namespaceIsLocal(a.getName().getNamespaceUri())) {
        xw.startElement(xs("attribute"));
        xw.attribute("name", a.getName().getLocalName());
        if (a.getUse() == Attribute.REQUIRED)
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
      if (nsm.isGlobal(p) && p.getName().getNamespaceUri().equals(targetNamespace)) {
        xw.startElement(xs("element"));
        xw.attribute("name", p.getName().getLocalName());
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

  class SchemaOutput extends AbstractSchemaVisitor {
    public void visitGroup(GroupDefinition def) {
      Particle particle = def.getParticle();
      if (!(particle instanceof Element) || !nsm.isGlobal((Element)particle)) {
        xw.startElement(xs("group"));
        xw.attribute("name", def.getName());
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
    }

    public void visitRoot(RootDeclaration decl) {
      decl.getParticle().accept(globalElementOutput);
    }
  }

  class MovedStructureOutput implements StructureVisitor {
    public Object visitElement(Element element) {
      // TODO
      return null;
    }

    public Object visitAttribute(Attribute attribute) {
      // TODO
      return null;
    }
  }

  static void output(Schema schema, PrefixManager pm, OutputDirectory od, ErrorReporter er) throws IOException {
    NamespaceManager nsm = new NamespaceManager(schema, pm);
    try {
      for (Iterator iter = schema.getSubSchemas().iterator(); iter.hasNext();)
        new BasicOutput((Schema)iter.next(), er, od, nsm, pm).output();
    }
    catch (XmlWriter.WrappedException e) {
      throw e.getIOException();
    }
  }

  public BasicOutput(Schema schema, ErrorReporter er, OutputDirectory od,
                     NamespaceManager nsm, PrefixManager pm) throws IOException {
    this.schema = schema;
    this.nsm = nsm;
    this.pm = pm;
    this.sourceUri = schema.getUri();
    this.od = od;
    this.targetNamespace = nsm.getTargetNamespace(schema.getUri());
    xw = new XmlWriter(od.getLineSeparator(),
                       od.open(schema.getUri()),
                       new String[0],
                       od.getEncoding());
  }

  void output() {
   xw.startElement(xs("schema"));
    // TODO choose xs so as to avoid conflict
    xw.attribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
    xw.attribute("elementFormDefault", "qualified");
    xw.attribute("version", "1.0");
    if (!targetNamespace.equals(""))
      xw.attribute("targetNamespace", targetNamespace);
    for (Iterator iter = nsm.getTargetNamespaces().iterator(); iter.hasNext();) {
      String ns = (String)iter.next();
      // TODO omit xml prefix
      if (!ns.equals(""))
        xw.attribute("xmlns:" + pm.getPrefix(ns), ns);
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
    return "xs:" + name;
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
