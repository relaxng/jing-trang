package com.thaiopensource.datatype;

import com.thaiopensource.util.Service;
import org.relaxng.datatype.DatatypeLibrary;
import org.relaxng.datatype.DatatypeLibraryFactory;

import java.util.Iterator;

// We use this instead of the one in org.relaxng.datatype.helper because tools.jar in Java 6 includes
// org.relaxng.datatype, which messes up class loading for the jing task in Ant, when Ant's class loader's
// parent will have tools.jar in its classpath.
public class DatatypeLibraryLoader implements DatatypeLibraryFactory {
  private final Service<DatatypeLibraryFactory> service = Service.newInstance(DatatypeLibraryFactory.class);

  public DatatypeLibrary createDatatypeLibrary(String uri) {
    for (Iterator<DatatypeLibraryFactory> iter = service.getProviders(); iter.hasNext();) {
      DatatypeLibraryFactory factory = iter.next();
      DatatypeLibrary library = factory.createDatatypeLibrary(uri);
      if (library != null)
        return library;
    }
    return null;
  }

}
