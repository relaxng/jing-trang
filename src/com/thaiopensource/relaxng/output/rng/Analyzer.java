package com.thaiopensource.relaxng.output.rng;

import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.NsNameNameClass;

import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

class Analyzer extends AbstractVisitor {
  public Object visitDefine(DefineComponent c) {
    return c.getBody().accept(this);
  }

  public Object visitDiv(DivComponent c) {
    return visitContainer(c);
  }

  public Object visitInclude(IncludeComponent c) {
    return visitContainer(c);
  }

  public Object visitGrammar(GrammarPattern p) {
    return visitContainer(p);
  }

  public Object visitContainer(Container c) {
    List list = c.getComponents();
    for (int i = 0, len = list.size(); i < len; i++)
      ((Component)list.get(i)).accept(this);
    return null;
  }

  public Object visitUnary(UnaryPattern p) {
    return p.getChild().accept(this);
  }

  public Object visitComposite(CompositePattern p) {
    List list = p.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      ((Pattern)list.get(i)).accept(this);
    return null;
  }

  public Object visitNameClassed(NameClassedPattern p) {
    p.getNameClass().accept(this);
    return visitUnary(p);
  }

  public Object visitChoice(ChoiceNameClass nc) {
    List list = nc.getChildren();
    for (int i = 0, len = list.size(); i < len; i++)
      ((NameClass)list.get(i)).accept(this);
    return null;
  }

  public Object visitValue(ValuePattern p) {
    if (!p.getType().equals("token") || !p.getDatatypeLibrary().equals(""))
      noteDatatypeLibrary(p.getDatatypeLibrary());
    for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
      Map.Entry entry = (Map.Entry)iter.next();
      noteNs((String)entry.getKey(), (String)entry.getValue());
    }
    return null;
  }

  public Object visitData(DataPattern p) {
    noteDatatypeLibrary(p.getDatatypeLibrary());
    Pattern except = p.getExcept();
    if (except != null)
      except.accept(this);
    return null;
  }

  public Object visitName(NameNameClass nc) {
    noteNs(nc.getPrefix(), nc.getNamespaceUri());
    return null;
  }

  public Object visitAnyName(AnyNameNameClass nc) {
    NameClass except = nc.getExcept();
    if (except != null)
      except.accept(this);
    return null;
  }

  public Object visitNsName(NsNameNameClass nc) {
    noteNs(null, nc.getNs());
    NameClass except = nc.getExcept();
    if (except != null)
      except.accept(this);
    return null;
  }

  private String datatypeLibrary = null;
  private final Map prefixMap = new HashMap();
  private boolean haveInherit = false;

  private void noteDatatypeLibrary(String uri) {
    if (datatypeLibrary == null || datatypeLibrary.length() == 0)
      datatypeLibrary = uri;
  }

  private void noteNs(String prefix, String ns) {
    if (ns == NameClass.INHERIT_NS) {
      haveInherit = true;
      return;
    }
    if (ns.length() == 0)
      return;
    if (prefix == null)
      prefix = "";
    prefixMap.put(prefix, ns);
  }

  Map getPrefixMap() {
    if (haveInherit)
      prefixMap.remove("");
    return prefixMap;
  }

  String getDatatypeLibrary() {
    return datatypeLibrary;
  }

}
