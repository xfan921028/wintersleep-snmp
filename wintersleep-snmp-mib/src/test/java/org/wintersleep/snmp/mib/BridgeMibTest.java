/*
 * Copyright 2006 Davy Verstappen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wintersleep.snmp.mib;

import org.wintersleep.snmp.mib.smi.*;
import org.wintersleep.snmp.util.url.DefaultURLListBuilder;

import java.io.File;
import java.util.List;

public class BridgeMibTest extends AbstractMibTestCase {

    public BridgeMibTest() {
        super(SmiVersion.V2);
    }

    @Override
    protected void addUrls(DefaultURLListBuilder builder) {
        super.addUrls(builder);
        builder.addDir(LIBSMI_IANA_DIR,
                "IANAifType-MIB");
        builder.addDir(LIBSMI_IETF_DIR,
                "IF-MIB",
                "BRIDGE-MIB");
    }

    public void testBridgeMib() {
        SmiType integer32 = getInteger32();

        SmiModule bridgeMib = getMib().findModule("BRIDGE-MIB");
        assertEquals("BRIDGE-MIB", bridgeMib.getId());

        SmiTextualConvention timeout = getMib().getTextualConventions().find("Timeout");
        assertNotNull(timeout);
        assertEquals(integer32, timeout.getBaseType());
        assertEquals(SmiPrimitiveType.INTEGER_32, timeout.getPrimitiveType());
        assertNull(timeout.getRangeConstraints());

        SmiVariable dot1dStpBridgeMaxAge = getMib().getVariables().find("dot1dStpBridgeMaxAge");
        assertNotNull(dot1dStpBridgeMaxAge);
        SmiType anonymousType = dot1dStpBridgeMaxAge.getType();
        assertNull(anonymousType.getIdToken());
        assertSame(timeout, anonymousType.getBaseType());
        assertEquals(SmiPrimitiveType.INTEGER_32, dot1dStpBridgeMaxAge.getType().getPrimitiveType());
        List<SmiRange> dot1dStpBridgeMaxAgeRangeConstraints = dot1dStpBridgeMaxAge.getType().getRangeConstraints();
        assertEquals(1, dot1dStpBridgeMaxAgeRangeConstraints.size());
        assertEquals(600, dot1dStpBridgeMaxAgeRangeConstraints.get(0).getMinValue().intValue());
        assertEquals(4000, dot1dStpBridgeMaxAgeRangeConstraints.get(0).getMaxValue().intValue());
    }


}
