package com.thaiopensource.validate;

public class FlagOption implements Option {
  private final FlagPropertyId pid;
  public FlagOption(FlagPropertyId pid) {
    this.pid = pid;
  }

  public FlagPropertyId getPropertyId() {
    return pid;
  }

  public Flag valueOf(String arg) throws OptionArgumentException {
    if (arg != null)
      throw new OptionArgumentPresenceException();
    return Flag.PRESENT;
  }

  public Object combine(Object[] values) {
    return null;
  }
}
