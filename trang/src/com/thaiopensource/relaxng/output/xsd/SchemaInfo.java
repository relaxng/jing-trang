package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.common.ErrorReporter;
import com.thaiopensource.relaxng.output.OutputDirectory;
import com.thaiopensource.relaxng.edit.SchemaCollection;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Iterator;

class SchemaInfo {
  private final SchemaCollection sc;
  private GrammarPattern grammar;
  private final ErrorReporter er;
  private final Map childTypeMap = new HashMap();
  private final Map defineMap = new HashMap();
  private final Map whereDefinedMap = new HashMap();
  private final Set rootElements = new HashSet();
  private final PatternVisitor childTypeVisitor = new ChildTypeVisitor();
  private final List generatedSourceUris = new Vector();

  static class SourceUri {
    String targetNamespace;
    String inheritedNamespace;
    // list of strings giving included URIs
    List includes = new Vector();
  }

  static class TargetNamespace {
    String rootSchema;
    String prefix;
  }

  // Maps sourceUri to SourceUri
  private final Map sourceUriMap = new HashMap();
  // Maps targetNamespace to TargetNamespace
  private final Map targetNamespaceMap = new HashMap();

  abstract class PatternAnalysisVisitor extends AbstractVisitor {
    abstract Object get(Pattern p);
    abstract Object choice(Object o1, Object o2);
    abstract Object group(Object o1, Object o2);
    Object interleave(Object o1, Object o2) {
      return group(o1, o2);
    }
    Object ref(Object obj) {
      return obj;
    }
    Object oneOrMore(Object obj) {
      return group(obj, obj);
    }
    abstract Object empty();
    abstract Object text();
    abstract Object data();
    abstract Object notAllowed();
    Object list(Object obj) {
      return data();
    }

    public Object visitChoice(ChoicePattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = choice(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitGroup(GroupPattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = group(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitInterleave(InterleavePattern p) {
      List list = p.getChildren();
      Object obj = get((Pattern)list.get(0));
      for (int i = 1, length = list.size(); i < length; i++)
        obj = interleave(obj, get((Pattern)list.get(i)));
      return obj;
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return choice(empty(), oneOrMore(get(p.getChild())));
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return oneOrMore(get(p.getChild()));
    }

    public Object visitOptional(OptionalPattern p) {
      return choice(empty(), get(p.getChild()));
    }

    public Object visitEmpty(EmptyPattern p) {
      return empty();
    }

    public Object visitRef(RefPattern p) {
      return ref(get(getBody(p)));
    }

    public Object visitMixed(MixedPattern p) {
      return interleave(text(), get(p.getChild()));
    }

    public Object visitText(TextPattern p) {
      return text();
    }

    public Object visitData(DataPattern p) {
      return data();
    }

    public Object visitValue(ValuePattern p) {
      return data();
    }

    public Object visitList(ListPattern p) {
      return list(get(p.getChild()));
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return notAllowed();
    }
  }

  class ChildTypeVisitor extends PatternAnalysisVisitor {
    Object get(Pattern p) {
      return getChildType(p);
    }

    Object empty() {
      return ChildType.EMPTY;
    }

    Object text() {
      return ChildType.TEXT;
    }

    Object data() {
      return ChildType.DATA;
    }

    Object notAllowed() {
      return ChildType.NOT_ALLOWED;
    }

    Object list(Object obj) {
      if (obj.equals(ChildType.NOT_ALLOWED))
        return obj;
      return data();
    }

    Object choice(Object o1, Object o2) {
      return ChildType.choice((ChildType)o1, (ChildType)o2);
    }

    Object group(Object o1, Object o2) {
      return ChildType.group((ChildType)o1, (ChildType)o2);
    }

    public Object visitElement(ElementPattern p) {
      return ChildType.ELEMENT;
    }

    public Object visitAttribute(AttributePattern p) {
      if (getChildType(p.getChild()).equals(ChildType.NOT_ALLOWED))
        return ChildType.NOT_ALLOWED;
      return ChildType.ATTRIBUTE;
    }
  }

  class GrammarVisitor extends AbstractVisitor {
    private final String sourceUri;
    private final String inheritedNamespace;
    GrammarVisitor(String sourceUri, String inheritedNamespace) {
      this.sourceUri = sourceUri;
      this.inheritedNamespace = inheritedNamespace;
      // TODO detect multiple inclusions
      lookupSourceUri(sourceUri).inheritedNamespace = inheritedNamespace;
    }
    public Object visitDefine(DefineComponent c) {
      if (c.getName() != DefineComponent.START) {
        defineMap.put(c.getName(), c.getBody());
        whereDefinedMap.put(c.getName(), sourceUri);
      }
      return null;
    }

    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      String href = c.getHref();
      lookupSourceUri(sourceUri).includes.add(href);
      getSchema(href).componentsAccept(new GrammarVisitor(href, resolveNamespace(c.getNs(), inheritedNamespace)));
      return null;
    }
  }

  class RootMarker extends AbstractVisitor {
    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    public Object visitDefine(DefineComponent c) {
      if (c.getName() == DefineComponent.START)
        c.getBody().accept(this);
      return null;
    }

    public Object visitElement(ElementPattern p) {
      rootElements.add(p);
      return null;
    }

    public Object visitComposite(CompositePattern p) {
      p.childrenAccept(this);
      return null;
    }

    public Object visitUnary(UnaryPattern p) {
      return p.getChild().accept(this);
    }

    public Object visitRef(RefPattern p) {
      return getBody(p).accept(this);
    }

    public Object visitInclude(IncludeComponent c) {
      getSchema(c.getHref()).componentsAccept(this);
      return null;
    }
  }

  static class NamespaceUsage {
    int elementCount;
    int attributeCount;
    static boolean isBetter(NamespaceUsage n1, NamespaceUsage n2) {
      return (n1.elementCount > n2.elementCount
              || (n1.elementCount == n2.elementCount
                  && n1.attributeCount > n2.attributeCount));
    }
  }

  static class PrefixUsage {
    int count;
  }

  class TargetNamespaceSelector extends AbstractVisitor {
    private boolean isElement;
    private boolean isRoot;
    private String inheritedNamespace;
    private final Map namespaceUsageMap = new HashMap();
    private final Map namespacePrefixUsageMap = new HashMap();

    public Object visitElement(ElementPattern p) {
      isElement = true;
      isRoot = rootElements.contains(p);
      return p.getNameClass().accept(this);
      // don't visit the content
    }

    public Object visitAttribute(AttributePattern p) {
      isElement = false;
      isRoot = false;
      return p.getNameClass().accept(this);
    }

    public Object visitChoice(ChoiceNameClass nc) {
      nc.childrenAccept(this);
      return null;
    }

    public Object visitName(NameNameClass nc) {
      String ns = resolveNamespace(nc.getNamespaceUri(), inheritedNamespace);
      NamespaceUsage usage = (NamespaceUsage)namespaceUsageMap.get(ns);
      if (usage == null) {
        usage = new NamespaceUsage();
        namespaceUsageMap.put(ns, usage);
        if (!ns.equals("") || isRoot)
          lookupTargetNamespace(ns);
      }
      if (isElement)
        usage.elementCount++;
      else
        usage.attributeCount++;
      String prefix = nc.getPrefix();
      if (prefix != null) {
        Map prefixUsageMap = (Map)namespacePrefixUsageMap.get(ns);
        if (prefixUsageMap == null) {
          prefixUsageMap = new HashMap();
          namespacePrefixUsageMap.put(ns, prefixUsageMap);
        }
        PrefixUsage prefixUsage = (PrefixUsage)prefixUsageMap.get(prefix);
        if (prefixUsage == null) {
          prefixUsage = new PrefixUsage();
          prefixUsageMap.put(prefix, prefixUsage);
        }
        prefixUsage.count++;
      }
      return null;
    }

    public Object visitComposite(CompositePattern p) {
      p.childrenAccept(this);
      return null;
    }

    public Object visitUnary(UnaryPattern p) {
      return p.getChild().accept(this);
    }

    public Object visitDefine(DefineComponent c) {
      c.getBody().accept(this);
      return null;
    }

    public Object visitDiv(DivComponent c) {
      c.componentsAccept(this);
      return null;
    }

    String selectTargetNamespace(GrammarPattern p, String ns) {
      this.inheritedNamespace = ns;
      p.componentsAccept(this);
      Map.Entry best = null;
      for (Iterator iter = namespaceUsageMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry tem = (Map.Entry)iter.next();
        if (best == null
            || NamespaceUsage.isBetter((NamespaceUsage)tem.getValue(),
                                       (NamespaceUsage)best.getValue()))
          best = tem;
      }
      namespaceUsageMap.clear();
      if (best == null)
        return null;
      String targetNamespace = (String)best.getKey();
      // for "" case
      lookupTargetNamespace(targetNamespace);
      return targetNamespace;
    }

    void assignPrefixes() {
      Set usedPrefixes = new HashSet();
      for (Iterator iter = targetNamespaceMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String ns = (String)entry.getKey();
        if (!ns.equals("")) {
          Map prefixUsageMap = (Map)namespacePrefixUsageMap.get(ns);
          if (prefixUsageMap != null) {
            Map.Entry best = null;
            for (Iterator entryIter = prefixUsageMap.entrySet().iterator(); entryIter.hasNext();) {
              Map.Entry tem = (Map.Entry)entryIter.next();
              if (best == null
                      || ((PrefixUsage)tem.getValue()).count > ((PrefixUsage)best.getValue()).count
                      && !usedPrefixes.contains(tem.getKey()))
                best = tem;
            }
            if (best != null) {
              String prefix = (String)best.getKey();
              ((TargetNamespace)entry.getValue()).prefix = prefix;
              usedPrefixes.add(prefix);
            }
          }
        }
      }
      int n = 1;
      for (Iterator iter = targetNamespaceMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String ns = (String)entry.getKey();
        if (!ns.equals("")) {
          if (lookupTargetNamespace(ns).prefix == null) {
            for (;;) {
              String prefix = "ns" + Integer.toString(n++);
              if (!usedPrefixes.contains(prefix)) {
                usedPrefixes.add(prefix);
                ((TargetNamespace)entry.getValue()).prefix = prefix;
                break;
              }
            }
          }
        }
      }
    }

  }

  SchemaInfo(SchemaCollection sc, ErrorReporter er) {
    this.sc = sc;
    this.er = er;
    forceGrammar();
    grammar = (GrammarPattern)sc.getMainSchema();
    grammar.componentsAccept(new GrammarVisitor(OutputDirectory.MAIN, ""));
    grammar.componentsAccept(new RootMarker());
    assignTargetNamespaces();
    chooseRootSchemas();
  }

  void forceGrammar() {
    sc.setMainSchema(convertToGrammar(sc.getMainSchema()));
    // TODO convert other schemas
  }

  void assignTargetNamespaces() {
    TargetNamespaceSelector selector = new TargetNamespaceSelector();
    lookupSourceUri(OutputDirectory.MAIN).targetNamespace
            = selector.selectTargetNamespace(grammar, getInheritedNamespace(OutputDirectory.MAIN));
    for (Iterator iter = getSourceUris().iterator(); iter.hasNext();) {
      String sourceUri = (String)iter.next();
      GrammarPattern p = getSchema(sourceUri);
      lookupSourceUri(sourceUri).targetNamespace
        = selector.selectTargetNamespace(p, getInheritedNamespace(sourceUri));
    }
    // TODO maybe use info from <start> to select which targetNamespace of included schemas to use
    String ns = filterUpTargetNamespace(OutputDirectory.MAIN);
    if (ns == null) {
      lookupTargetNamespace("");
      lookupSourceUri(OutputDirectory.MAIN).targetNamespace = "";
      ns = "";
    }
    inheritDownTargetNamespace(OutputDirectory.MAIN, ns);
    selector.assignPrefixes();
  }

  private String filterUpTargetNamespace(String sourceUri) {
    String ns = getTargetNamespace(sourceUri);
    if (ns != null)
      return ns;
    List includes = lookupSourceUri(sourceUri).includes;
    if (includes.size() == 0)
      return null;
    Map occurMap = new HashMap();
    for (Iterator iter = includes.iterator(); iter.hasNext();) {
      String tem = filterUpTargetNamespace((String)iter.next());
      if (tem != null) {
        Integer count = (Integer)occurMap.get(tem);
        occurMap.put(tem, new Integer(count == null ? 1 : count.intValue() + 1));
      }
    }
    Map.Entry best = null;
    for (Iterator iter = occurMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry tem = (Map.Entry)iter.next();
      if (best == null || ((Integer)tem.getValue()).intValue() > ((Integer)best.getValue()).intValue())
        best = tem;
    }
    if (best == null)
      return null;
    ns = (String)best.getKey();
    lookupSourceUri(sourceUri).targetNamespace = ns;
    return ns;
  }

  private void inheritDownTargetNamespace(String sourceUri, String targetNamespace) {
    for (Iterator iter = lookupSourceUri(sourceUri).includes.iterator(); iter.hasNext();) {
      String uri = (String)iter.next();
      String ns = lookupSourceUri(uri).targetNamespace;
      if (ns == null) {
        ns = targetNamespace;
        lookupSourceUri(uri).targetNamespace = ns;
      }
      inheritDownTargetNamespace(uri, ns);
    }
  }

  String getInheritedNamespace(String sourceUri) {
    return lookupSourceUri(sourceUri).inheritedNamespace;
  }

  private void chooseRootSchemas() {
    for (Iterator iter = targetNamespaceMap.entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      String ns = (String)entry.getKey();;
      List list = new Vector();
      findRootSchemas(OutputDirectory.MAIN, ns, list);
      if (list.size() == 1)
        ((TargetNamespace)entry.getValue()).rootSchema = (String)list.get(0);
      else {
        String sourceUri = generateSourceUri(ns);
        lookupSourceUri(sourceUri).includes.addAll(list);
        lookupSourceUri(sourceUri).targetNamespace = ns;
        ((TargetNamespace)entry.getValue()).rootSchema = sourceUri;
        generatedSourceUris.add(sourceUri);
      }
    }
  }

  private String generateSourceUri(String ns) {
    // TODO add method to OutputDirectory to do this properly
    if (ns.equals(""))
      return "local";
    else
      return "/" + getPrefix(ns);
  }

  private void findRootSchemas(String sourceUri, String ns, List list) {
    if (getTargetNamespace(sourceUri).equals(ns))
      list.add(sourceUri);
    else {
      for (Iterator iter = lookupSourceUri(sourceUri).includes.iterator(); iter.hasNext();)
        findRootSchemas((String)iter.next(), ns, list);
    }
  }

  String getRootSchema(String targetNamespace) {
    return lookupTargetNamespace(targetNamespace).rootSchema;
  }

  List effectiveIncludes(String sourceUri) {
    String ns = getTargetNamespace(sourceUri);
    List list = new Vector();
    for (Iterator iter = lookupSourceUri(sourceUri).includes.iterator(); iter.hasNext();)
      findRootSchemas((String)iter.next(), ns, list);
    return list;
  }

  List getGeneratedSourceUris() {
    return generatedSourceUris;
  }

  GrammarPattern convertToGrammar(Pattern p) {
    if (p instanceof GrammarPattern)
      return (GrammarPattern)p;
    GrammarPattern g = new GrammarPattern();
    g.setSourceLocation(p.getSourceLocation());
    DefineComponent dc = new DefineComponent(DefineComponent.START, p);
    dc.setSourceLocation(p.getSourceLocation());
    g.getComponents().add(dc);
    return g;
  }

  GrammarPattern getGrammar() {
    return grammar;
  }

  GrammarPattern getSchema(String sourceUri) {
    return (GrammarPattern)sc.getSchemas().get(sourceUri);
  }

  String getTargetNamespace(String sourceUri) {
    return lookupSourceUri(sourceUri).targetNamespace;
  }

  Set getSourceUris() {
    return sc.getSchemas().keySet();
  }

  ChildType getChildType(Pattern p) {
    ChildType ct = (ChildType)childTypeMap.get(p);
    if (ct == null) {
      ct = (ChildType)p.accept(childTypeVisitor);
      childTypeMap.put(p, ct);
    }
    return ct;
  }

  Pattern getBody(RefPattern p) {
    return (Pattern)defineMap.get(p.getName());
  }

  String qualifyName(RefPattern p) {
    String name = p.getName();
    return qualifyName(getTargetNamespace((String)whereDefinedMap.get(name)), name);
  }

  String qualifyName(NameNameClass nc, String sourceUri) {
    return qualifyName(resolveNamespace(nc.getNamespaceUri(), getInheritedNamespace(sourceUri)),
                       nc.getLocalName());
  }

  String qualifyName(String ns, String localName) {
    if (ns.equals(""))
      return localName;
    return getPrefix(ns) + ":" + localName;
  }

  boolean isGlobal(ElementPattern p) {
    return rootElements.contains(p);
  }

  Set getTargetNamespaces() {
    return targetNamespaceMap.keySet();
  }

  String getPrefix(String targetNamespace) {
    return lookupTargetNamespace(targetNamespace).prefix;
  }

  static String resolveNamespace(String ns, String inheritedNamespace) {
    if (ns == NameNameClass.INHERIT_NS)
      return inheritedNamespace;
    return ns;
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
}