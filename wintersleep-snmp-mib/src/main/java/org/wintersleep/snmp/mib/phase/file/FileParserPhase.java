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
package org.wintersleep.snmp.mib.phase.file;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.wintersleep.snmp.mib.exception.SmiException;
import org.wintersleep.snmp.mib.phase.Phase;
import org.wintersleep.snmp.mib.phase.file.antlr.SMILexer;
import org.wintersleep.snmp.mib.phase.file.antlr.SMIParser;
import org.wintersleep.snmp.mib.smi.SmiMib;
import org.wintersleep.snmp.mib.smi.SmiModule;
import org.wintersleep.snmp.mib.smi.SmiVersion;
import org.wintersleep.snmp.util.location.Location;
import org.wintersleep.snmp.util.problem.DefaultProblemReporterFactory;
import org.wintersleep.snmp.util.problem.ProblemEventHandler;
import org.wintersleep.snmp.util.problem.ProblemReporterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Set;

// TODO allow any URL's

public class FileParserPhase implements Phase {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileParserPhase.class);

    private FileParserProblemReporter reporter;

    private List<URL> inputUrls;

    public FileParserPhase(FileParserProblemReporter reporter) {
        this.reporter = reporter;
    }

    public FileParserPhase(ProblemReporterFactory reporterFactory) {
        reporter = reporterFactory.create(FileParserProblemReporter.class);
    }

    public FileParserPhase(ProblemEventHandler eventHandler) {
        DefaultProblemReporterFactory reporterFactory = new DefaultProblemReporterFactory(eventHandler);
        reporter = reporterFactory.create(FileParserProblemReporter.class);
    }

    public FileParserProblemReporter getFileParserProblemReporter() {
        return reporter;
    }

    public List<URL> getInputUrls() {
        return inputUrls;
    }

    public void setInputUrls(List<URL> inputUrls) {
        this.inputUrls = inputUrls;
    }

    public SmiMib process(SmiMib mib) throws SmiException {
        for (URL url : getInputUrls()) {
            parse(mib, url, determineResourceLocation(url));
        }

        if (LOGGER.isDebugEnabled()) {
            logParseResults(mib);
        }

        return mib;
    }

    private String determineResourceLocation(URL url) {
        if ("file".equals(url.getProtocol())) {
            return "file://" + url.getPath();
        }
        return url.toString();
    }

    public void parse(SmiMib mib, URL url, String resourceLocation) {
        InputStream is = null;
        try {
            LOGGER.debug("Parsing :" + url);
            is = url.openStream();
            is = new BufferedInputStream(is);
            SMILexer lexer = new SMILexer(is);

            SMIParser parser = new SMIParser(lexer);
            parser.init(mib, resourceLocation);

            // TODO should define this in the grammar
            SmiModule module = parser.module_definition();
            while (module != null) {
                module = parser.module_definition();
            }
        } catch (TokenStreamException e) {
            LOGGER.debug(e.getMessage(), e);
            reporter.reportTokenStreamError(resourceLocation, e.getMessage());
        } catch (RecognitionException e) {
            LOGGER.debug(e.getMessage(), e);
            reporter.reportParseError(new Location(resourceLocation, e.getLine(), e.getColumn()), e.getMessage());
        } catch (IOException e) {
            LOGGER.debug(e.getMessage(), e);
            reporter.reportIoException(new Location(resourceLocation, 0, 0), e.getMessage());
        } finally {
            LOGGER.debug("Finished parsing :" + resourceLocation);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getMessage(), e);
                }
            }
        }
    }

    private void logParseResults(SmiMib mib) {
        Set<SmiModule> v1modules = mib.findModules(SmiVersion.V1);
        Set<SmiModule> v2modules = mib.findModules(SmiVersion.V2);
        LOGGER.debug("#SMIv1 modules=" + v1modules.size() + " #SMIv2 modules=" + v2modules.size());
        if (v1modules.size() > v2modules.size()) {
            LOGGER.debug("V2 modules:");
            logMibs(v2modules);
        } else if (v1modules.size() < v2modules.size()) {
            LOGGER.debug("V1 modules:");
            logMibs(v1modules);
        }
    }

    private void logMibs(Set<SmiModule> modules) {
        for (SmiModule module : modules) {
            LOGGER.debug(module + ": #V1 features=" + module.getV1Features() + " #V2 features=" + module.getV2Features());
        }
    }

}
