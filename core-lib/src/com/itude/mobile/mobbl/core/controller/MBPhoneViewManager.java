/*
 * (C) Copyright Itude Mobile B.V., The Netherlands
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itude.mobile.mobbl.core.controller;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;

import com.itude.mobile.mobbl.core.view.components.tabbar.MBActionBarBuilder;
import com.itude.mobile.mobbl.core.view.components.tabbar.MBTabletActionBarBuilder;

public class MBPhoneViewManager extends MBViewManager
{
  // End of Android hooks

  @Override
  public void setContentView(int id)
  {
    setContentView(getLayoutInflater().inflate(id, null));
  }

  @Override
  public void setContentView(View v)
  {
    setContentView(v, new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
  }

  @Override
  protected MBActionBarBuilder getDefaultActionBar()
  {
    return new MBTabletActionBarBuilder(this);
  }
}