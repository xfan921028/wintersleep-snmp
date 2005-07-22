/*
 * Copyright 2005 Nigel Sheridan-Smith, Davy Verstappen.
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

package org.jsmiparser.antlr.asn1;

import java.util.*;

/**
 *
 * @author  Nigel Sheridan-Smith
 */
public class ASNExports {
    
    private boolean allExported;
    private List<String> exports;
    
    /** Creates a new instance of ASNExports */
    public ASNExports() {
    }
    
    public void setAllExported (boolean a)
    {
        allExported = a;
    }
    
    public boolean isAllExported ()
    {
        return allExported;
    }
    
    public void setExports (List<String> e)
    {
        exports = e;
    }
    
    public List<String> getExports ()
    {
        return exports;
    }
    
}
