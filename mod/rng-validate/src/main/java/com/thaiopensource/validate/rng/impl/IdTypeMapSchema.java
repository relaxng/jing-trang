package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.validate.AbstractSchema;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.relaxng.pattern.IdTypeMap;
import org.xml.sax.ErrorHandler;

public class IdTypeMapSchema extends AbstractSchema {
  private final IdTypeMap idTypeMap;

  public IdTypeMapSchema(IdTypeMap idTypeMap, PropertyMap properties) {
    super(properties);
    this.idTypeMap = idTypeMap;
  }

  public Validator createValidator(PropertyMap properties) {
    ErrorHandler eh = properties.get(ValidateProperty.ERROR_HANDLER);
    return new IdValidator(idTypeMap, eh);
  }
}
