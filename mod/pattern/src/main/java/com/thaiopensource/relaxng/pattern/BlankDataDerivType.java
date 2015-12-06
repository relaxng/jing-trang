package com.thaiopensource.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

import java.util.List;

class BlankDataDerivType extends DataDerivType {
  private PatternMemo blankMemo;
  private PatternMemo nonBlankMemo;

  BlankDataDerivType() { }

  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                        List<DataDerivFailure> fail) {
    if (DataDerivFunction.isBlank(str)) {
      if (blankMemo == null || (fail != null && blankMemo.isNotAllowed()))
        blankMemo = super.dataDeriv(builder, p, str, vc, fail);
      return blankMemo;
    }
    else {
      if (nonBlankMemo == null || (fail != null && nonBlankMemo.isNotAllowed()))
        nonBlankMemo = super.dataDeriv(builder, p, str, vc, fail);
      return nonBlankMemo;
    }
  }

  DataDerivType copy() {
    return new BlankDataDerivType();
  }

  DataDerivType combine(DataDerivType ddt) {
    if (ddt instanceof BlankDataDerivType || ddt instanceof SingleDataDerivType)
      return this;
    return InconsistentDataDerivType.getInstance();
  }
}