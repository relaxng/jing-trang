package com.thaiopensource.validate;

public class StringOption implements Option {
  private final StringPropertyId pid;

  public StringOption(StringPropertyId pid) {
    this.pid = pid;
  }

  public StringPropertyId getPropertyId() {
    return pid;
  }

  public String valueOf(String arg) throws OptionArgumentException {
    if (arg == null)
      return defaultValue();
    return normalize(arg);
  }

  public String defaultValue() throws OptionArgumentPresenceException {
    throw new OptionArgumentPresenceException();
  }

  public String normalize(String value) throws OptionArgumentFormatException {
    return value;
  }

  public Object combine(Object[] values) {
    return null;
  }
}
