package com.thaiopensource.relaxng.output;

import com.thaiopensource.relaxng.translate.util.ParamProcessor;
import com.thaiopensource.relaxng.translate.util.EncodingParam;
import com.thaiopensource.relaxng.translate.util.IntegerParam;

public class OutputDirectoryParamProcessor extends ParamProcessor {
  private final OutputDirectory od;
  private static final int MAX_INDENT = 16;
  private static final int MIN_LINELENGTH = 20;
  private static final int MAX_LINELENGTH = 1024;

  public OutputDirectoryParamProcessor(OutputDirectory od) {
    this.od = od;
    super.declare("encoding",
                  new EncodingParam() {
                    protected void setEncoding(String encoding) {
                      OutputDirectoryParamProcessor.this.od.setEncoding(encoding);
                    }
                  });
    super.declare("indent",
                  new IntegerParam(0, MAX_INDENT) {
                    protected void setInteger(int value) {
                      OutputDirectoryParamProcessor.this.od.setIndent(value);
                    }
                  });
    super.declare("lineLength",
                  new IntegerParam(MIN_LINELENGTH, MAX_LINELENGTH) {
                    protected void setInteger(int value) {
                      OutputDirectoryParamProcessor.this.od.setLineLength(value);
                    }
                  });
  }
}
