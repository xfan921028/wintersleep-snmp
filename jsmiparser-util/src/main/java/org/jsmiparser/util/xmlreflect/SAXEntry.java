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
package org.jsmiparser.util.xmlreflect;

import java.util.Map;

public class SAXEntry implements Map.Entry<String, String> {

    private String m_localName;
    private String m_value;

    public SAXEntry(String localName, String value) {
        m_localName = localName;
        m_value = value;
    }

    public String getKey() {
        return m_localName;
    }

    public String getValue() {
        return m_value;
    }

    public String setValue(String o) {
        throw new UnsupportedOperationException();
    }
}
