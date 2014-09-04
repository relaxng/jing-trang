package com.thaiopensource.validate.schematron.extfn;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.Int64Value;
import net.sf.saxon.value.SequenceType;


/**
 * @author cristi_talau
 */
public class LineNumberFunctionDefinition extends ExtensionFunctionDefinition {

  @Override
  public ExtensionFunctionCall makeCallExpression() {
    return new ExtensionFunctionCall() {
      
      @Override
      public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
        int columnNumber = -1;
        Item item = context.getContextItem();          
        if (item instanceof net.sf.saxon.om.NodeInfo) {
          net.sf.saxon.om.NodeInfo nodeInfo = (net.sf.saxon.om.NodeInfo) item;            
          columnNumber = nodeInfo.getLineNumber();
        }
        return Int64Value.makeIntegerValue(columnNumber);
      }
    };
  }

  @Override
  public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
    return SequenceType.SINGLE_INTEGER;
  }

  @Override
  public StructuredQName getFunctionQName() {
    return new StructuredQName("jing-extension-functions", 
        "http://www.thaiopensource.com/ns/extension-functions", "line-number");
  }

  @Override
  public SequenceType[] getArgumentTypes() {
    return new SequenceType[0];
  }
}