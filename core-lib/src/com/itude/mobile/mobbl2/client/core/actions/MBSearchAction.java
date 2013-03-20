package com.itude.mobile.mobbl2.client.core.actions;

import com.itude.mobile.android.util.DeviceUtil;
import com.itude.mobile.mobbl2.client.core.MBException;
import com.itude.mobile.mobbl2.client.core.controller.MBAction;
import com.itude.mobile.mobbl2.client.core.controller.MBOutcome;
import com.itude.mobile.mobbl2.client.core.controller.MBViewManager;
import com.itude.mobile.mobbl2.client.core.model.MBDocument;
import com.itude.mobile.mobbl2.client.core.util.Constants;

public abstract class MBSearchAction implements MBAction
{
  private MBDocument _searchRequestDoc;
  private String     _query;
  private boolean    _progressiveSearch;
  private String     _outcomeNameNormal;
  private String     _outcomeNameProgressive;

  @Override
  public MBOutcome execute(MBDocument document, String path)
  {
    try
    {
      if (document == null || !Constants.C_DOC_SEARCH_REQUEST.equals(document.getDocumentName()))
      {
        throw new MBException("Wrong document! Expected an " + Constants.C_DOC_SEARCH_REQUEST);
      }

      _searchRequestDoc = document;

      _query = document.getValueForPath("SearchRequest[0]/@query");
      _progressiveSearch = document.getBooleanForPath("SearchRequest[0]/@isProgressive");
      _outcomeNameNormal = document.getValueForPath("SearchRequest[0]/@searchResultNormal");
      _outcomeNameProgressive = document.getValueForPath("SearchRequest[0]/@searchResultProgressive");

      MBDocument searchResult = executeSearch();

      // outcome to display the list of search results
      MBOutcome outcome = displaySearchResults(searchResult, path);
      return outcome;
    }
    finally
    {
      if (DeviceUtil.isTablet() || DeviceUtil.getInstance().isPhoneV14())
      {
        MBViewManager.getInstance().supportInvalidateOptionsMenu();
      }
    }
  }

  protected abstract MBDocument executeSearch();

  protected abstract MBOutcome displaySearchResults(MBDocument searchResult, String path);

  protected String getQuery()
  {
    return _query;
  }

  protected boolean isProgressiveSearch()
  {
    return _progressiveSearch;
  }

  protected String getOutcomeNameNormal()
  {
    return _outcomeNameNormal;
  }

  protected String getOutcomeNameProgressive()
  {
    return _outcomeNameProgressive;
  }

  public MBDocument getSearchRequestDoc()
  {
    return _searchRequestDoc;
  }

}
