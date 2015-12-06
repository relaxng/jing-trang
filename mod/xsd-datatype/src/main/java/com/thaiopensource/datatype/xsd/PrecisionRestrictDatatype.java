package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeException;

import java.math.BigDecimal;

class PrecisionRestrictDatatype extends ValueRestrictDatatype {
  private final int precision;

  PrecisionRestrictDatatype(DatatypeBase base, int precision) {
    super(base);
    this.precision = precision;
  }

  void checkRestriction(Object obj) throws DatatypeException {
    final int actualPrecision = getPrecision((BigDecimal)obj);
    if (actualPrecision > precision) {
      String message;
      if (precision == 1)
        message = localizer().message("precision_1_violation",
                                      getDescriptionForRestriction(),
                                      actualPrecision);
      else
        message = localizer().message("precision_violation",
                                      new Object[] {
                                              getDescriptionForRestriction(),
                                              precision,
                                              actualPrecision
                                      });
      throw new DatatypeException(message);
    }
  }

  static int getPrecision(BigDecimal n) {
    return n.movePointRight(n.scale()).abs().toString().length();
  }
}
