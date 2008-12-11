package com.thaiopensource.datatype.xsd;

import com.thaiopensource.datatype.xsd.regex.RegexEngine;
import com.thaiopensource.datatype.xsd.regex.RegexSyntaxException;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeBuilder;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.DatatypeLibrary;

import java.util.HashMap;
import java.util.Map;

public class DatatypeLibraryImpl implements DatatypeLibrary {
  private final Map<String, DatatypeBase> typeMap = new HashMap<String, DatatypeBase>();
  private final RegexEngine regexEngine;

  static private final String LONG_MAX = "9223372036854775807";
  static private final String LONG_MIN = "-9223372036854775808";
  static private final String INT_MAX = "2147483647";
  static private final String INT_MIN = "-2147483648";
  static private final String SHORT_MAX = "32767";
  static private final String SHORT_MIN = "-32768";
  static private final String BYTE_MAX = "127";
  static private final String BYTE_MIN = "-128";

  static private final String UNSIGNED_LONG_MAX = "18446744073709551615";
  static private final String UNSIGNED_INT_MAX = "4294967295";
  static private final String UNSIGNED_SHORT_MAX = "65535";
  static private final String UNSIGNED_BYTE_MAX = "255";
  // Follow RFC 3066 syntax.
  static private final String LANGUAGE_PATTERN = "[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*";

  public DatatypeLibraryImpl(RegexEngine regexEngine) {
    this.regexEngine = regexEngine;
    typeMap.put("string", new StringDatatype());
    typeMap.put("normalizedString", new CdataDatatype());
    typeMap.put("token", new TokenDatatype());
    typeMap.put("boolean", new BooleanDatatype());

    DatatypeBase decimalType = new DecimalDatatype();
    typeMap.put("decimal", decimalType);
    DatatypeBase integerType = new IntegerRestrictDatatype(decimalType);
    typeMap.put("integer", integerType);
    typeMap.put("nonPositiveInteger", restrictMax(integerType, "0"));
    typeMap.put("negativeInteger", restrictMax(integerType, "-1"));
    typeMap.put("long", restrictMax(restrictMin(integerType, LONG_MIN), LONG_MAX));
    typeMap.put("int", restrictMax(restrictMin(integerType, INT_MIN), INT_MAX));
    typeMap.put("short", restrictMax(restrictMin(integerType, SHORT_MIN), SHORT_MAX));
    typeMap.put("byte", restrictMax(restrictMin(integerType, BYTE_MIN), BYTE_MAX));
    DatatypeBase nonNegativeIntegerType = restrictMin(integerType, "0");
    typeMap.put("nonNegativeInteger", nonNegativeIntegerType);
    typeMap.put("unsignedLong", restrictMax(nonNegativeIntegerType, UNSIGNED_LONG_MAX));
    typeMap.put("unsignedInt", restrictMax(nonNegativeIntegerType, UNSIGNED_INT_MAX));
    typeMap.put("unsignedShort", restrictMax(nonNegativeIntegerType, UNSIGNED_SHORT_MAX));
    typeMap.put("unsignedByte", restrictMax(nonNegativeIntegerType, UNSIGNED_BYTE_MAX));
    typeMap.put("positiveInteger", restrictMin(integerType, "1"));
    typeMap.put("double", new DoubleDatatype());
    typeMap.put("float", new FloatDatatype());

    typeMap.put("Name", new NameDatatype());
    typeMap.put("QName", new QNameDatatype());

    DatatypeBase ncNameType = new NCNameDatatype();
    typeMap.put("NCName", ncNameType);

    DatatypeBase nmtokenDatatype = new NmtokenDatatype();
    typeMap.put("NMTOKEN", nmtokenDatatype);
    typeMap.put("NMTOKENS", list(nmtokenDatatype));

    typeMap.put("ID", new IdDatatype());
    DatatypeBase idrefType = new IdrefDatatype();
    typeMap.put("IDREF", idrefType);
    typeMap.put("IDREFS", list(idrefType));

    typeMap.put("NOTATION", new QNameDatatype());

    typeMap.put("base64Binary", new Base64BinaryDatatype());
    typeMap.put("hexBinary", new HexBinaryDatatype());
    typeMap.put("anyURI", new AnyUriDatatype());
    typeMap.put("language", new RegexDatatype(LANGUAGE_PATTERN) {
      String getLexicalSpaceKey() {
        return "language";
      }
    });

    typeMap.put("dateTime", new DateTimeDatatype("Y-M-DTt"));
    typeMap.put("time", new DateTimeDatatype("t"));
    typeMap.put("date", new DateTimeDatatype("Y-M-D"));
    typeMap.put("gYearMonth", new DateTimeDatatype("Y-M"));
    typeMap.put("gYear", new DateTimeDatatype("Y"));
    typeMap.put("gMonthDay", new DateTimeDatatype("--M-D"));
    typeMap.put("gDay", new DateTimeDatatype("---D"));
    typeMap.put("gMonth", new DateTimeDatatype("--M"));

    DatatypeBase entityType = new EntityDatatype();
    typeMap.put("ENTITY", entityType);
    typeMap.put("ENTITIES", list(entityType));
    // Partially implemented
    typeMap.put("duration", new DurationDatatype());
  }

  public DatatypeBuilder createDatatypeBuilder(String localName) throws DatatypeException {
    DatatypeBase base = typeMap.get(localName);
    if (base == null)
      throw new DatatypeException();
    if (base instanceof RegexDatatype) {
      try {
        ((RegexDatatype)base).compile(getRegexEngine());
      }
      catch (RegexSyntaxException e) {
        throw new DatatypeException(DatatypeBuilderImpl.localizer.message("regex_internal_error", localName));
      }
    }
    return new DatatypeBuilderImpl(this, base);
  }

  RegexEngine getRegexEngine() throws DatatypeException {
    if (regexEngine == null)
      throw new DatatypeException(DatatypeBuilderImpl.localizer.message("regex_impl_not_found"));
    return regexEngine;
  }

  private static DatatypeBase restrictMax(DatatypeBase base, String limit) {
    try {
      return new MaxInclusiveRestrictDatatype(base, base.getValue(limit, null), limit);
    }
    catch (DatatypeException e) {
      throw new AssertionError();
    }
  }

  private static DatatypeBase restrictMin(DatatypeBase base, String limit) {
    try {
      return new MinInclusiveRestrictDatatype(base, base.getValue(limit, null), limit);
    }
    catch (DatatypeException e) {
      throw new AssertionError();
    }
  }

  private static DatatypeBase list(DatatypeBase base) {
    return new MinLengthRestrictDatatype(new ListDatatype(base), 1);
  }

  public Datatype createDatatype(String type) throws DatatypeException {
    return createDatatypeBuilder(type).createDatatype();
  }
}
