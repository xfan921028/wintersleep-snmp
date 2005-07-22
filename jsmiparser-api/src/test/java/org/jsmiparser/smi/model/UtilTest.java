/*
 * Copyright 2005 Davy Verstappen.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jsmiparser.smi.model;

import junit.framework.TestCase;
import org.jsmiparser.smi.SmiUtil;

public class UtilTest extends TestCase {
    public void testStripLastOidPart() {
        String colOid = "1.2.3";

        String rowOid = SmiUtil.stripLastOidPart(colOid);
        assertEquals("1.2", rowOid);

        String tableOid = SmiUtil.stripLastOidPart(rowOid);
        assertEquals("1", tableOid);
    }

}
