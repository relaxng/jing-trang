package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.Element;
import com.thaiopensource.relaxng.output.xsd.basic.Attribute;
import com.thaiopensource.relaxng.output.xsd.basic.Schema;
import com.thaiopensource.relaxng.output.xsd.basic.Particle;
import com.thaiopensource.relaxng.output.xsd.basic.SchemaWalker;
import com.thaiopensource.relaxng.output.xsd.basic.GroupDefinition;
import com.thaiopensource.relaxng.output.common.Name;

import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

public class NamespaceManager {
  private final Schema schema;
  private final List generatedSourceUris = new Vector();
  private final Map elementNameMap = new HashMap();

  private static final String ANON = "anon";

  static class SourceUri {
    String targetNamespace;
    // list of strings giving included URIs
    List includes = new Vector();
  }

  static class TargetNamespace {
    String rootSchema;
    List movedPatterns = new Vector();
    Set movedPatternSet = new HashSet();
    Map movedPatternNameMap = new HashMap();
    int nextMovedElementSuffix;
    int nextMovedAttributeSuffix;
  }

  static class NameInfo {
    static final int OCCUR_NONE = 0;
    static final int OCCUR_NESTED = 1;
    static final int OCCUR_TOP = 2;
    static final int OCCUR_MOVE = 3;
    static final int OCCUR_ROOT = 4;
    int occur;
    Particle globalType;
  }

  // Maps sourceUri to SourceUri
  private final Map sourceUriMap = new HashMap();
  // Maps targetNamespace to TargetNamespace
  private final Map targetNamespaceMap = new HashMap();

  class RootMarker extends SchemaWalker {
    public void visitGroup(GroupDefinition def) {
    }

    public Object visitElement(Element p) {
      NameInfo info = lookupElementName(p.getName());
      info.globalType = p;
      info.occur = NameInfo.OCCUR_ROOT;
      lookupTargetNamespace(p.getName().getNamespaceUri());
      return null;
    }
  }

  NamespaceManager(Schema schema) {
    this.schema = schema;
    schema.accept(new RootMarker());
  }

  boolean isGlobal(Element element) {
    return lookupElementName(element.getName()).globalType.equals(element);
  }

  boolean isGlobal(Attribute attribute) {
    return false;
  }

  String getProxy(Element element) {
    return null;
  }

  String getProxy(Attribute attribute) {
    return null;
  }

  String getTargetNamespace(String schemaUri) {
    return lookupSourceUri(schemaUri).targetNamespace;
  }

  Set getTargetNamespaces() {
    return targetNamespaceMap.keySet();
  }

  String getRootSchema(String targetNamespace) {
    return lookupTargetNamespace(targetNamespace).rootSchema;
  }

  List getMovedElements(String namespace) {
    return null;
  }

  List getMovedAttributes(String namespace) {
    return null;
  }

  List effectiveIncludes(String sourceUri) {
    String ns = getTargetNamespace(sourceUri);
    List list = new Vector();
    for (Iterator iter = lookupSourceUri(sourceUri).includes.iterator(); iter.hasNext();)
      findRootSchemas((String)iter.next(), ns, list);
    return list;
  }

  private void findRootSchemas(String sourceUri, String ns, List list) {
    if (getTargetNamespace(sourceUri).equals(ns))
      list.add(sourceUri);
    else {
      for (Iterator iter = lookupSourceUri(sourceUri).includes.iterator(); iter.hasNext();)
        findRootSchemas((String)iter.next(), ns, list);
    }
  }

  private SourceUri lookupSourceUri(String uri) {
    SourceUri s = (SourceUri)sourceUriMap.get(uri);
    if (s == null) {
      s = new SourceUri();
      sourceUriMap.put(uri, s);
    }
    return s;
  }

  private TargetNamespace lookupTargetNamespace(String ns) {
    TargetNamespace t = (TargetNamespace)targetNamespaceMap.get(ns);
    if (t == null) {
      t = new TargetNamespace();
      targetNamespaceMap.put(ns, t);
    }
    return t;
  }

  private NameInfo lookupElementName(Name name) {
    NameInfo info = (NameInfo)elementNameMap.get(name);
    if (info == null) {
      info = new NameInfo();
      elementNameMap.put(name, info);
    }
    return info;
  }

}
