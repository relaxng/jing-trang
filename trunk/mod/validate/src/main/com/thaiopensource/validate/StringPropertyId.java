package com.thaiopensource.validate;

import com.thaiopensource.util.PropertyId;

/**
 * A PropertyId whose value is constrained to be an instance of
 * String.
 *
 * @see String
 */

public class StringPropertyId extends PropertyId<String> {
   public StringPropertyId(String name) {
      super(name, String.class);
    }
}
