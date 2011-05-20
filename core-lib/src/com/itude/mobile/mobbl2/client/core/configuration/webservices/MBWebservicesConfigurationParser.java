package com.itude.mobile.mobbl2.client.core.configuration.webservices;

import java.util.Map;

import com.itude.mobile.mobbl2.client.core.configuration.MBConfigurationParser;
import com.itude.mobile.mobbl2.client.core.configuration.MBDefinition;
import com.itude.mobile.mobbl2.client.core.services.MBResultListenerDefinition;

public class MBWebservicesConfigurationParser extends MBConfigurationParser
{

  @Override
  public MBDefinition parseData(byte[] data, String documentName)
  {
    MBWebservicesConfiguration config = (MBWebservicesConfiguration) super.parseData(data, documentName);
    config.linkGlobalListeners();

    return config;
  }

  @Override
  public boolean processElement(String elementName, Map<String, String> attributeDict)
  {

    if (elementName.equals("EndPoints"))
    {
      MBWebservicesConfiguration confDef = new MBWebservicesConfiguration();
      getStack().push(confDef);
    }
    else if (elementName.equals("EndPoint"))
    {
      MBEndPointDefinition endpointDef = new MBEndPointDefinition();
      endpointDef.setDocumentIn(attributeDict.get("documentIn"));
      endpointDef.setDocumentOut(attributeDict.get("documentOut"));
      endpointDef.setEndPointUri(attributeDict.get("endPoint"));
      endpointDef.setCacheable(Boolean.parseBoolean(attributeDict.get("cacheable")));
      if (attributeDict.containsKey("ttl"))
      {
        endpointDef.setTtl(Integer.parseInt(attributeDict.get("ttl")));
      }
      if (attributeDict.containsKey("timeout"))
      {
        endpointDef.setTimeout(Integer.parseInt(attributeDict.get("timeout")));
      }
      else
      {
        endpointDef.setTimeout(300);
      }

      getStack().peek().addEndPoint(endpointDef);
      getStack().push(endpointDef);

    }
    else if (elementName.equals("ResultListener"))
    {
      MBResultListenerDefinition listenerDef = new MBResultListenerDefinition();
      listenerDef.setName(attributeDict.get("name"));
      listenerDef.setMatchExpression(attributeDict.get("matchExpression"));

      getStack().peek().addResultListener(listenerDef);
      getStack().push(listenerDef);
    }
    else
    {
      return false;
    }

    return true;
  }

  @Override
  public void didProcessElement(String elementName)
  {
    if (!elementName.equals("EndPoints"))
    {
      getStack().pop();
    }
  }

  @Override
  public boolean isConcreteElement(String element)
  {
    return element.equals("EndPoints") || element.equals("EndPoint") || element.equals("ResultListener");
  }

  @Override
  public boolean isIgnoredElement(String element)
  {
    return element.equals("ResultListeners");
  }

}