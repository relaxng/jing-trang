package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.edit.PatternVisitor;
import com.thaiopensource.relaxng.edit.ElementPattern;
import com.thaiopensource.relaxng.edit.AttributePattern;
import com.thaiopensource.relaxng.edit.OneOrMorePattern;
import com.thaiopensource.relaxng.edit.ZeroOrMorePattern;
import com.thaiopensource.relaxng.edit.OptionalPattern;
import com.thaiopensource.relaxng.edit.InterleavePattern;
import com.thaiopensource.relaxng.edit.GroupPattern;
import com.thaiopensource.relaxng.edit.ChoicePattern;
import com.thaiopensource.relaxng.edit.GrammarPattern;
import com.thaiopensource.relaxng.edit.ExternalRefPattern;
import com.thaiopensource.relaxng.edit.RefPattern;
import com.thaiopensource.relaxng.edit.ParentRefPattern;
import com.thaiopensource.relaxng.edit.ValuePattern;
import com.thaiopensource.relaxng.edit.DataPattern;
import com.thaiopensource.relaxng.edit.MixedPattern;
import com.thaiopensource.relaxng.edit.ListPattern;
import com.thaiopensource.relaxng.edit.TextPattern;
import com.thaiopensource.relaxng.edit.EmptyPattern;
import com.thaiopensource.relaxng.edit.NotAllowedPattern;
import com.thaiopensource.relaxng.edit.UnaryPattern;
import com.thaiopensource.relaxng.edit.CompositePattern;
import com.thaiopensource.relaxng.edit.Pattern;
import com.thaiopensource.relaxng.edit.AbstractRefPattern;
import com.thaiopensource.relaxng.edit.Annotated;
import com.thaiopensource.relaxng.edit.NameClassedPattern;
import com.thaiopensource.relaxng.edit.NameClassVisitor;
import com.thaiopensource.relaxng.edit.NameClass;
import com.thaiopensource.relaxng.edit.Component;
import com.thaiopensource.relaxng.edit.ComponentVisitor;
import com.thaiopensource.relaxng.edit.Param;
import com.thaiopensource.relaxng.edit.ChoiceNameClass;
import com.thaiopensource.relaxng.edit.DivComponent;
import com.thaiopensource.relaxng.edit.AnyNameNameClass;
import com.thaiopensource.relaxng.edit.IncludeComponent;
import com.thaiopensource.relaxng.edit.NsNameNameClass;
import com.thaiopensource.relaxng.edit.DefineComponent;
import com.thaiopensource.relaxng.edit.NameNameClass;
import com.thaiopensource.relaxng.edit.OpenNameClass;
import com.thaiopensource.relaxng.edit.Container;
import com.thaiopensource.relaxng.parse.sax.SAXParseable;

import java.util.List;
import java.util.Stack;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.io.Writer;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;

/*
Specify indent
Specify encoding
Annotations
name class to name attribute
avoid unnecessary groups
Use default namespace decl
Use top-level datatypeLibrary
*/
public class XmlOutput implements PatternVisitor, NameClassVisitor, ComponentVisitor {

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
    private Map namespaceDecls;

    public XmlWriter(String lineSep, Writer w, Map namespaceDecls) {
      this.lineSep = lineSep;
      this.w = w;
      this.namespaceDecls = namespaceDecls;
    }

    void startElement(String name) {
      if (inStartTag) {
        maybeWriteNamespaceDecls();
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
        maybeWriteNamespaceDecls();
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
        maybeWriteNamespaceDecls();
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

    private void maybeWriteNamespaceDecls() {
      if (level != 1)
        return;
      for (Iterator iter = namespaceDecls.entrySet().iterator(); iter.hasNext();) {
        Map.Entry entry = (Map.Entry)iter.next();
        String prefix = (String)entry.getKey();
        String name = prefix.length() == 0 ? "xmlns" : "xmlns:" + prefix;
        attribute(name, (String)entry.getValue());
      }
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

  private XmlWriter xw;

  private XmlOutput(XmlWriter xw) {
    this.xw = xw;
  }

  public Object visitElement(ElementPattern p) {
    xw.startElement("element");
    innerAnnotations(p);
    p.getNameClass().accept(this);
    p.getChild().accept(this);
    end(p);
    return null;
  }

  public Object visitAttribute(AttributePattern p) {
    xw.startElement("attribute");
    innerAnnotations(p);
    p.getNameClass().accept(this);
    p.getChild().accept(this);
    end(p);
    return null;
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
    if (p.getNs() != NameClass.INHERIT_NS)
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
    xw.attribute("type", p.getType());
    xw.attribute("datatypeLibrary", p.getDatatypeLibrary());
    innerAnnotations(p);
    xw.text(p.getValue());
    end(p);
    return null;
  }

  public Object visitData(DataPattern p) {
    xw.startElement("data");
    xw.attribute("type", p.getType());
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
    if (except != null)
      except.accept(this);
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
    p.getChild().accept(this);
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
    if (nc.getNs() != NameClass.INHERIT_NS)
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
    except.accept(this);
    xw.endElement();
  }

  public Object visitName(NameNameClass nc) {
    xw.startElement("name");
    if (nc.getNamespaceUri() != NameClass.INHERIT_NS)
      xw.attribute("ns", nc.getNamespaceUri());
    innerAnnotations(nc);
    xw.text(nc.getLocalName());
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
    c.getBody().accept(this);
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

  static public void output(Pattern p, String file) throws IOException {
    try {
      Writer w = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(file)));
      Map map = new HashMap();
      map.put("", SAXParseable.URI);
      p.accept(new XmlOutput(new XmlWriter(System.getProperty("line.separator"), w, map)));
      w.flush();
    }
    catch (WrappedException e) {
      throw (IOException)e.getCause();
    }
  }
}
