package com.thaiopensource.relaxng.impl;

import java.text.MessageFormat;
import java.util.ResourceBundle;

public class Localizer {
  static final private String bundleName = "com.thaiopensource.relaxng.impl.resources.Messages";

  public static String message(String key) {
    return MessageFormat.format(ResourceBundle.getBundle(bundleName).getString(key),
			 new Object[]{});
  }

  public static String message(String key, Object arg) {
    return MessageFormat.format(ResourceBundle.getBundle(bundleName).getString(key),
			        new Object[]{arg});
  }

  public static String message(String key, Object arg1, Object arg2) {
    return MessageFormat.format(ResourceBundle.getBundle(bundleName).getString(key),
			        new Object[]{arg1, arg2});
  }
}
