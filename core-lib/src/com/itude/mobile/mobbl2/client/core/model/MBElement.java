package com.itude.mobile.mobbl2.client.core.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.itude.mobile.mobbl2.client.core.configuration.mvc.MBAttributeDefinition;
import com.itude.mobile.mobbl2.client.core.configuration.mvc.MBElementDefinition;
import com.itude.mobile.mobbl2.client.core.model.exceptions.MBCannotAssignException;
import com.itude.mobile.mobbl2.client.core.model.exceptions.MBInvalidAttributeNameException;
import com.itude.mobile.mobbl2.client.core.util.StringUtilities;

public class MBElement extends MBElementContainer
{

  private static final String       TEXT_ATTRIBUTE = "text()";

  private final Map<String, String> _values;                  // Dictionaryofstrings
  private MBElementDefinition       _definition;

  public MBElement(MBElementDefinition definition)
  {
    super();
    _definition = definition;
    _values = new HashMap<String, String>();
  }

  @Override
  public MBElement clone()
  {
    MBElement newElement = new MBElement(_definition);
    newElement._values.putAll(_values);
    copyChildrenInto(newElement);

    return newElement;
  }

  @Override
  public void setValue(String value, String path)
  {
    if (path.startsWith("@"))
    {
      setValue(value, path.substring(1));
    }
    else
    {
      super.setValue(value, path);
    }
  }

  public void setAttributeValue(String value, String attributeName)
  {
    setAttributeValue(value, attributeName, true);
  }

  public void setAttributeValue(String value, String attributeName, boolean throwIfInvalid)
  {
    if (throwIfInvalid)
    {
      validateAttribute(attributeName);
      _values.put(attributeName, value);
    }
    else
    {
      if (isValidAttribute(attributeName))
      {
        _values.put(attributeName, value);
      }
    }

  }

  public String getValueForAttribute(String attributeName)
  {
    String rtrn = _values.get(attributeName);
    if (rtrn == null)
    {
      // above we assumed the attributeName is valid (should be true 100% of the time in production)
      // so we only validate the attrib if we do not have a value for it.
      validateAttribute(attributeName);
    }
    return rtrn;
  }

  public String getValueForKey(String key)
  {
    return getValueForAttribute(key);
  }

  public StringBuffer asXmlWithLevel(StringBuffer p_appendToMe, int level)
  {
    String bodyText = getBodyText();
    boolean hasBodyText = (bodyText != null && bodyText.length() > 0);

    StringUtilities.appendIndentString(p_appendToMe, level).append("<").append(_definition.getName());
    for (MBAttributeDefinition def : _definition.getAttributes())
    {
      String attrName = def.getName();
      String attrValue = _values.get(attrName);
      if (!attrName.equals(TEXT_ATTRIBUTE))
      {
        p_appendToMe.append(attributeAsXml(attrName, attrValue));
      }
    }

    if (_definition.getChildren().size() == 0 && !hasBodyText)
    {
      p_appendToMe.append("/>\n");
    }
    else
    {
      p_appendToMe.append(">");
      if (hasBodyText)
      {
        p_appendToMe.append(getBodyText());
      }
      else
      {
        p_appendToMe.append("\n");
      }

      for (MBElementDefinition elemDef : _definition.getChildren())
      {
        List<MBElement> lst = getElements().get(elemDef.getName());
        if (lst != null)
        {
          for (MBElement elem : lst)
          {
            elem.asXmlWithLevel(p_appendToMe, level + 2);
          }
        }
      }

      int closingLevel = 0;
      if (!hasBodyText)
      {
        closingLevel = level;
      }
      StringUtilities.appendIndentString(p_appendToMe, closingLevel).append("</").append(_definition.getName()).append(">\n");
    }

    return p_appendToMe;
  }

  @Override
  public MBElementDefinition getDefinition()
  {
    return _definition;
  }

  public void setDefinition(MBElementDefinition definition)
  {
    _definition = definition;
  }

  public boolean isValidAttribute(String attributeName)
  {
    return (getDefinition()).isValidAttribute(attributeName);
  }

  private void validateAttribute(String attributeName)
  {
    if (!isValidAttribute(attributeName))
    {
      String message = "Attribute \"" + attributeName + "\" not defined for element with name \"" + getDefinition().getName()
                       + "\". Use one of \"" + getDefinition().getAttributeNames() + "\"";

      throw new MBInvalidAttributeNameException(message);
    }
  }

  public String getBodyText()
  {
    if (isValidAttribute(TEXT_ATTRIBUTE))
    {
      return getValueForAttribute(TEXT_ATTRIBUTE);
    }
    return null;
  }

  public void setBodyText(String text)
  {
    setAttributeValue(text, TEXT_ATTRIBUTE);
  }

  public void assignToElement(MBElement target)
  {
    if (!target.getDefinition().getName().equals(_definition.getName()))
    {
      String message = "Cannot assign element since types differ: " + target.getDefinition().getName() + " != " + _definition.getName()
                       + " (use assignByName:)";
      throw new MBCannotAssignException(message);
    }

    target._values.clear();
    target._values.putAll(_values);
    target.getElements().clear();
    copyChildrenInto(target);
  }

  @Override
  public String getUniqueId()
  {
    String uid = "";
    uid += getDefinition().getName();
    for (MBAttributeDefinition def : _definition.getAttributes())
    {
      String attrName = def.getName();
      if (!attrName.equals("xmlns"))
      {
        String attrValue = _values.get(attrName);
        uid += "_";
        if (attrValue != null)
        {
          uid += cookValue(attrValue);
        }
      }
    }
    uid += super.getUniqueId();

    return uid;
  }

  @Override
  public void addAllPathsTo(Set<String> set, String currentPath)
  {
    String pathPrefix = currentPath + "/@";
    for (String attribute : _values.keySet())
    {
      set.add(pathPrefix + attribute);
    }
    super.addAllPathsTo(set, currentPath);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getValueForPathComponents(List<String> pathComponents, String originalPath, boolean nillIfMissing,
                                         List<String> translatedPathComponents)
  {
    if (pathComponents.size() > 0 && pathComponents.get(0).startsWith("@"))
    {
      String attrName = pathComponents.get(0);
      if (translatedPathComponents != null) translatedPathComponents.add(attrName);

      return (T) getValueForAttribute(attrName.substring(1));
    }
    else
    {
      return (T)super.getValueForPathComponents(pathComponents, originalPath, nillIfMissing, translatedPathComponents);
    }
  }

  public String cookValue(String uncooked)
  {
    // TODO Double check if this method was implemented properly

    if (uncooked == null)
    {
      return null;
    }

    String cooked = "";
    for (int i = 0; i < uncooked.length(); i++)
    {
      char c = uncooked.charAt(i);
      if (c < 32 || c == '&' || c == '\'' || c > 126)
      {
        cooked += "&#" + (int) c + ";";
      }
      else
      {
        cooked += c;
      }
    }

    return cooked;
  }

  public String attributeAsXml(String name, Object attrValue)
  {
    attrValue = cookValue((String) attrValue);

    if (attrValue == null)
    {
      return "";
    }

    return " " + name + "='" + attrValue + "'";
  }

  public void assignByName(MBElementContainer other)
  {
    other.deleteAllChildElements();

    MBElementDefinition def = getDefinition();
    for (MBAttributeDefinition attrDef : def.getAttributes())
    {
      if (((MBElementDefinition) other.getDefinition()).isValidAttribute(attrDef.getName()))
      {
        ((MBElement) other).setAttributeValue(getValueForAttribute(attrDef.getName()), attrDef.getName());
      }
    }

    for (String elementName : getElements().keySet())
    {
      for (MBElement src : getElements().get(elementName))
      {
        MBElement newElem = other.createElementWithName(src.getDefinition().getName());
        src.assignByName(newElem);
      }
    }

  }

  @Override
  public String getName()
  {
    return getDefinition().getName();
  }

  @Override
  public String toString()
  {
    StringBuffer rt = new StringBuffer();
    return asXmlWithLevel(rt, 0).toString();
  }

}