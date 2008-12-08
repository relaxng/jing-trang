package com.thaiopensource.validate.prop.rng;

import com.thaiopensource.util.PropertyId;
import com.thaiopensource.validate.FlagOption;
import com.thaiopensource.validate.FlagPropertyId;
import com.thaiopensource.validate.Option;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.StringPropertyId;
import org.relaxng.datatype.DatatypeLibraryFactory;

public class RngProperty {

  private RngProperty() { }

  public static final PropertyId<DatatypeLibraryFactory> DATATYPE_LIBRARY_FACTORY
          = PropertyId.newInstance("DATATYPE_LIBRARY_FACTORY", DatatypeLibraryFactory.class);
  public static final FlagPropertyId CHECK_ID_IDREF = new FlagPropertyId("CHECK_ID_IDREF");
  public static final FlagPropertyId FEASIBLE = new FlagPropertyId("FEASIBLE");
  public static final StringPropertyId SIMPLIFIED_SCHEMA = new StringPropertyId("SIMPLIFIED_SCHEMA");

  public static Option getOption(String uri) {
    if (!uri.startsWith(SchemaReader.BASE_URI))
      return null;
    uri = uri.substring(SchemaReader.BASE_URI.length());
    if (uri.equals("feasible"))
      return new FlagOption(FEASIBLE);
    if (uri.equals("check-id-idref"))
      return new FlagOption(CHECK_ID_IDREF);
    return null;
  }
}
