package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;
import com.thaiopensource.xml.util.WellKnownNamespaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PatternDumper {
  private static final String INTERNAL_NAMESPACE = "http://www.thaiopensource.com/relaxng/internal";
  private boolean startTagOpen = false;
  private final ArrayList<String> tagStack = new ArrayList<String>();
  private final StringBuilder buf;
  private int level = 0;
  private boolean suppressIndent = false;
  private final List<ElementPattern> patternList = new ArrayList<ElementPattern>();
  private final Map<String, Integer> localNamePatternCount = new HashMap<String, Integer>();
  private int otherPatternCount;
  private final Map<ElementPattern, String> patternNameMap = new HashMap<ElementPattern, String>();

  private final PatternFunction<VoidValue> dumper = new Dumper();
  private final PatternFunction<VoidValue> elementDumper = new ElementDumper();
  private final PatternFunction<VoidValue> optionalDumper = new OptionalDumper();
  private final PatternFunction<VoidValue> groupDumper = new GroupDumper();
  private final PatternFunction<VoidValue> choiceDumper = new ChoiceDumper();
  private final PatternFunction<VoidValue> interleaveDumper = new InterleaveDumper();
  private final NameClassVisitor nameClassDumper = new NameClassDumper();
  private final NameClassVisitor choiceNameClassDumper = new ChoiceNameClassDumper();

  static public String toString(Pattern p) {
    return new PatternDumper().dump(p).getSchema();
  }

  private PatternDumper() {
    buf = new StringBuilder();
  }

  private String getSchema() {
    return buf.toString();
  }

  private PatternDumper dump(Pattern p) {
    write("<?xml version=\"1.0\"?>");
    startElement("grammar");
    attribute("xmlns", WellKnownNamespaces.RELAX_NG);
    startElement("start");
    p.apply(dumper);
    endElement();
    for (int i = 0; i < patternList.size(); i++) {
      startElement("define");
      ElementPattern tem = patternList.get(i);
      attribute("name", getName(tem));
      tem.apply(elementDumper);
      endElement();
    }
    endElement();
    write('\n');
    return this;
  }

  private String getName(ElementPattern p) {
    String name = patternNameMap.get(p);
    // patterns for element patterns with local name X are named: X, X_2, X_3
    // however if X is of the form Y_N (N > 0), then the patterns are named: X_1, X_2, X_3
    // for element patterns with complex name classes, the patterns are named: _1, _2, _3
    if (name == null) {
      NameClass nc = p.getNameClass();
      if (nc instanceof SimpleNameClass) {
        String localName = ((SimpleNameClass)nc).getName().getLocalName();
        Integer i = localNamePatternCount.get(localName);
        if (i == null) {
          i = 1;
          name = localName;
          // see if the name can be the same as one of our generated names
          int u = name.lastIndexOf('_');
          if (u >= 0) {
            try {
              if (Integer.valueOf(name.substring(u + 1, name.length())) > 0)
                // it can, so transform it so that it cannot
                name += "_1";
            }
            catch (NumberFormatException e) {
              // not a number, so cannot be the same as one of our generated names
            }
          }
        }
        else
          name = localName + "_" + ++i;
        localNamePatternCount.put(localName, i);
      }
      else
        name = "_" + ++otherPatternCount;
      patternList.add(p);
      patternNameMap.put(p, name);
    }
    return name;
  }

  private void startElement(String name) {
    closeStartTag();
    indent(level);
    write('<');
    write(name);
    push(name);
    startTagOpen = true;
    level++;
  }

  private void closeStartTag() {
    if (startTagOpen) {
      startTagOpen = false;
      write('>');
    }
  }

  private void attribute(String name, String value) {
    write(' ');
    write(name);
    write('=');
    write('"');
    chars(value, true);
    write('"');
  }

  private void data(String str) {
    if (str.length() > 0) {
      closeStartTag();
      chars(str, false);
      suppressIndent = true;
    }
  }

  private void chars(String str, boolean isAttribute) {
    int len = str.length();
    for (int i = 0; i < len; i++) {
      char c = str.charAt(i);
      switch (c) {
      case '&':
	write("&amp;");
	break;
      case '<':
	write("&lt;");
	break;
      case '>':
	write("&gt;");
	break;
      case 0xD:
        write("&#xD;");
        break;
      case 0xA:
        if (isAttribute)
          write("&#xA;");
        else
          write(c);
        break;
      case 0x9:
        if (isAttribute)
          write("&#x9;");
        else
          write(c);
        break;
      case '"':
        if (isAttribute)
          write("&quot;");
        else
          write(c);
        break;
      default:
	write(c);
	break;
      }
    }
  }
      
  private void endElement() {
    --level;
    if (startTagOpen) {
      startTagOpen = false;
      write("/>");
      pop();
    }
    else {
      if (!suppressIndent)
	indent(level);
      write("</");
      write(pop());
      write(">");
    }
    suppressIndent = false;
  }

  private void indent(int level) {
    write('\n');
    for (int i = 0; i < level; i++)
      write("  ");
  }

  private void write(String str) {
    buf.append(str);
  }

  private void write(char c) {
    buf.append(c);
  }

  private void push(String s) {
    tagStack.add(s);
  }

  private String pop() {
    return tagStack.remove(tagStack.size() - 1);
  }

  class Dumper implements PatternFunction<VoidValue> {
    public VoidValue caseEmpty(EmptyPattern p) {
      startElement("empty");
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseNotAllowed(NotAllowedPattern p) {
      startElement("notAllowed");
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseGroup(GroupPattern p) {
      startElement("group");
      p.getOperand1().apply(groupDumper);
      p.getOperand2().apply(groupDumper);
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseInterleave(InterleavePattern p) {
      startElement("interleave");
      p.getOperand1().apply(interleaveDumper);
      p.getOperand2().apply(interleaveDumper);
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseChoice(ChoicePattern p) {
      final Pattern p1 = p.getOperand1();
      final Pattern p2 = p.getOperand2();
      if (p1 instanceof EmptyPattern)
        p2.apply(optionalDumper);
      else if (p2 instanceof EmptyPattern)
        p1.apply(optionalDumper);
      else
        choice(p1, p2);
      return VoidValue.VOID;
    }

    protected void choice(Pattern p1, Pattern p2) {
      startElement("choice");
      p1.apply(choiceDumper);
      p2.apply(choiceDumper);
      endElement();
    }

    public VoidValue caseOneOrMore(OneOrMorePattern p) {
      startElement("oneOrMore");
      p.getOperand().apply(dumper);
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseElement(ElementPattern p) {
      startElement("ref");
      attribute("name", getName(p));
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseAttribute(AttributePattern p) {
      startElement("attribute");
      outputName(p.getNameClass());
      p.getContent().apply(dumper);
      endElement();
      return VoidValue.VOID;
    }

    protected void outputName(NameClass nc) {
      if (nc instanceof SimpleNameClass) {
        Name name = ((SimpleNameClass)nc).getName();
        attribute("name", name.getLocalName());
        attribute("ns", name.getNamespaceUri());
      }
      else
        nc.accept(nameClassDumper);
    }

    public VoidValue caseData(DataPattern p) {
      startData(p);
      endElement();
      return VoidValue.VOID;
    }

    private void startData(DataPattern p) {
      startElement("data");
      final Name dtName = p.getDatatypeName();
      attribute("type", dtName.getLocalName());
      attribute("datatypeLibrary", dtName.getNamespaceUri());
      for (Iterator<String> iter = p.getParams().iterator(); iter.hasNext();) {
        startElement("param");
        attribute("name", iter.next());
        data(iter.next());
        endElement();
      }
    }

    public VoidValue caseDataExcept(DataExceptPattern p) {
      startData(p);
      startElement("except");
      p.getExcept().apply(dumper);
      endElement();
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseValue(ValuePattern p) {
      startElement("value");      
      Name dtName = p.getDatatypeName();
      attribute("type", dtName.getLocalName());
      attribute("datatypeLibrary", dtName.getNamespaceUri());
      String stringValue = p.getStringValue();
      final Object value = p.getValue();
      String ns = "";
      // XXX won't work with a datatypeLibrary that doesn't use Name to implement QName's
      if (value instanceof Name) {
        ns = ((Name)value).getNamespaceUri();
        int colonIndex = stringValue.indexOf(':');
        if (colonIndex < 0)
          stringValue = stringValue.substring(colonIndex + 1, stringValue.length());
      }
      attribute("ns", ns);
      data(stringValue);
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseText(TextPattern p) {
      startElement("text");
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseList(ListPattern p) {
      startElement("list");
      p.getOperand().apply(dumper);
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseRef(RefPattern p) {
      return p.getPattern().apply(this);
    }

    public VoidValue caseAfter(AfterPattern p) {
      startElement("i:after");
      attribute("xmlns:i", INTERNAL_NAMESPACE);
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      endElement();
      return VoidValue.VOID;
    }


    public VoidValue caseError(ErrorPattern p) {
      startElement("i:error");
      attribute("xmlns:i", INTERNAL_NAMESPACE);
      endElement();
      return VoidValue.VOID;
    }
  }

  class ElementDumper extends Dumper {
    public VoidValue caseElement(ElementPattern p) {
      startElement("element");
      outputName(p.getNameClass());
      p.getContent().apply(dumper);
      endElement();
      return VoidValue.VOID;
    }
  }

  class OptionalDumper extends AbstractPatternFunction<VoidValue> {
    public VoidValue caseOther(Pattern p) {
      startElement("optional");
      p.apply(dumper);
      endElement();
      return VoidValue.VOID;
    }

    public VoidValue caseOneOrMore(OneOrMorePattern p) {
      startElement("zeroOrMore");
      p.getOperand().apply(dumper);
      endElement();
      return VoidValue.VOID;
    }
  }

  class GroupDumper extends Dumper {
    public VoidValue caseGroup(GroupPattern p) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      return VoidValue.VOID;
    }
  }

  class ChoiceDumper extends Dumper {
    protected void choice(Pattern p1, Pattern p2) {
      p1.apply(this);
      p2.apply(this);
    }
  }

  class InterleaveDumper extends Dumper {
     public VoidValue caseInterleave(InterleavePattern p) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
      return VoidValue.VOID;
    }
  }

  class NameClassDumper implements NameClassVisitor {
    public void visitChoice(NameClass nc1, NameClass nc2) {
      startElement("choice");
      nc1.accept(choiceNameClassDumper);
      nc2.accept(choiceNameClassDumper);
      endElement();
    }

    public void visitNsName(String ns) {
      startElement("nsName");
      attribute("ns", ns);
      endElement();
    }

    public void visitNsNameExcept(String ns, NameClass nc) {
      startElement("nsName");
      attribute("ns", ns);
      startElement("except");
      nc.accept(nameClassDumper);
      endElement();
      endElement();
    }

    public void visitAnyName() {
      startElement("anyName");
      endElement();
    }

    public void visitAnyNameExcept(NameClass nc) {
      startElement("anyName");
      startElement("except");
      nc.accept(nameClassDumper);
      endElement();
      endElement();
    }

    public void visitName(Name name) {
      startElement("name");
      attribute("ns", name.getNamespaceUri());
      data(name.getLocalName());
      endElement();
    }

    public void visitError() {
      startElement("error");
      endElement();
    }
    
    public void visitNull() {
      visitAnyName();
    }
  }

  class ChoiceNameClassDumper extends NameClassDumper {
    public void visitChoice(NameClass nc1, NameClass nc2) {
      nc1.accept(this);
      nc2.accept(this);
    }
  }
}
