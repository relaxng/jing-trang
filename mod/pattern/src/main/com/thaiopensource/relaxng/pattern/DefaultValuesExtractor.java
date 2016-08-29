package com.thaiopensource.relaxng.pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

/**
 * Extracts the default values for attributes and notifies a
 * listener for each value.
 * 
 * @author george@oxygenxml.com
 */
public class DefaultValuesExtractor {
  /**
   * Receives notification of default values. 
   */
  public static interface DefaultValuesListener {
    /**
     * The callback/notification method.
     * @param elementName The local name of the element.
     * @param elementNamespace The element namespace.
     * @param attributeName The local name of the attribute.
     * @param attributeNamepsace The attribute namespace.
     * @param value The default value.
     */
    public void defaultValue(String elementName, String elementNamespace, String attributeName, String attributeNamepsace, String value);
  }
  
  /**
   * The listener that will receive default value notifications.
   */
  private DefaultValuesListener listener;
  // store a list of element patterns.
  private final List<Pattern> patternList = new ArrayList<Pattern>();
  private final HashSet<Pattern> patternSet = new HashSet<Pattern>();

  private ElementContentVisitor ecv = new ElementContentVisitor(); 
  private ElementsVisitor ev = new ElementsVisitor(); 
  
  
  /**
   * Constructor
   * 
   * @param listener The annotation manager
   */
  public DefaultValuesExtractor(DefaultValuesListener listener) {
    this.listener = listener;
  }

  /**
   * Trigger the parsing.
   * @param p The start pattern.
   */
  public void parsePattern(Pattern p) {
    p.apply(ecv);
    for (int i = 0; i < patternList.size(); i++) {
      Pattern tem = patternList.get(i);
      tem.apply(ev);
    }
  }
  
  private void addPattern(Pattern p) {
    if (!patternSet.contains(p)) {
      patternList.add(p);
      patternSet.add(p);
    }
  }

  /**
   * Base class for pattern visitors.
   * @author george
   */
  class BaseVisitor implements PatternFunction<VoidValue>, NameClassVisitor {
    // ** Pattern visitor methods.** //
    public VoidValue caseElement(ElementPattern p)          {return VoidValue.VOID;}
    public VoidValue caseAttribute(AttributePattern p)      {return VoidValue.VOID;}

    public VoidValue caseError(ErrorPattern p)              {return VoidValue.VOID;}
    public VoidValue caseEmpty(EmptyPattern p)              {return VoidValue.VOID;}
    public VoidValue caseNotAllowed(NotAllowedPattern p)    {return VoidValue.VOID;}    
    public VoidValue caseGroup(GroupPattern g)              {g.getOperand1().apply(this);g.getOperand2().apply(this);return VoidValue.VOID;}
    public VoidValue caseInterleave(InterleavePattern i)    {i.getOperand1().apply(this);i.getOperand2().apply(this);return VoidValue.VOID;}
    public VoidValue caseChoice(ChoicePattern c)            {c.getOperand1().apply(this);c.getOperand2().apply(this);return VoidValue.VOID;}
    public VoidValue caseOneOrMore(OneOrMorePattern p)      {p.getOperand().apply(this);return VoidValue.VOID;}
    public VoidValue caseData(DataPattern d)                {return VoidValue.VOID;}
    public VoidValue caseDataExcept(DataExceptPattern p)    {return VoidValue.VOID;}
    public VoidValue caseValue(ValuePattern p)              {return VoidValue.VOID;}
    public VoidValue caseText(TextPattern t)                {return VoidValue.VOID;}
    public VoidValue caseList(ListPattern l)                {return VoidValue.VOID;}
    public VoidValue caseRef(RefPattern p)                  {p.getPattern().apply(this);return VoidValue.VOID;}
    public VoidValue caseAfter(AfterPattern p)              {return VoidValue.VOID;}

    // ** NameClass visitor methods.** //
    public void visitName(Name name)                        {}
    
    public void visitChoice(NameClass nc1, NameClass nc2)   {nc1.accept(this);nc2.accept(this);}
    public void visitNsName(String ns)                      {}
    public void visitNsNameExcept(String ns, NameClass nc)  {}
    public void visitAnyName()                              {}
    public void visitAnyNameExcept(NameClass nc)            {}
    public void visitNull()                                 {}
    public void visitError()                                {}
  }
  
  /**
   * Adds element patterns to the patterns list.
   * @author george
   */
  class ElementContentVisitor extends BaseVisitor {
    @Override
    public VoidValue caseElement(ElementPattern p) {
      addPattern(p);
      return VoidValue.VOID;
    }
  }
  
  /**
   * Visits an element, extracts default attributes and calls
   * the element content visitor to visit the element content.
   * 
   * @author george
   */
  class ElementsVisitor extends BaseVisitor {
    /**
     * Keeps all the elements found. List of Name.
     */
    private List<Name> elements = new ArrayList<Name>();

    @Override
    public VoidValue caseElement(ElementPattern p) {
      // determine the element name and call the attribute visitor on
      // its content.
      elements.clear();
      p.getNameClass().accept(this);
      if (elements.size() > 0) {
        p.getContent().apply(new AttributesVisitor(elements));
      }
      // call the content visitor to get other elements.
      p.getContent().apply(ecv);
      return VoidValue.VOID;
    }

    @Override
    public void visitName(Name name) {
      elements.add(name);
    }
  }
  
  /**
   * Notify the listener for each visited attribute with default value.
   */
  class AttributesVisitor extends BaseVisitor {
    private String defaultValue;
    private List<Name> elements;
    /**
     * @param elements The parent element names for the visited attributes.
     */
    public AttributesVisitor(List<Name> elements) {
      this.elements = elements;
    }
    @Override
    public VoidValue caseAttribute(AttributePattern p) {
      defaultValue =  p.getDefaultValue();
      if (defaultValue != null) {
        p.getNameClass().accept(this);
      }
      return VoidValue.VOID;
    }
    @Override
    public void visitName(Name name) {
      for (Name eName : elements) {
        listener.defaultValue(eName.getLocalName(), eName.getNamespaceUri(), name.getLocalName(), name.getNamespaceUri(), defaultValue);
      }
    }
  }
}
