package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;
import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DataDerivType for a pattern which is a choice of values of the same datatype.
 */
class ValueDataDerivType extends DataDerivType {
  private final Datatype dt;
  private final Name dtName;
  private PatternMemo noValue;
  private Map<DatatypeValue, PatternMemo> valueMap;

  ValueDataDerivType(Datatype dt, Name dtName) {
    this.dt = dt;
    this.dtName = dtName;
  }

  DataDerivType copy() {
    return new ValueDataDerivType(dt, dtName);
  }

  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                        List<DataDerivFailure> fail) {
    Object value = dt.createValue(str, vc);
    if (value == null) {
      if (noValue == null)
        noValue = super.dataDeriv(builder, p, str, vc, fail);
      else if (fail != null && noValue.isNotAllowed()) {
        try {
          dt.checkValid(str, vc);
        }
        catch (DatatypeException e) {
          fail.add(new DataDerivFailure(dt, dtName, e));
        }
      }
      return noValue;
    }
    else {
      DatatypeValue dtv = new DatatypeValue(value, dt);
      if (valueMap == null)
        valueMap = new HashMap<DatatypeValue, PatternMemo>();
      PatternMemo tem = valueMap.get(dtv);
      if (tem == null) {
        tem = super.dataDeriv(builder, p, str, vc, fail);
        valueMap.put(dtv, tem);
      }
      else if (tem.isNotAllowed() && fail != null)
        super.dataDeriv(builder, p, str, vc, fail);
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
