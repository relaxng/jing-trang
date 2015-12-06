package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.relaxng.pattern.IdTypeMap;
import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import org.xml.sax.ErrorHandler;

public class FeasibleIdTypeMapSchema extends AbstractSchema {
  private final IdTypeMap idTypeMap;

  public FeasibleIdTypeMapSchema(IdTypeMap idTypeMap, PropertyMap properties) {
    super(properties);
    this.idTypeMap = idTypeMap;
  }

  public Validator createValidator(PropertyMap properties) {
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    return new IdValidator(idTypeMap, eh) {
      public void endDocument() {
         setComplete();
      }
    };
  }
}
