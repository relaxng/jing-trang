package com.thaiopensource.datatype.xsd;

import org.relaxng.datatype.DatatypeException;

class MinExclusiveRestrictDatatype extends ValueRestrictDatatype {
  private final OrderRelation order;
  private final Object limit;
  private final String limitString;

  MinExclusiveRestrictDatatype(DatatypeBase base, Object limit, String limitString) {
    super(base);
    this.order = base.getOrderRelation();
    this.limit = limit;
    this.limitString = limitString;
  }

  void checkRestriction(Object value) throws DatatypeException {
    if (!order.isLessThan(limit, value))
      throw new DatatypeException(localizer().message("min_exclusive_violation",
                                                      getDescriptionForRestriction(),
                                                      limitString));
  }
}
