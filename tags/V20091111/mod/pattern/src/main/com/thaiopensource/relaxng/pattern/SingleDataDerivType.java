package com.thaiopensource.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

import java.util.List;

/**
 * DerivType for a Pattern whose derivative wrt any data is always the same.
 */
class SingleDataDerivType extends DataDerivType {
  private PatternMemo memo;

  SingleDataDerivType() { }

  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                        List<DataDerivFailure> fail) {
    if (memo == null)
      // this type never adds any failures
      memo = super.dataDeriv(builder, p, str, vc, null);
    return memo;
  }

  DataDerivType copy() {
    return new SingleDataDerivType();
  }

  DataDerivType combine(DataDerivType ddt) {
    return ddt;
  }
}