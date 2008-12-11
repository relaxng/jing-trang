package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeException;

import java.math.BigDecimal;

class ScaleRestrictDatatype extends ValueRestrictDatatype {
  private final int scale;

  ScaleRestrictDatatype(DatatypeBase base, int scale) {
    super(base);
    this.scale = scale;
  }

  void checkRestriction(Object obj) throws DatatypeException {
    int actualScale = ((BigDecimal)obj).scale();
    if (actualScale > scale) {
      String message;
      switch (scale) {
      case 0:
        message = localizer().message("scale_0_violation");
        break;
      case 1:
        message = localizer().message("scale_1_violation", actualScale);
        break;
      default:
        message = localizer().message("scale_violation", scale, actualScale);
        break;
      }
      throw new DatatypeException(message);
    }
  }
}
