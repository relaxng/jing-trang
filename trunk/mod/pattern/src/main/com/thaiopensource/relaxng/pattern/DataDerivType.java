package com.thaiopensource.relaxng.pattern;

import org.relaxng.datatype.ValidationContext;

abstract class DataDerivType {
  abstract DataDerivType copy();
  abstract DataDerivType combine(DataDerivType ddt);
  PatternMemo dataDeriv(ValidatorPatternBuilder builder, Pattern p, String str, ValidationContext vc,
                        DataDerivFailure fail) {
    return builder.getPatternMemo(p.apply(new DataDerivFunction(str, vc, builder, fail)));
  }
}
