package com.thaiopensource.relaxng.pattern;

import org.relaxng.datatype.Datatype;
import org.relaxng.datatype.DatatypeException;
import org.relaxng.datatype.ValidationContext;

import java.util.List;

class DataDataDerivType extends DataDerivType {
  private final DataPattern dp;
  private PatternMemo validMemo;
  private PatternMemo invalidMemo;

  DataDataDerivType(DataPattern dp) {
    this.dp = dp;
  }

  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                        List<DataDerivFailure> fail) {
    boolean isValid;
    final Datatype dt = dp.getDatatype();
    DataDerivFailure ddf = null;
    if (fail != null) {
      try {
        dt.checkValid(str, vc);
        isValid = true;
      }
      catch (DatatypeException e) {
        isValid = false;
        ddf = new DataDerivFailure(dp, e);
      }
    }
    else
      isValid = dt.isValid(str, vc);
    if (isValid) {
      if (validMemo == null || (fail != null && validMemo.isNotAllowed()))
        validMemo = super.dataDeriv(builder, p, str, vc, fail);
      return validMemo;
    }
    else {
      if (invalidMemo == null)
        invalidMemo = super.dataDeriv(builder, p, str, vc, fail);
      else if (invalidMemo.isNotAllowed() && ddf != null)
        fail.add(ddf);
      return invalidMemo;
    }
  }

  DataDerivType copy() {
    return new DataDataDerivType(dp);
  }

  DataDerivType combine(DataDerivType ddt) {
    if (ddt instanceof DataDataDerivType) {
      if (((DataDataDerivType)ddt).dp.getDatatype() == dp.getDatatype())
        return this;
      return InconsistentDataDerivType.getInstance();
    }
    if (ddt instanceof ValueDataDerivType) {
      if (((ValueDataDerivType)ddt).getDatatype() == dp.getDatatype())
        return ddt;
      return InconsistentDataDerivType.getInstance();
    }
    return ddt.combine(this);
  }
}