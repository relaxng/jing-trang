package com.thaiopensource.relaxng.output.xsd;

import com.thaiopensource.relaxng.output.xsd.basic.SchemaTransformer;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeUnion;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleType;
import com.thaiopensource.relaxng.output.xsd.basic.SimpleTypeRestriction;
import com.thaiopensource.relaxng.output.xsd.basic.Facet;

import java.util.List;
import java.util.Iterator;
import java.util.Vector;

class Transformer extends SchemaTransformer {
  public Object visitUnion(SimpleTypeUnion t) {
    List list = transformSimpleTypeList(t.getChildren());
    SimpleType combined = combineEnumeration(t, list);
    if (combined != null)
      return combined;
    return new SimpleTypeUnion(t.getLocation(), list);
  }

  private SimpleType combineEnumeration(SimpleTypeUnion orig, List transformedChildren) {
    if (transformedChildren.size() < 2)
      return null;
    Object first = transformedChildren.get(0);
    if (!(first instanceof SimpleTypeRestriction))
      return null;
    String builtinTypeName = ((SimpleTypeRestriction)first).getName();
    List facets = new Vector();
    for (Iterator iter = transformedChildren.iterator(); iter.hasNext();) {
      Object obj = iter.next();
      if (!(obj instanceof SimpleTypeRestriction))
        return null;
      SimpleTypeRestriction restriction = (SimpleTypeRestriction)obj;
      if (!restriction.getName().equals(builtinTypeName))
        return null;
      if (restriction.getFacets().isEmpty())
        return null;
      for (Iterator facetIter = restriction.getFacets().iterator(); facetIter.hasNext();) {
        Facet facet = (Facet)facetIter.next();
        if (!facet.getName().equals("enumeration"))
          return null;
        facets.add(facet);
      }
    }
    return new SimpleTypeRestriction(orig.getLocation(), builtinTypeName, facets);
  }

}
