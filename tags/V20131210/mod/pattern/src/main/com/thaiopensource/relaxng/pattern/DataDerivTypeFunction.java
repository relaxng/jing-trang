package com.thaiopensource.relaxng.pattern;

class DataDerivTypeFunction extends AbstractPatternFunction<DataDerivType> {
  private final ValidatorPatternBuilder builder;

  DataDerivTypeFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  static DataDerivType dataDerivType(ValidatorPatternBuilder builder, Pattern pattern) {
    return pattern.apply(builder.getDataDerivTypeFunction());
  }

  public DataDerivType caseOther(Pattern p) {
    return new SingleDataDerivType();
  }

  public DataDerivType caseRef(RefPattern p) {
    return apply(p.getPattern());
  }

  public DataDerivType caseAfter(AfterPattern p) {
    Pattern p1 = p.getOperand1();
    DataDerivType ddt = apply(p.getOperand1());
    if (!p1.isNullable())
      return ddt;
    return ddt.combine(new BlankDataDerivType());
  }

  private DataDerivType caseBinary(BinaryPattern p) {
    return apply(p.getOperand1()).combine(apply(p.getOperand2()));
  }

  public DataDerivType caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }

  public DataDerivType caseGroup(GroupPattern p) {
    return caseBinary(p);
  }

  public DataDerivType caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public DataDerivType caseOneOrMore(OneOrMorePattern p) {
    return apply(p.getOperand());
  }

  public DataDerivType caseList(ListPattern p) {
    return InconsistentDataDerivType.getInstance();
  }

  public DataDerivType caseValue(ValuePattern p) {
    return new ValueDataDerivType(p.getDatatype(), p.getDatatypeName());
  }

  public DataDerivType caseData(DataPattern p) {
    if (p.allowsAnyString())
      return new SingleDataDerivType();
    return new DataDataDerivType(p);
  }

  public DataDerivType caseDataExcept(DataExceptPattern p) {
    if (p.allowsAnyString())
      return apply(p.getExcept());
    return new DataDataDerivType(p).combine(apply(p.getExcept()));
  }

  private DataDerivType apply(Pattern p) {
    return builder.getPatternMemo(p).dataDerivType();
  }
}
