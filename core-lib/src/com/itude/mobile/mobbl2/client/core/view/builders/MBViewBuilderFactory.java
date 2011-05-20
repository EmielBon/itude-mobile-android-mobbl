package com.itude.mobile.mobbl2.client.core.view.builders;

public class MBViewBuilderFactory
{
  private static MBViewBuilderFactory _instance;

  private MBPanelViewBuilder          _panelViewBuilder;
  private MBPageViewBuilder           _pageViewBuilder;
  private MBForEachViewBuilder        _forEachViewBuilder;
  private MBRowViewBuilder            _rowViewBuilder;
  private MBFieldViewBuilder          _fieldViewBuilder;
  private MBStyleHandler              _styleHandler;

  private MBViewBuilderFactory()
  {
    _panelViewBuilder = new MBPanelViewBuilder();
    _pageViewBuilder = new MBPageViewBuilder();
    _forEachViewBuilder = new MBForEachViewBuilder();
    _rowViewBuilder = new MBRowViewBuilder();
    _fieldViewBuilder = new MBFieldViewBuilder();
    _styleHandler = new MBStyleHandler();
  }

  public static MBViewBuilderFactory getInstance()
  {
    if (_instance == null)
    {
      _instance = new MBViewBuilderFactory();
    }

    return _instance;
  }

  public static void setInstance(MBViewBuilderFactory factory)
  {
    if (_instance != null && _instance != factory)
    {
      _instance = null;
    }
    _instance = factory;
  }

  public MBPanelViewBuilder getPanelViewBuilder()
  {
    return _panelViewBuilder;
  }

  public void setPanelViewBuilder(MBPanelViewBuilder panelViewBuilder)
  {
    _panelViewBuilder = panelViewBuilder;
  }

  public MBPageViewBuilder getPageViewBuilder()
  {
    return _pageViewBuilder;
  }

  public void setPageViewBuilder(MBPageViewBuilder pageViewBuilder)
  {
    _pageViewBuilder = pageViewBuilder;
  }

  public MBForEachViewBuilder getForEachViewBuilder()
  {
    return _forEachViewBuilder;
  }

  public void setForEachViewBuilder(MBForEachViewBuilder forEachViewBuilder)
  {
    _forEachViewBuilder = forEachViewBuilder;
  }

  public MBRowViewBuilder getRowViewBuilder()
  {
    return _rowViewBuilder;
  }

  public void setRowViewBuilder(MBRowViewBuilder rowViewBuilder)
  {
    _rowViewBuilder = rowViewBuilder;
  }

  public MBFieldViewBuilder getFieldViewBuilder()
  {
    return _fieldViewBuilder;
  }

  public void setFieldViewBuilder(MBFieldViewBuilder fieldViewBuilder)
  {
    _fieldViewBuilder = fieldViewBuilder;
  }

  public MBStyleHandler getStyleHandler()
  {
    return _styleHandler;
  }

  public void setStyleHandler(MBStyleHandler styleHandler)
  {
    _styleHandler = styleHandler;
  }

}