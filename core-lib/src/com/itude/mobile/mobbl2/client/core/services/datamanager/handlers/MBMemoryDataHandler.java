package com.itude.mobile.mobbl2.client.core.services.datamanager.handlers;

import java.util.Hashtable;
import java.util.Map;

import android.util.Log;

import com.itude.mobile.mobbl2.client.core.configuration.mvc.MBDocumentDefinition;
import com.itude.mobile.mobbl2.client.core.model.MBDocument;
import com.itude.mobile.mobbl2.client.core.model.MBDocumentFactory;
import com.itude.mobile.mobbl2.client.core.services.MBMetadataService;
import com.itude.mobile.mobbl2.client.core.services.datamanager.MBDataHandlerBase;
import com.itude.mobile.mobbl2.client.core.util.DataUtil;
import com.itude.mobile.mobbl2.client.core.util.exceptions.MBDataParsingException;

public class MBMemoryDataHandler extends MBDataHandlerBase
{
  private final Map<String, MBDocument> _dictionary;

  public MBMemoryDataHandler()
  {
    super();
    _dictionary = new Hashtable<String, MBDocument>();
  }

  @Override
  public MBDocument loadDocument(String documentName)
  {
    MBDocument doc = _dictionary.get(documentName);
    if (doc == null)
    {
      // Not yet in the store; handle default construction of the document using a file as template
      String fileName = "documents/" + documentName + ".xml";
      byte[] data = null;
      try
      {
        data = DataUtil.getInstance().readFromAssetOrFile(fileName);
      }
      catch (MBDataParsingException e)
      {
        Log.d("MOBBL", "Unable to find file " + fileName + " in assets");
      }
      MBDocumentDefinition docDef = MBMetadataService.getInstance().getDefinitionForDocumentName(documentName);
      return MBDocumentFactory.getInstance().getDocumentWithData(data, MBDocumentFactory.PARSER_XML, docDef);
    }
    return doc;
  }

  @Override
  public MBDocument loadDocument(String documentName, MBDocument args)
  {
    return loadDocument(documentName);
  }

  @Override
  public void storeDocument(MBDocument document)
  {
    _dictionary.put(document.getName(), document);
  }

}