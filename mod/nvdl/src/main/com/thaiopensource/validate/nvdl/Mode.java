package com.thaiopensource.validate.nvdl;

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import java.util.Hashtable;
import java.util.Enumeration;


class Mode {
  static final String ANY_NAMESPACE = "##any";
  static final int ATTRIBUTE_PROCESSING_NONE = 0;
  static final int ATTRIBUTE_PROCESSING_QUALIFIED = 1;
  static final int ATTRIBUTE_PROCESSING_FULL = 2;
  
  /**
   * A special mode. In a mode usage this will be 
   * resolved by the mode usage to the actual current mode
   * from that mode usage.
   */
  static final Mode CURRENT = new Mode("#current", null);

  /**
   * The mode name.
   */
  private final String name;
  
  /**
   * The base mode.
   */
  private Mode baseMode;
  
  /**
   * Flag indicating if this mode is defined by the user
   * or is an automatically generated mode.
   */
  private boolean defined;
  /**
   * Locate the place where this mode is defined.
   */
  private Locator whereDefined;
  
  /**
   * Locate the place this mode is first used.
   * Useful to report with location errors like 
   * 'Mode "xxx" not defined'.
   */
  private Locator whereUsed;
  private final Hashtable elementMap = new Hashtable();
  private final Hashtable attributeMap = new Hashtable();
  private int attributeProcessing = -1;

  /**
   * Creates a mode extending a base mode.
   * @param name The new mode name.
   * @param baseMode The base mode.
   */
  Mode(String name, Mode baseMode) {
    this.name = name;
    this.baseMode = baseMode;
  }

  /**
   * Get this mode name.
   * @return The name.
   */
  String getName() {
    return name;
  }

  /**
   * Get the base mode.
   * @return The base mode.
   */
  Mode getBaseMode() {
    return baseMode;
  }

  /**
   * Set a base mode.
   * @param baseMode The new base mode.
   */
  void setBaseMode(Mode baseMode) {
    this.baseMode = baseMode;
  }

  /**
   * Get the set of element actions for a given namespace.
   * If this mode has an explicit handling of that namespace then we get those
   * actions, otherwise we get the actions for any namespace.
   * @param ns The namespace we look for element actions for.
   * @return A set of element actions.
   */
  ActionSet getElementActions(String ns) {
    ActionSet actions = getElementActionsExplicit(ns);
    if (actions == null) {
      actions = getElementActionsExplicit(ANY_NAMESPACE);
      // this is not correct: it breaks a derived mode that use anyNamespace
      // elementMap.put(ns, actions);
    }
    return actions;
  }

  /**
   * Look for element actions specifically specified
   * for this namespace. If the current mode does not have
   * actions for that namespace look at base modes. If the actions 
   * are defined in a base mode we need to get a copy of those actions
   * associated with this mode, so we call changeCurrentMode on them.
   * 
   * @param ns The namespace
   * @return A set of element actions.
   */
  private ActionSet getElementActionsExplicit(String ns) {
    ActionSet actions = (ActionSet)elementMap.get(ns);
    if (actions == null && baseMode != null) {
      actions = baseMode.getElementActionsExplicit(ns);
      if (actions != null) {
        actions = actions.changeCurrentMode(this);
        elementMap.put(ns, actions);
      }
    }
    return actions;
  }

  /**
   * Get the set of attribute actions for a given namespace.
   * If this mode has an explicit handling of that namespace then we get those
   * actions, otherwise we get the actions for any namespace.
   * @param ns The namespace we look for attribute actions for.
   * @return A set of attribute actions.
   */
  AttributeActionSet getAttributeActions(String ns) {
    AttributeActionSet actions = getAttributeActionsExplicit(ns);
    if (actions == null) {
      actions = getAttributeActionsExplicit(ANY_NAMESPACE);
      // this is not correct: it breaks a derived mode that use anyNamespace
      // attributeMap.put(ns, actions);
    }
    return actions;
  }

  /**
   * Look for attribute actions specifically specified
   * for this namespace. If the current mode does not have
   * actions for that namespace look at base modes. If the actions 
   * are defined in a base mode we need to get a copy of those actions
   * associated with this mode, so we call changeCurrentMode on them.
   * 
   * @param ns The namespace
   * @return A set of attribute actions.
   */
   private AttributeActionSet getAttributeActionsExplicit(String ns) {
    AttributeActionSet actions = (AttributeActionSet)attributeMap.get(ns);
    if (actions == null && baseMode != null) {
      actions = baseMode.getAttributeActionsExplicit(ns);
      if (actions != null)
        attributeMap.put(ns, actions);
    }
    return actions;
  }

  /**
   * Computes (if not already computed) the attributeProcessing
   * for this mode and returns it.
   * If it find anything different than attach then we need to perform 
   * attribute processing.
   * If only attributes for a specific namespace have actions then we only need to
   * process qualified attributes, otherwise we need to process all attributes.
   * 
   * @return The attribute processing for this mode.
   */
  int getAttributeProcessing() {
    if (attributeProcessing == -1) {
      if (baseMode != null)
        attributeProcessing = baseMode.getAttributeProcessing();
      else
        attributeProcessing = ATTRIBUTE_PROCESSING_NONE;
      for (Enumeration e = attributeMap.keys(); e.hasMoreElements() && attributeProcessing != ATTRIBUTE_PROCESSING_FULL;) {
        String ns = (String)e.nextElement();
        AttributeActionSet actions = (AttributeActionSet)attributeMap.get(ns);
        if (!actions.getAttach()
            || actions.getReject()
            || actions.getSchemas().length > 0)
          attributeProcessing = ((ns.equals("") || ns.equals(ANY_NAMESPACE))
                                ? ATTRIBUTE_PROCESSING_FULL
                                : ATTRIBUTE_PROCESSING_QUALIFIED);
      }
    }
    return attributeProcessing;
  }

  /**
   * Get the locator that points to the place the 
   * mode is defined.
   * @return a locator.
   */
  Locator getWhereDefined() {
    return whereDefined;
  }

  /**
   * Getter for the defined flag.
   * @return defined.
   */
  boolean isDefined() {
    return defined;
  }

  /**
   * Get a locator pointing to the first place this mode is used.
   * @return a locator.
   */
  Locator getWhereUsed() {
    return whereUsed;
  }

  /**
   * Record the locator if this is the first location this mode is used.
   * @param locator Points to the location this mode is used from.
   */
  void noteUsed(Locator locator) {
    if (whereUsed == null && locator != null)
      whereUsed = new LocatorImpl(locator);
  }

  /**
   * Record the locator this mode is defined at.
   * @param locator Points to the mode definition.
   */
  void noteDefined(Locator locator) {
    defined = true;
    if (whereDefined == null && locator != null)
      whereDefined = new LocatorImpl(locator);
  }

  boolean bindElement(String ns, ActionSet actions) {
    if (elementMap.get(ns) != null)
      return false;
    elementMap.put(ns, actions);
    return true;
  }

  boolean bindAttribute(String ns, AttributeActionSet actions) {
    if (attributeMap.get(ns) != null)
      return false;
    attributeMap.put(ns, actions);
    return true;
  }

}
