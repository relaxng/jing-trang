package com.thaiopensource.relaxng.output.rng;

class WrappedException extends RuntimeException {
  private Throwable cause;

  public Throwable getCause() {
    return cause;
  }

  WrappedException(Throwable cause) {
    this.cause = cause;
  }
}
