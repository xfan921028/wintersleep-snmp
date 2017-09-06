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

import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;
import org.wintersleep.snmp.mib.parser.SmiDefaultParser;
import org.wintersleep.snmp.mib.parser.SmiParser;
import org.wintersleep.snmp.mib.smi.*;
import org.wintersleep.snmp.util.problem.annotations.ProblemSeverity;
import org.wintersleep.snmp.util.url.FileURLListFactory;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public abstract class AbstractMibTestCase extends TestCase {

    public static final String LIBSMI_MIBS_URL = findMibs() + "/libsmi";
    public static final String LIBSMI_MIBS_DIR = LIBSMI_MIBS_URL;

    private static final Logger m_log = LoggerFactory.getLogger(AbstractMibTestCase.class);

    private final SmiVersion version;
    // TODO: this can probably be factored out into a
    private final URL[] urls;
    private final File[] files;
    private final String[] resources;

    private static ThreadLocal<Class<? extends AbstractMibTestCase>> m_testClass = new ThreadLocal<Class<? extends AbstractMibTestCase>>();
    private static ThreadLocal<SmiMib> m_mib = new ThreadLocal<SmiMib>();

    private SmiType m_integer32;
    private SmiType m_counter;
    private SmiDefaultParser m_parser;


    private static String findMibs() {
        File pwd = Paths.get("").toAbsolutePath().toFile();
        File parent = pwd.getParentFile();
        while (parent != null) {
            File mibs = new File(parent, "mibs");
            if (mibs.exists()) {
                return mibs.getAbsolutePath();
            }
            parent = parent.getParentFile();
        }
        throw new IllegalStateException("Cannot find mibs directory.");
    }

    public AbstractMibTestCase() {
        this.version = null;
        this.urls = new URL[0];
        this.files = new File[0];
        this.resources = new String[0];
    }

    public AbstractMibTestCase(SmiVersion version) {
        this.version = version;
        this.urls = new URL[0];
        this.files = new File[0];
        this.resources = new String[0];
    }

    public AbstractMibTestCase(SmiVersion version, URL... urls) {
        for (URL url : urls) {
            if (url == null) {
                throw new IllegalArgumentException("Null URLs are not allowed.");
            }
        }
        this.version = version;
        this.files = new File[0];
        this.urls = urls;
        this.resources = new String[0];
    }

    public AbstractMibTestCase(SmiVersion version, File... files) {
        for (File file : files) {
            if (file == null) {
                throw new IllegalArgumentException("Null files are not allowed.");
            }
        }
        this.version = version;
        this.urls = new URL[0];
        this.files = files;
        this.resources = new String[0];
    }

    public AbstractMibTestCase(SmiVersion version, String... resources) {
        for (String resource : resources) {
            if (resource == null) {
                throw new IllegalArgumentException("Null resources are not allowed.");
            }
        }
        this.version = version;
        this.urls = new URL[0];
        this.files = new File[0];
        this.resources = resources;
    }


    protected SmiDefaultParser getParser() {
        return m_parser;
    }

    protected SmiMib getMib() {
        // this is a rather ugly hack to mimic JUnit4 @BeforeClass, without having to annotate all test methods:
        if (m_mib.get() == null || m_testClass.get() != getClass()) {
            try {
                SmiParser parser = createParser();
                StopWatch stopWatch = new StopWatch();
                stopWatch.start();
                SmiMib mib = parser.parse();
                stopWatch.stop();
                m_log.info("Parsing time: " + stopWatch.getTotalTimeSeconds() + " s");
                if (mustParseSuccessfully()) {
                    assertTrue(((SmiDefaultParser) parser).getProblemEventHandler().isOk());
                    assertEquals(0, ((SmiDefaultParser) parser).getProblemEventHandler().getSeverityCount(ProblemSeverity.ERROR));
                }
                m_mib.set(mib);
                m_testClass.set(getClass());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return m_mib.get();
    }

    protected boolean mustParseSuccessfully() {
        return true;
    }

    protected SmiParser createParser() throws Exception {
        FileURLListFactory urlListFactory = new FileURLListFactory(LIBSMI_MIBS_URL + "/ietf");

        if (version == null || version == SmiVersion.V1) {
            urlListFactory.add("RFC1155-SMI");
        }
        if (version == null || version == SmiVersion.V2) {
            urlListFactory.add("SNMPv2-SMI");
            urlListFactory.add("SNMPv2-TC");
            urlListFactory.add("SNMPv2-CONF");
            urlListFactory.add("SNMPv2-MIB");
        }

        // TODO: fix hack
        List<URL> urls = urlListFactory.create();
        urls.addAll(Arrays.asList(this.urls));
        for (File file : getFiles()) {
            urls.add(file.toURI().toURL());
        }
        for (String resource : resources) {
            URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
            if (url == null) {
                throw new IllegalArgumentException("Could not find resource: " + resource);
            }
            urls.add(url);
        }

        m_parser = new SmiDefaultParser();
        m_parser.getFileParserPhase().setInputUrls(urls);
        return m_parser;
    }

    public final File[] getFiles() {
        return files;
    }

    public SmiType getInteger32() {
        if (m_integer32 == null) {
            m_integer32 = getMib().getTypes().find("Integer32");
            assertSame(SmiConstants.INTEGER_TYPE, m_integer32.getBaseType());
            assertSame(SmiPrimitiveType.INTEGER_32, m_integer32.getPrimitiveType());
            assertEquals(1, m_integer32.getRangeConstraints().size());
            assertEquals(-2147483648, m_integer32.getRangeConstraints().get(0).getMinValue().intValue());
            assertEquals(2147483647, m_integer32.getRangeConstraints().get(0).getMaxValue().intValue());
            assertNull(m_integer32.getSizeConstraints());
            assertNull(m_integer32.getEnumValues());
            assertNull(m_integer32.getBitFields());
            assertNull(m_integer32.getFields());
        }
        return m_integer32;
    }

    public SmiType getCounter() {
        if (m_counter == null) {
            m_counter = getMib().getTypes().find("Counter");
            assertSame(SmiConstants.INTEGER_TYPE, m_counter.getBaseType());
            assertSame(SmiPrimitiveType.COUNTER_32, m_counter.getPrimitiveType());
            assertEquals(1, m_counter.getRangeConstraints().size());
            assertEquals(0, m_counter.getRangeConstraints().get(0).getMinValue().intValue());
            assertEquals(0xFFFFFFFFL, m_counter.getRangeConstraints().get(0).getMaxValue().longValue());
            assertNull(m_counter.getSizeConstraints());
            assertNull(m_counter.getEnumValues());
            assertNull(m_counter.getBitFields());
            assertNull(m_counter.getFields());
        }
        return m_counter;
    }

    protected void showOverview() {
        for (SmiModule module : m_mib.get().getModules()) {
            for (SmiSymbol symbol : module.getSymbols()) {
                String msg = module.getId() + ": " + symbol.getId() + ": " + symbol.getClass().getSimpleName();
                if (symbol instanceof SmiOidValue) {
                    SmiOidValue oidValue = (SmiOidValue) symbol;
                    msg += ": " + oidValue.getOidStr();
                }
                System.out.println(msg);
            }
        }
    }

    protected void checkOidTree(SmiMib mib) {
        //mib.getRootNode().dumpTree(System.out, "");

        int count = 0;
        for (SmiSymbol symbol : mib.getSymbols()) {
            if (symbol instanceof SmiOidValue) {
                SmiOidValue oidValue = (SmiOidValue) symbol;
                //oidValue.dumpAncestors(System.out);
                if (oidValue.getNode() != mib.getRootNode()) {
                    String msg = oidValue.getIdToken().toString();
                    assertNotNull(msg, oidValue.getNode().getParent());

                    SmiOidNode foundOidNode = oidValue.getNode().getParent().findChild(oidValue.getNode().getValue());
                    assertNotNull(msg, foundOidNode);
                    assertSame(msg, oidValue.getNode(), foundOidNode);
                    assertTrue(msg, oidValue.getNode().getParent().contains(oidValue.getNode()));
                }
//                SmiOidValue foundSymbol = findOidSymbol(mib.getRootNode(), symbol.getId());
//                assertNotNull(symbol.getId(), foundSymbol);
//                assertSame(symbol.getId(), symbol, foundSymbol);
                count++;
            }
        }

        //mib.getRootNode().dumpTree(System.out, "");
        int totalChildCount = mib.getRootNode().getTotalChildCount();
        //assertTrue(count + " < " +  totalChildCount, count < totalChildCount);
        //System.out.println("totalChildCount: " + totalChildCount);

        // I don't think you can draw any conclusions based on the count:
        // - due to anonymous oid node, you can have more oid nodes than oid symbols
        // - due to duplicated symbols you can have more symbols than nodes:
        //assertEquals(count + mib.getDummyOidNodesCount(), totalChildCount);
    }

    protected void checkObjectTypeAccessAll(SmiMib mib) {
        for (SmiObjectType objectType : mib.getObjectTypes()) {
            assertNotNull(objectType.getId(), objectType.getAccessAll());
        }
    }

    protected void add(Set<String> paths, String dir, String name) {
        paths.add(dir + "/" + name);
    }
}
