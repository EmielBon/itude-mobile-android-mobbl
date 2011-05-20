package com.itude.mobile.mobbl2.client.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import com.itude.mobile.mobbl2.client.core.configuration.MBDefinition;
import com.itude.mobile.mobbl2.client.core.configuration.mvc.MBDocumentDefinition;
import com.itude.mobile.mobbl2.client.core.configuration.mvc.MBElementDefinition;
import com.itude.mobile.mobbl2.client.core.configuration.mvc.exceptions.MBInvalidPathException;
import com.itude.mobile.mobbl2.client.core.model.exceptions.MBDocumentNotFoundException;
import com.itude.mobile.mobbl2.client.core.model.exceptions.MBIndexOutOfBoundsException;
import com.itude.mobile.mobbl2.client.core.model.exceptions.MBNoIndexSpecifiedException;
import com.itude.mobile.mobbl2.client.core.services.MBDataManagerService;
import com.itude.mobile.mobbl2.client.core.services.MBScriptService;
import com.itude.mobile.mobbl2.client.core.util.MBDynamicAttributeComparator;
import com.itude.mobile.mobbl2.client.core.util.StringUtilities;

public class MBElementContainer
{
  private Map<String, List<MBElement>> _elements; // Dictionaryoflistsofelements
  private MBElementContainer           _parent;

  public MBElementContainer()
  {
    _elements = new HashMap<String, List<MBElement>>();
  }

  public void copyChildrenInto(MBElementContainer other)
  {
    for (String elementName : _elements.keySet())
    {
      for (MBElement src : _elements.get(elementName))
      {
        MBElement copy = src.clone();
        other.addElement(copy);
      }
    }
  }

  public void addAllPathsTo(Set<String> set, String currentPath)
  {
    for (String elementName : _elements.keySet())
    {
      int idx = 0;
      String pathPrefix = currentPath + "/" + elementName + "[";
      for (MBElement element : _elements.get(elementName))
      {
        String path = pathPrefix + (idx++) + "]";
        element.addAllPathsTo(set, path);
      }
    }
  }

  public int evaluateIndexExpression(String combinedExpression, String elementName)
  {
    List<String> matchAttributes = new ArrayList<String>();
    List<String> matchValues = new ArrayList<String>();

    String[] expressions = combinedExpression.split(" and ");

    for (String expression : expressions)
    {

      int eqPos = expression.indexOf("=");
      String attrName = StringUtilities.stripCharacters(expression.substring(0, eqPos), " ");
      String valueExpression = expression.substring(eqPos + 1);

      attrName = substituteExpressions(attrName, attrName, null);

      valueExpression = StringUtilities.stripCharacters(valueExpression, "'\"");

      String value = substituteExpressions(valueExpression, valueExpression, null);

      matchAttributes.add(attrName);
      matchValues.add(value);
    }

    List<MBElement> elements = getElementsWithName(elementName);
    for (int i = 0; i < elements.size(); i++)
    {
      boolean match = true;
      for (int j = 0; match && j < matchAttributes.size(); j++)
      {
        String attrName = matchAttributes.get(j);
        String value = matchValues.get(j);
        match &= elements.get(i).getValueForAttribute(attrName).equals(value);
      }
      if (match)
      {
        return i;
      }
    }

    // Return an index that exceeds the size of the elements array; this will be handled by if([rootList count] <= idx) below
    // i.e. if nillIfMissing is TRUE then a not matching expression will also return nil because of this:

    return elements.size();
  }

  public MBElementContainer getParent()
  {
    return _parent;
  }

  public void setParent(MBElementContainer parent)
  {
    _parent = parent;
  }

  public Object init()
  {
    return null;
  }

  public MBElement createElementWithName(String name)
  {
    Stack<String> pathComponents = new Stack<String>();
    pathComponents.addAll(StringUtilities.splitPath(name));

    if (pathComponents.size() > 1)
    {
      String elementName = pathComponents.pop();

      MBElement target = (MBElement) getValueForPathComponents(pathComponents, name, false, null);

      return target.createElementWithName(elementName);
    }
    else
    {
      MBElementDefinition childDef = null;
      if (getDefinition() instanceof MBElementDefinition) childDef = ((MBElementDefinition) getDefinition()).getChildWithName(name);
      else if (getDefinition() instanceof MBDocumentDefinition) childDef = ((MBDocumentDefinition) getDefinition()).getChildWithName(name);
      MBElement element = childDef.createElement();
      addElement(element);

      return element;
    }
  }

  public void deleteElementWithName(String name, int index)
  {
    List<MBElement> elementContainer = getElementsWithName(name);

    if (index < 0 || index >= elementContainer.size())
    {
      String message = "Invalid index (" + index + ") for element with name " + name + " (count=" + elementContainer.size() + ")";
      throw new MBInvalidPathException(message);
    }

    elementContainer.remove(index);
  }

  public void deleteAllChildElements()
  {
    _elements.clear();
  }

  public void addElement(MBElement element)
  {
    String name = element.getDefinition().getName();
    element.setParent(this);

    List<MBElement> elemContainer = getElementsWithName(name);
    if (elemContainer == null)
    {
      elemContainer = new ArrayList<MBElement>();
      _elements.put(name, elemContainer);
    }
    elemContainer.add(element);
  }

  public Map<String, List<MBElement>> getElements()
  {
    return _elements;
  }

  public void setElements(Map<String, List<MBElement>> elements)
  {
    _elements = elements;
  }

  public <T> T getValueForPath(String path)
  {
    return (T)getValueForPath(path, null);
  }

  public <T> T getValueForPath(String path, List<String> translatedPathComponents)
  {
    if (path == null)
    {
      return null;
    }

    List<String> pathComponents = StringUtilities.splitPath(path);

    // If there is a ':' in the name of the first component; we might need a different document than 'self'
    if (pathComponents.size() > 0)
    {
      int range = pathComponents.get(0).indexOf(":");

      if (range > -1)
      {
        String documentName = pathComponents.get(0).substring(0, range);
        String rootElementName = pathComponents.get(0).substring(range + 1);

        if (!documentName.equals(this.getDocumentName()))
        {
          if (translatedPathComponents == null)
          {
            translatedPathComponents = new ArrayList<String>();
          }

          // Different document! Dispatch the valueForPath
          translatedPathComponents.add(documentName + ":");
          MBDocument doc = getDocumentFromSharedContext(documentName);
          if (rootElementName.length() > 0)
          {
            pathComponents.set(0, rootElementName);
          }
          else
          {
            pathComponents.remove(0);
          }

          return (T)doc.getValueForPathComponents(pathComponents, path, true, translatedPathComponents);
        }
        else
        {
          pathComponents.set(0, rootElementName);
        }
      }
    }

    return (T)getValueForPathComponents(pathComponents, path, true, translatedPathComponents);
  }

  public void setValue(String value, String path)
  {
    Stack<String> pathComponents = new Stack<String>();
    pathComponents.addAll(StringUtilities.splitPath(path));

    String attributeName = new String(pathComponents.lastElement());
    if (attributeName.startsWith("@"))
    {
      pathComponents.pop();
      attributeName = attributeName.substring(1);

      MBElement element = (MBElement) getValueForPathComponents(pathComponents, path, false, null);
      element.setAttributeValue(value, attributeName);
    }
    else
    {
      String message = "Identitifer " + attributeName + " in Path " + path + " does not specify an attribute; cannot set value";
      throw new MBInvalidPathException(message);
    }

  }

  public List<MBElement> getElementsWithName(String name)
  {
    if (name.equals("*"))
    {
      List<MBElement> result = new ArrayList<MBElement>();
      for (List<MBElement> lst : _elements.values())
      {
        result.addAll(lst);
      }

      return result;
    }
    else
    {
      List<MBElement> result = _elements.get(name);
      if (result == null)
      {
        // not found, check if the name is a valid child at all
        if (!getDefinition().isValidChild(name))
        {
          String message = "Child element with name " + name + " not present";
          throw new MBInvalidPathException(message);
        }
        // ok, it is valid. return an empty list.
        result = new ArrayList<MBElement>();
        _elements.put(name, result);
      }

      return result;
    }

  }

  public MBDefinition getDefinition()
  {
    return null;
  }

  @SuppressWarnings("unchecked")
  public <T> T getValueForPathComponents(List<String> pathComponents, String originalPath, boolean nillIfMissing,
                                         List<String> translatedPathComponents)
  {
    if (pathComponents.size() == 0) return (T) this;

    String[] rootNameParts = splitPathOnBrackets(pathComponents.get(0));
    // hello
    String childElementName = rootNameParts[0];

    int idx = determineIndex(rootNameParts, childElementName);
    pathComponents.remove(0);
    List<MBElement> allElementsWithSameNameAsChild = getElementsWithName(childElementName);
    if (idx == -99)
    {
      // this was not an indexed path (just hello, not hello[1234])
      if (pathComponents.size() == 0)
      {
        return (T) allElementsWithSameNameAsChild;
      }
      String message = "No index specified for " + childElementName + " in path" + originalPath;
      throw new MBNoIndexSpecifiedException(message);
    }
    else if (idx < 0)
    {
      String message = "Illegal index " + idx + " for " + childElementName + " in path " + originalPath;
      throw new MBIndexOutOfBoundsException(message);
    }

    if (allElementsWithSameNameAsChild.size() <= idx)
    {
      if (nillIfMissing)
      {
        return null;
      }
      String message = "Index " + idx + " exceeds " + (allElementsWithSameNameAsChild.size() - 1) + " for " + childElementName + " in path " + originalPath;
      throw new MBIndexOutOfBoundsException(message);
    }

    MBElement root = allElementsWithSameNameAsChild.get(idx);
    if (translatedPathComponents != null)
      translatedPathComponents.add(root.getName() + "[" + idx + "]");
    return (T)root.getValueForPathComponents(pathComponents, originalPath, nillIfMissing, translatedPathComponents);
  }

  private String[] splitPathOnBrackets(String fullPath)
  {
    String[] rootNameParts;
    // hello[1] is split into hello and 1 (or just hello if the path is "hello")
    int indexOpenBracket = fullPath.indexOf('[');
    boolean isIndexedComponent = indexOpenBracket > -1;
    if (isIndexedComponent)
    {
      rootNameParts = new String[2];
      rootNameParts[0] = fullPath.substring(0, indexOpenBracket); // hello
      int indexCloseBracket = fullPath.indexOf(']', indexOpenBracket);
      if (indexCloseBracket<0)
        rootNameParts[1] = fullPath.substring(indexOpenBracket + 1);
      else
      {
        rootNameParts[1] = fullPath.substring(indexOpenBracket + 1, indexCloseBracket); 
      }
        
    }
    else
    {
      rootNameParts = new String[]{fullPath};
    }
    return rootNameParts;
  }

  // hello[0] returns 0, hello[123] returns 123, -99 when it is not an indexed path (just hello)
  private int determineIndex(String[] rootNameParts, String childElementName)
  {
    if (rootNameParts.length > 1)
    {
      // so it was an indexed path (hello[1])
      String idxStr = rootNameParts[1];
      if (idxStr.indexOf('=') > -1)
      {
        return evaluateIndexExpression(idxStr, childElementName);
      }
      else
      {
        return Integer.parseInt(idxStr);
      }
    }
    return -99;
  }

  public String getName()
  {
    return getDefinition().getName();
  }

  public Map<String, MBDocument> getSharedContext()
  {
    return getParent().getSharedContext();
  }

  public void setSharedContext(Map<String, MBDocument> sharedContext)
  {
    getParent().setSharedContext(sharedContext);
  }

  public MBDocument getDocumentFromSharedContext(String documentName)
  {
    MBDocument result = getSharedContext().get(documentName);
    if (result == null)
    {
      result = MBDataManagerService.getInstance().loadDocument(documentName);
      if (result == null)
      {
        String message = "Could not load document with name " + documentName;
        throw new MBDocumentNotFoundException(message);
      }
      registerDocumentWithSharedContext(result);
    }

    return result;
  }

  public void registerDocumentWithSharedContext(MBDocument document)
  {
    document.setSharedContext(getSharedContext());
    getSharedContext().put(document.getName(), document);
  }

  public MBDocument getDocument()
  {
    return getParent().getDocument();
  }

  public String getDocumentName()
  {
    return getParent().getDocumentName();
  }

  public String substituteExpressions(String expression, String nilMarker, String currentPath)
  {
    if (expression == null)
    {
      return null;
    }

    if (!expression.contains("{"))
    {
      return expression;
    }

    String subPart = "";
    String singleExpression;

    String result = "";

    int position = 0;
    int subPartPosition = -1;
    while ((position = expression.indexOf("${")) > -1)
    {
      result += expression.substring(0, position);
      expression = expression.substring(position + 2);

      if ((subPartPosition = expression.indexOf("}")) != -1)
      {
        subPart = expression.substring(subPartPosition + 1);

        singleExpression = expression.substring(0, subPartPosition);

        if (singleExpression.startsWith(".") && currentPath != null && currentPath.length() > 0)
        {

          singleExpression = currentPath + "/" + singleExpression;
        }

        if (expression.length() > subPartPosition + 2)
        {
          expression = expression.substring(subPartPosition + 2);
        }
        else
        {
          expression = "";
        }
        String value = (String) getValueForPath(singleExpression);
        if (value != null)
        {
          result += value;
        }
        else
        {
          result += nilMarker;
        }

      }

    }

    result += subPart;

    return result;
  }

  public String evaluateExpression(String expression)
  {
    return evaluateExpression(expression, null);
  }

  public String evaluateExpression(String expression, String currentPath)
  {
    String translated = substituteExpressions(expression, "null", currentPath);
    return MBScriptService.getInstance().evaluate(translated);
  }

  public String getUniqueId()
  {
    String uid = "";
    for (String elementName : _elements.keySet())
    {
      int idx = 0;
      for (MBElement element : _elements.get(elementName))
      {
        uid += "[" + idx + "_" + element.getUniqueId();
      }
    }

    return uid;
  }

  //Sorts on the given attribute(s) Multiple attributes must be separated by ,
  //Descending sort on an attribute can be done by prefixing the attribute with a -
  public void sortElements(String elementName, String attributeNames)
  {
    List<MBElement> elements = getElementsWithName(elementName);
    if (elements.size() == 0)
    {
      return;
    }

    Vector<Object[]> trace = new Vector<Object[]>();

    for (String attrSpec : attributeNames.split(","))
    {
      attrSpec = attrSpec.trim();
      boolean ascending = attrSpec.startsWith("+") || !attrSpec.startsWith("-");
      if (attrSpec.startsWith("+") || attrSpec.startsWith("-"))
      {
        attrSpec = attrSpec.substring(1);
      }

      trace.add(new Object[]{attrSpec, ascending});
    }

    Collections.sort(elements, new MBDynamicAttributeComparator(trace));

    getDocument().clearPathCache();
  }

}