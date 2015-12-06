package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.xsd.regex.java.RegexEngineImpl;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DatatypeLibraryImplTest {
  DatatypeLibrary lib = new DatatypeLibraryImpl(new RegexEngineImpl());
  @Test
  public void testCreateDatatype() throws DatatypeException {
    Datatype dt = lib.createDatatype("integer");
    try {
      dt.checkValid("foo", null);
    }
    catch (DatatypeException e) {
      String message = e.getMessage();
      Assert.assertNotNull(message);
      Assert.assertTrue(message.contains("integer"));
    }
  }

  @Test
  public void testCreateDatatypeBuilder() throws DatatypeException {
    DatatypeBuilder dtb = lib.createDatatypeBuilder("decimal");
    dtb.addParameter("fractionDigits", "2", null);
    dtb.addParameter("totalDigits", "3", null);
    dtb.addParameter("maxInclusive", "42", null);
    dtb.addParameter("minInclusive", "-17", null);
    Datatype dt = dtb.createDatatype();
    try {
      dt.checkValid("foo", null);
    }
    catch (DatatypeException e) {
      String message = e.getMessage();
      Assert.assertNotNull(message);
      Assert.assertTrue(message.contains("decimal"));
      Assert.assertFalse(message.contains("digits"));
    }
    try {
      dt.checkValid("47", null);
    }
    catch (DatatypeException e) {
      String message = e.getMessage();
      Assert.assertNotNull(message);
      Assert.assertTrue(message.contains("42"));
    }
    try {
      dt.checkValid("-30", null);
    }
    catch (DatatypeException e) {
      String message = e.getMessage();
      Assert.assertNotNull(message);
      Assert.assertTrue(message.contains("-17"));
    }
    try {
      dt.checkValid("0.123", null);
    }
    catch (DatatypeException e) {
      String message = e.getMessage();
      Assert.assertNotNull(message);
      Assert.assertTrue(message.contains("digits"));
      Assert.assertTrue(message.contains("point"));
      Assert.assertTrue(message.contains("3"));
      Assert.assertTrue(message.contains("2"));
    }
    try {
      dt.checkValid("10.12", null);
    }
    catch (DatatypeException e) {
      String message = e.getMessage();
      Assert.assertNotNull(message);
      Assert.assertTrue(message.contains("digits"));
      Assert.assertFalse(message.contains("point"));
      Assert.assertTrue(message.contains("3"));
      Assert.assertTrue(message.contains("4"));      
    }
  }
}
