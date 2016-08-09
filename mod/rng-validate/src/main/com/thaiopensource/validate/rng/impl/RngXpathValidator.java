package com.thaiopensource.validate.rng.impl;

import com.thaiopensource.relaxng.pattern.Pattern;
import com.thaiopensource.relaxng.pattern.ValidatorPatternBuilder;
import org.xml.sax.ErrorHandler;

/**
 * Relax NG validator which outputs XPath locators (instead of line and column).
 */
public class RngXpathValidator extends RngValidator {

    public RngXpathValidator(Pattern pattern, ValidatorPatternBuilder builder, ErrorHandler eh) {
        super(pattern, builder, eh);
    }
}
