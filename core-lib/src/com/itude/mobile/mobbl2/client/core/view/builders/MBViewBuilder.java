package com.itude.mobile.mobbl2.client.core.view.builders;

import java.util.List;

import android.view.View;
import android.view.ViewGroup;

import com.itude.mobile.mobbl2.client.core.controller.MBViewManager;
import com.itude.mobile.mobbl2.client.core.view.MBComponent;
import com.itude.mobile.mobbl2.client.core.view.MBField;

public class MBViewBuilder
{

  public void buildChildren(List<? extends MBComponent> children, ViewGroup view, MBViewManager.MBViewState viewState)
  {
    for (MBComponent child : children)
    {
      getStyleHandler().applyInsetsForComponent(child);

      View childView = child.buildViewWithMaxBounds(viewState);
      if (childView == null) continue;

      view.addView(childView);
    }
  }

  public boolean isComponentOfType(MBComponent p_child, String p_type)
  {
    return p_child instanceof MBField && ((MBField) p_child).getType() != null && ((MBField) p_child).getType().equals(p_type);
  }

  public MBStyleHandler getStyleHandler()
  {
    return MBViewBuilderFactory.getInstance().getStyleHandler();
  }

}