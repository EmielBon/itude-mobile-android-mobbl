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
package com.itude.mobile.mobbl.core.services.datahandlers;

import java.util.Date;

import android.test.ApplicationTestCase;

import com.itude.mobile.android.util.DataUtil;
import com.itude.mobile.mobbl.core.MBApplicationCore;
import com.itude.mobile.mobbl.core.model.MBDocument;
import com.itude.mobile.mobbl.core.model.MBElement;
import com.itude.mobile.mobbl.core.services.MBDataManagerService;
import com.itude.mobile.mobbl.core.services.MBMetadataService;

public class MBMemoryDataHandlerTest extends ApplicationTestCase<MBApplicationCore>
{

  public MBMemoryDataHandlerTest()
  {
    super(MBApplicationCore.class);

  }

  @Override
  protected void setUp() throws Exception
  {
    createApplication();
    DataUtil.getInstance().setContext(getContext());

    MBMetadataService.setConfigName("config/config.xml");
  }

  public void testLoadDocumentString()
  {
    MBDocument doc = MBDataManagerService.getInstance().loadDocument("TestDocument1");
    assertNotNull(doc);
  }

  public void testStoreDocument()
  {
    MBDocument doc = MBDataManagerService.getInstance().loadDocument("TestDocument1");
    MBDocument copy = doc.clone();
    MBElement el = ((MBElement) copy.getValueForPath("/LoginInfo[0]"));
    String testValue = String.valueOf((new Date()).getTime());
    el.setAttributeValue(testValue, "LoginMessage");
    MBDataManagerService.getInstance().storeDocument(copy);
    copy = MBDataManagerService.getInstance().loadDocument("TestDocument1");
    assertNotSame(doc, copy);
  }

}
