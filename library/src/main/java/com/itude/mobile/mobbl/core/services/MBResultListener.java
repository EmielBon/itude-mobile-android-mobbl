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
package com.itude.mobile.mobbl.core.services;

import com.itude.mobile.mobbl.core.model.MBDocument;

/**
 * Protocol for processing results call to a server.
 * <br/>
 * Classes which use this protocol can be added to the webservice endpoint definition file (typically endpoints.xml) to catch errors or provide logic for specific server responses.
 */
public interface MBResultListener {
    public void handleResult(String result, MBDocument requestDocument, MBResultListenerDefinition definition);

}
