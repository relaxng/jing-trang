package com.thaiopensource.relaxng.parse.nonxml;

import com.thaiopensource.relaxng.parse.Location;

final class LocatedString {
  private final String str;
  private final Location loc;

  LocatedString(String str, Location loc) {
    this.str = str;
    this.loc = loc;
  }

  String getString() {
    return str;
  }

  Location getLocation() {
    return loc;
  }
}
