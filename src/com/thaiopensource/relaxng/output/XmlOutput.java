package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.edit.AbstractRefPattern;
import com.thaiopensource.relaxng.edit.AbstractVisitor;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.OpenNameClass;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/*
Specify indent
Specify encoding
Annotations
Multiple files
*/
public class XmlOutput  {

  static class WrappedException extends RuntimeException {
    private Throwable cause;

    public Throwable getCause() {
      return cause;
    }

    WrappedException(Throwable cause) {
      this.cause = cause;
    }
  }

  static class XmlWriter {
    private String lineSep;
    private Writer w;
    private Stack tagStack = new Stack();
    private boolean inStartTag = false;
    private boolean inText = false;
    private int level = 0;
    private String[] topLevelAttributes;

    public XmlWriter(String lineSep, Writer w, String[] topLevelAttributes) {
      this.lineSep = lineSep;
      this.w = w;
      this.topLevelAttributes = topLevelAttributes;
    }

    void startElement(String name) {
      if (inStartTag) {
        maybeWriteTopLevelAttributes();
        inStartTag = false;
        write(">");
        newline();
      }
      if (inText)
        inText = false;
      else
        indent();
      write('<');
      write(name);
      tagStack.push(name);
      inStartTag = true;
      level++;
    }

    void endElement() {
      if (inStartTag) {
        maybeWriteTopLevelAttributes();
        level--;
        inStartTag = false;
        tagStack.pop();
        write("/>");
      }
      else {
        level--;
        if (inText)
          inText = false;
        else
          indent();
        write("</");
        write((String)tagStack.pop());
        write(">");
      }
      newline();
    }

    void attribute(String name, String value) {
      if (!inStartTag)
        throw new IllegalStateException("attribute outside of start-tag");
      write(' ');
      write(name);
      write('=');
      write('"');
      data(value);
      write('"');
    }

    void text(String s) {
      if (s.length() == 0)
        return;
      if (inStartTag) {
        maybeWriteTopLevelAttributes();
        inStartTag = false;
        write(">");
      }
      data(s);
      inText = true;
    }

    void data(String s) {
      int n = s.length();
      for (int i = 0; i < n; i++) {
        switch (s.charAt(i)) {
        case '<':
          write("&lt;");
          break;
        case '>':
          write("&gt;");
          break;
        case '&':
          write("&amp;");
          break;
        case '\r':
          write("&#xD;");
          break;
        case '\n':
          write(lineSep);
          break;
        default:
          write(s.charAt(i));
          break;
        }
      }
    }

    private void indent() {
      for (int i = 0; i < level; i++)
        write("  ");
    }

    private void newline() {
      write(lineSep);
    }

    private void maybeWriteTopLevelAttributes() {
      if (level != 1)
        return;
      for (int i = 0; i < topLevelAttributes.length; i += 2)
        attribute(topLevelAttributes[i], topLevelAttributes[i + 1]);
    }

    private void write(String s) {
      try {
        w.write(s);
      }
      catch (IOException e) {
        throw new WrappedException(e);
      }
    }

    private void write(char c) {
      try {
        w.write(c);
      }
      catch (IOException e) {
        throw new WrappedException(e);
      }
    }

  }

  static class Output implements PatternVisitor, NameClassVisitor, ComponentVisitor {
    private XmlWriter xw;
    private String datatypeLibrary;
    private Map prefixMap;

    private Output(String lineSep, Writer w, String datatypeLibrary, Map prefixMap) {
      this.xw = xw;
      this.datatypeLibrary = datatypeLibrary;
      this.prefixMap = prefixMap;
      this.xw = new XmlWriter(lineSep, w, getTopLevelAttributes());
    }

    String[] getTopLevelAttributes() {
      int nAtts = prefixMap.size();
      if (datatypeLibrary != null)
        nAtts += 1;
      String[] atts = new String[nAtts * 2];
      int i = 0;
      for (Iterator iter = prefixMap.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String prefix = (String)entry.getKey();
        if (prefix.equals(""))
          atts[i++] = "ns";
        else
          atts[i++] = "xmlns:" + prefix;
        atts[i++] = (String)entry.getValue();
      }
      if (datatypeLibrary != null) {
        atts[i++] = "datatypeLibrary";
        atts[i++] = datatypeLibrary;
      }
      return atts;
    }

    public Object visitElement(ElementPattern p) {
      xw.startElement("element");
      boolean usedNameAtt = tryNameAttribute(p.getNameClass(), false);
      innerAnnotations(p);
      if (!usedNameAtt)
        p.getNameClass().accept(this);
      implicitGroup(p.getChild());
      end(p);
      return null;
    }

    public Object visitAttribute(AttributePattern p) {
      xw.startElement("attribute");
      boolean usedNameAtt = tryNameAttribute(p.getNameClass(), true);
      innerAnnotations(p);
      if (!usedNameAtt)
        p.getNameClass().accept(this);
      Pattern child = p.getChild();
      if (!(child instanceof TextPattern) || hasAnnotations(child))
        child.accept(this);
      end(p);
      return null;
    }

    boolean tryNameAttribute(NameClass nc, boolean isAttribute) {
      if (hasAnnotations(nc))
        return false;
      if (!(nc instanceof NameNameClass))
        return false;
      NameNameClass nnc = (NameNameClass)nc;
      String ns = nnc.getNamespaceUri();
      if (ns == NameClass.INHERIT_NS) {
        if (isAttribute)
          return false;
        xw.attribute("name", nnc.getLocalName());
        return true;
      }
      if (ns.length() == 0) {
        if (!isAttribute)
          return false;
        xw.attribute("name", nnc.getLocalName());
        return true;
      }
      String prefix = nnc.getPrefix();
      if (prefix == null) {
        if (!ns.equals(prefixMap.get("")))
          return false;
        xw.attribute("name", nnc.getLocalName());
      }
      else {
        if (!ns.equals(prefixMap.get(prefix)))
          xw.attribute("xmlns:" + prefix, ns);
        xw.attribute("name", prefix + ":" + nnc.getLocalName());
      }
      return true;
    }

    public Object visitOneOrMore(OneOrMorePattern p) {
      return visitUnary("oneOrMore", p);
    }

    public Object visitZeroOrMore(ZeroOrMorePattern p) {
      return visitUnary("zeroOrMore", p);
    }

    public Object visitOptional(OptionalPattern p) {
      return visitUnary("optional", p);
    }

    public Object visitInterleave(InterleavePattern p) {
      return visitComposite("interleave", p);
    }

    public Object visitGroup(GroupPattern p) {
      return visitComposite("group", p);
    }

    public Object visitChoice(ChoicePattern p) {
      return visitComposite("choice", p);
    }

    public Object visitGrammar(GrammarPattern p) {
      xw.startElement("grammar");
      finishContainer(p, p);
      return null;
    }

    public Object visitExternalRef(ExternalRefPattern p) {
      xw.startElement("externalRef");
      xw.attribute("href", p.getHref());
      if (p.getNs() != NameClass.INHERIT_NS
          && !p.getNs().equals(prefixMap.get("")))
        xw.attribute("ns", p.getNs());
      innerAnnotations(p);
      end(p);
      return null;
    }

    public Object visitRef(RefPattern p) {
      return visitAbstractRef("ref", p);
    }

    public Object visitParentRef(ParentRefPattern p) {
      return visitAbstractRef("parentRef", p);
    }

    public Object visitAbstractRef(String name, AbstractRefPattern p) {
      xw.startElement(name);
      xw.attribute("name", p.getName());
      innerAnnotations(p);
      end(p);
      return null;
    }

    public Object visitValue(ValuePattern p) {
      xw.startElement("value");
      if (!p.getType().equals("token")
          || !p.getDatatypeLibrary().equals("")) {
        xw.attribute("type", p.getType());
        if (!p.getDatatypeLibrary().equals(datatypeLibrary))
          xw.attribute("datatypeLibrary", p.getDatatypeLibrary());
        for (Iterator iter = p.getPrefixMap().entrySet().iterator(); iter.hasNext();) {
          Map.Entry entry = (Map.Entry)iter.next();
          String prefix = (String)entry.getKey();
          String ns = (String)entry.getValue();
          if (ns != NameClass.INHERIT_NS && !ns.equals(prefixMap.get(prefix)))
            xw.attribute(prefix.length() == 0 ? "ns" : "xmlns:" + prefix,
                         ns);
        }
      }
      innerAnnotations(p);
      xw.text(p.getValue());
      end(p);
      return null;
    }

    public Object visitData(DataPattern p) {
      xw.startElement("data");
      xw.attribute("type", p.getType());
      if (!p.getDatatypeLibrary().equals(datatypeLibrary))
        xw.attribute("datatypeLibrary", p.getDatatypeLibrary());
      innerAnnotations(p);
      List list = p.getParams();
      for (int i = 0, len = list.size(); i < len; i++) {
        Param param = (Param)list.get(i);
        xw.startElement("param");
        xw.attribute("name", param.getName());
        innerAnnotations(p);
        xw.text(param.getValue());
        end(param);
      }
      Pattern except = p.getExcept();
      if (except != null) {
        xw.startElement("except");
        implicitChoice(except);
        xw.endElement();
      }
      end(p);
      return null;
    }

    public Object visitMixed(MixedPattern p) {
      return visitUnary("mixed", p);
    }

    public Object visitList(ListPattern p) {
      return visitUnary("list", p);
    }

    public Object visitText(TextPattern p) {
      return visitNullary("text", p);
    }

    public Object visitEmpty(EmptyPattern p) {
      return visitNullary("empty", p);
    }

    public Object visitNotAllowed(NotAllowedPattern p) {
      return visitNullary("notAllowed", p);
    }

    public Object visitNullary(String name, Pattern p) {
      xw.startElement(name);
      innerAnnotations(p);
      end(p);
      return null;
    }

    public Object visitUnary(String name, UnaryPattern p) {
      xw.startElement(name);
      innerAnnotations(p);
      implicitGroup(p.getChild());
      end(p);
      return null;
    }

    public Object visitComposite(String name, CompositePattern p) {
      xw.startElement(name);
      innerAnnotations(p);
      List list = p.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Pattern)list.get(i)).accept(this);
      end(p);
      return null;
    }

    public Object visitChoice(ChoiceNameClass nc) {
      xw.startElement("choice");
      innerAnnotations(nc);
      List list = nc.getChildren();
      for (int i = 0, len = list.size(); i < len; i++)
        ((NameClass)list.get(i)).accept(this);
      end(nc);
      return null;
    }

    public Object visitAnyName(AnyNameNameClass nc) {
      xw.startElement("anyName");
      innerAnnotations(nc);
      visitExcept(nc);
      end(nc);
      return null;
    }

    public Object visitNsName(NsNameNameClass nc) {
      xw.startElement("nsName");
      if (nc.getNs() != NameClass.INHERIT_NS
          && !nc.getNs().equals(prefixMap.get("")))
        xw.attribute("ns", nc.getNs());
      innerAnnotations(nc);
      visitExcept(nc);
      end(nc);
      return null;
    }

    private void visitExcept(OpenNameClass onc) {
      NameClass except = onc.getExcept();
      if (except == null)
        return;
      xw.startElement("except");
      implicitChoice(except);
      xw.endElement();
    }

    public Object visitName(NameNameClass nc) {
      xw.startElement("name");
      String ns = nc.getNamespaceUri();
      if (ns == NameClass.INHERIT_NS) {
        innerAnnotations(nc);
        xw.text(nc.getLocalName());
      }
      else {
        String prefix = nc.getPrefix();
        if (prefix == null || ns.length() == 0) {
          if (!ns.equals(prefixMap.get("")))
            xw.attribute("ns", ns);
          innerAnnotations(nc);
          xw.text(nc.getLocalName());
        }
        else {
          if (!ns.equals(prefixMap.get(prefix)))
            xw.attribute("xmlns:" + prefix, ns);
          innerAnnotations(nc);
          xw.text(prefix + ":" + nc.getLocalName());
        }
      }
      end(nc);
      return null;
    }

    public Object visitDefine(DefineComponent c) {
      String name = c.getName();
      if (name == c.START)
        xw.startElement("start");
      else {
        xw.startElement("define");
        xw.attribute("name", name);
      }
      if (c.getCombine() != null)
        xw.attribute("combine", c.getCombine().toString());
      innerAnnotations(c);
      implicitGroup(c.getBody());
      end(c);
      return null;
    }

    public Object visitDiv(DivComponent c) {
      xw.startElement("div");
      finishContainer(c, c);
      return null;
    }

    public Object visitInclude(IncludeComponent c) {
      xw.startElement("include");
      finishContainer(c, c);
      return null;
    }

    private void finishContainer(Annotated subject, Container container) {
      innerAnnotations(subject);
      List list = container.getComponents();
      for (int i = 0, len = list.size(); i < len; i++)
        ((Component)list.get(i)).accept(this);
      end(subject);
    }

    public void innerAnnotations(Annotated subject) {
      // XXX
    }

    public void outerAnnotations(Annotated subject) {
      // XXX
    }

    private void end(Annotated subject) {
      xw.endElement();
      outerAnnotations(subject);
    }

    private void implicitGroup(Pattern p) {
      if (!hasAnnotations(p) && p instanceof GroupPattern) {
        List list = ((GroupPattern)p).getChildren();
        for (int i = 0, len = list.size(); i < len; i++)
          ((Pattern)list.get(i)).accept(this);
      }
      else
        p.accept(this);
    }

    private void implicitChoice(Pattern p) {
      if (!hasAnnotations(p) && p instanceof ChoicePattern) {
        List list = ((ChoicePattern)p).getChildren();
        for (int i = 0, len = list.size(); i < len; i++)
          ((Pattern)list.get(i)).accept(this);
      }
      else
        p.accept(this);
    }

    private void implicitChoice(NameClass nc) {
      if (!hasAnnotations(nc) && nc instanceof ChoiceNameClass) {
        List list = ((ChoiceNameClass)nc).getChildren();
        for (int i = 0, len = list.size(); i < len; i++)
          ((NameClass)list.get(i)).accept(this);
      }
      else
        nc.accept(this);
    }

    private static boolean hasAnnotations(Annotated subject) {
      return (!subject.getAttributeAnnotations().isEmpty()
              || !subject.getChildElementAnnotations().isEmpty()
              || !subject.getFollowingElementAnnotations().isEmpty());
    }
  }

  static class Analyzer extends AbstractVisitor {
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

  static public void output(Pattern p, String file) throws IOException {
    try {
      Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)));
      Analyzer analyzer = new Analyzer();
      p.accept(analyzer);
      p.accept(new Output(System.getProperty("line.separator"),
                          w,
                          analyzer.getDatatypeLibrary(),
                          analyzer.getPrefixMap()));
      w.flush();
    }
    catch (WrappedException e) {
      throw (IOException)e.getCause();
    }
  }
}
