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
package com.itude.mobile.mobbl.core.util;

import java.io.File;
import java.lang.reflect.Field;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.itude.mobile.mobbl.core.MBApplicationCore;
import com.itude.mobile.mobbl.core.model.MBDocument;
import com.itude.mobile.mobbl.core.services.MBDataManagerService;
import com.itude.mobile.mobbl.core.services.MBMetadataService;

public class MBCacheManagerTest extends ApplicationTestCase<MBApplicationCore>
{
  public final static String C_GENERIC_REST_REQUEST = "MBGenericRestRequest";
  public final static String C_EMPTY_DOC            = "MBEmptyDoc";

  public MBCacheManagerTest()
  {
    super(MBApplicationCore.class);
  }

  @Override
  protected void setUp() throws Exception
  {
    createApplication();
    MBMetadataService.setConfigName("unittests/config_unittests.xml");
    MBMetadataService.setEndpointsName("testconfig/endpoints.xml");
    super.setUp();
  }

  public void testInit()
  {
    MBCacheManager cacheManager = MBCacheManager.getInstance();
    assertNotNull(cacheManager);
    Field[] fields = cacheManager.getClass().getDeclaredFields();
    for (Field field : fields)
    {
      if (!("_operationQueue".equals(field.getName())))
      {
        field.setAccessible(true);
        try
        {
          assertNotNull("Field " + field.getName() + " is null", field.get(cacheManager));
        }
        catch (Exception e)
        {
          fail(e.getMessage());
        }
      }
    }
  }

  public void testCache()
  {
    String test = "Test string for testing the MBCacheManager";
    doPutInCache("test", test.getBytes(), 0);
    doGetFromCache("test", test.getBytes());
  }

  public void testCacheExpiration()
  {
    String test = "Test string for testing the MBCacheManager";
    doPutInCache("test", test.getBytes(), 0);

    // test successful caching
    doGetFromCache("test", test.getBytes());

    // now test expiration
    MBCacheManager.expireDataForKey("test");
    doGetFromCache("test", null);
  }

  public void testCacheTimedExpiration()
  {
    String test = "Test string for testing the MBCacheManager";
    doPutInCache("test", test.getBytes(), 3); // cache for 3 seconds

    // test successful caching
    doGetFromCache("test", test.getBytes());

    // wait for 5 seconds
    try
    {
      Thread.sleep(5000);
    }
    catch (Exception e)
    {
      Log.d(MBConstants.APPLICATION_NAME, "Thread.sleep failed");
    }

    // now test expiration
    doGetFromCache("test", null);
  }

  public void testDocumentCache()
  {
    MBDocument document = MBDataManagerService.getInstance().loadDocument(C_GENERIC_REST_REQUEST);
    assertNotNull(document);

    doPutDocumentInCache(document.getUniqueId(), document, 0);
    doGetDocumentFromCache(document.getUniqueId(), document);
  }

  public void testDocumentCacheExpiration()
  {
    MBDocument document = MBDataManagerService.getInstance().loadDocument(C_GENERIC_REST_REQUEST);
    assertNotNull(document);
    doPutDocumentInCache(document.getUniqueId(), document, 0);

    // test successful caching
    doGetDocumentFromCache(document.getUniqueId(), document);

    // now test expiration
    MBCacheManager.expireDocumentForKey(document.getUniqueId());

    doGetDocumentFromCache(document.getUniqueId(), null);
  }

  public void testDocumentCacheTimedExpiration()
  {
    MBDocument document = MBDataManagerService.getInstance().loadDocument(C_GENERIC_REST_REQUEST);
    assertNotNull(document);
    doPutDocumentInCache(document.getUniqueId(), document, 3); // cache for 3 seconds

    // test successful caching
    doGetDocumentFromCache(document.getUniqueId(), document);

    // wait for 5 seconds
    try
    {
      Thread.sleep(5000);
    }
    catch (Exception e)
    {
      Log.d(MBConstants.APPLICATION_NAME, "Thread.sleep failed");
    }

    // now test expiration
    doGetDocumentFromCache(document.getUniqueId(), null);
  }

  public void testExpireAllDocumentsInCache()
  {
    MBDocument documentOne = MBDataManagerService.getInstance().loadDocument(C_GENERIC_REST_REQUEST);
    assertNotNull(documentOne);

    MBDocument documentTwo = MBDataManagerService.getInstance().loadDocument(C_EMPTY_DOC);
    assertNotNull(documentTwo);

    doPutDocumentInCache(documentOne.getUniqueId(), documentOne, 0);
    doPutDocumentInCache(documentTwo.getUniqueId(), documentTwo, 0);

    // test successful caching
    doGetDocumentFromCache(documentOne.getUniqueId(), documentOne);
    doGetDocumentFromCache(documentTwo.getUniqueId(), documentTwo);

    // now test expiration
    MBCacheManager.expireAllDocuments();

    doGetDocumentFromCache(documentOne.getUniqueId(), null);
    doGetDocumentFromCache(documentTwo.getUniqueId(), null);
  }

  private void doPutInCache(String key, byte[] data, int ttls)
  {
    MBCacheManager.setData(data, key, ttls);
    // lets give MBCacheManager the oppertunity to write to file
    try
    {
      Thread.sleep(2000);
    }
    catch (Exception e)
    {
      fail("Sleep went wrong");
    }
  }

  private void doPutDocumentInCache(String key, MBDocument document, int ttls)
  {
    MBCacheManager.setDocument(document, document.getUniqueId(), ttls);
    // lets give MBCacheManager the oppertunity to write to file
    try
    {
      Thread.sleep(2000);
    }
    catch (Exception e)
    {
      fail("Sleep went wrong");
    }
  }

  private void doGetFromCache(String key, byte[] expected)
  {
    byte[] resultBytes = MBCacheManager.getDataForKey(key);
    if (expected == null) assertNull(resultBytes);
    else
    {
      assertTrue(resultBytes.length == expected.length);
      for (int i = 0; i < resultBytes.length; i++)
        assertEquals(expected[i], resultBytes[i]);
    }
  }

  private void doGetDocumentFromCache(String key, MBDocument expected)
  {
    MBDocument result = MBCacheManager.documentForKey(key);
    if (expected == null) assertNull(result);
    else
    {
      assertNotNull(result);
      assertEquals(expected.getUniqueId(), result.getUniqueId());
    }
  }

  @Override
  protected void tearDown() throws Exception
  {
    File cacheDir = new File(getContext().getFilesDir(), "cache");
    if (cacheDir.exists() && cacheDir.isDirectory())
    {
      File[] files = cacheDir.listFiles();
      if (files.length > 0)
      {
        for (File file : files)
          file.delete();
      }
      cacheDir.delete();
    }
    super.tearDown();
  }

}
