package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.xml.util.WellKnownNamespaces;
import com.thaiopensource.util.Service;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.util.Enumeration;

public class DatatypeLibraryFactoryImpl implements DatatypeLibraryFactory {

  private DatatypeLibrary datatypeLibrary = null;
  private final RegexEngine regexEngine;
  private final boolean autoRegexEngine;

  public DatatypeLibraryFactoryImpl() {
    this.regexEngine = null;
    this.autoRegexEngine = true;
  }

  public DatatypeLibraryFactoryImpl(RegexEngine regexEngine) {
    this.regexEngine = regexEngine;
    this.autoRegexEngine = false;
  }

  public DatatypeLibrary createDatatypeLibrary(String uri) {
    if (!WellKnownNamespaces.XML_SCHEMA_DATATYPES.equals(uri))
      return null;
    synchronized (this) {
      if (datatypeLibrary == null)
        datatypeLibrary = new DatatypeLibraryImpl(autoRegexEngine ? findRegexEngine() : regexEngine);
      return datatypeLibrary;
    }
  }

  private static RegexEngine findRegexEngine() {
    Enumeration e = new Service(RegexEngine.class).getProviders();
    if (!e.hasMoreElements())
      return null;
    return (RegexEngine)e.nextElement();
  }

}
