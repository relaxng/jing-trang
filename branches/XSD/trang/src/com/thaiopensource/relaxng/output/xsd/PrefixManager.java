package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.UnaryPattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class PrefixManager {

  private final Map prefixMap = new HashMap();
  private final Set usedPrefixes = new HashSet();
  private int nextGenIndex = 1;

  static class PrefixUsage {
    int count;
  }

  static class PrefixSelector extends AbstractVisitor {
    private final SchemaInfo si;
    private String inheritedNamespace;
    private final Map namespacePrefixUsageMap = new HashMap();

    PrefixSelector(SchemaInfo si) {
      this.si = si;
      this.inheritedNamespace = "";
    }

    public Object visitElement(ElementPattern p) {
      p.getNameClass().accept(this);
      p.getChild().accept(this);
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      return p.getNameClass().accept(this);
    }

    public Object visitChoice(ChoiceNameClass nc) {
      nc.childrenAccept(this);
      return null;
    }

    public Object visitName(NameNameClass nc) {
      String ns = nc.getNamespaceUri();
      if (ns == NameNameClass.INHERIT_NS)
        ns = inheritedNamespace;
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

    void assignPrefixes(Map prefixMap, Set usedPrefixes) {
      for (Iterator iter = namespacePrefixUsageMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String ns = (String)entry.getKey();
        if (!ns.equals("")) {
          Map prefixUsageMap = (Map)(entry.getValue());
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
              prefixMap.put(ns, prefix);
              usedPrefixes.add(prefix);
            }
          }
        }
      }
    }
  }

  PrefixManager(SchemaInfo si) {
    new PrefixSelector(si).assignPrefixes(prefixMap, usedPrefixes);
  }

  String getPrefix(String namespace) {
    String prefix = (String)prefixMap.get(namespace);
    if (prefix == null) {
      do {
        prefix = "ns" + Integer.toString(nextGenIndex++);
      } while (!usedPrefixes.contains(prefix));
      usedPrefixes.add(prefix);
      prefixMap.put(namespace, prefix);
    }
    return prefix;
  }
}
