package com.thaiopensource.relaxng.pattern;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.ValidationContext;

import java.util.HashMap;
import java.util.Map;

/**
 * DataDerivType for a pattern which is a choice of values of the same datatype.
 */
class ValueDataDerivType extends DataDerivType {
  private final Datatype dt;
  private PatternMemo noValue;
  private Map<DatatypeValue, PatternMemo> valueMap;

  ValueDataDerivType(Datatype dt) {
    this.dt = dt;
  }

  DataDerivType copy() {
    return new ValueDataDerivType(dt);
  }

  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                        DataDerivFailure fail) {
    Object value = dt.createValue(str, vc);
    if (value == null) {
      if (noValue == null)
        noValue = super.dataDeriv(builder, p, str, vc, null);
      return noValue;
    }
    else {
      DatatypeValue dtv = new DatatypeValue(value, dt);
      if (valueMap == null)
        valueMap = new HashMap<DatatypeValue, PatternMemo>();
      PatternMemo tem = valueMap.get(dtv);
      if (tem == null) {
        tem = super.dataDeriv(builder, p, str, vc, null);
        valueMap.put(dtv, tem);
      }
      return tem;
    }
  }

  DataDerivType combine(DataDerivType ddt) {
    if (ddt instanceof ValueDataDerivType) {
      if (((ValueDataDerivType)ddt).dt == this.dt)
        return this;
      else
        return InconsistentDataDerivType.getInstance();
    }
    else
      return ddt.combine(this);
  }

  Datatype getDatatype() {
    return dt;
  }
}
