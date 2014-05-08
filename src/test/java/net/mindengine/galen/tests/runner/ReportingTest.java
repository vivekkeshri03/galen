/*******************************************************************************
* Copyright 2014 Ivan Shubin http://mindengine.net
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
******************************************************************************/
package net.mindengine.galen.tests.runner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;
import net.mindengine.galen.components.report.ReportingListenerTestUtils;
import net.mindengine.galen.reports.ConsoleReportingListener;
import net.mindengine.galen.reports.GalenTestInfo;
import net.mindengine.galen.reports.HtmlReportBuilder;
import net.mindengine.galen.reports.LayoutReportNode;
import net.mindengine.galen.reports.TestReport;
import net.mindengine.galen.reports.model.LayoutReport;
import net.mindengine.galen.validation.LayoutReportListener;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import com.google.common.io.Files;

import freemarker.template.TemplateException;

public class ReportingTest {
    
    private static final String GALEN_LOG_LEVEL = "galen.log.level";

    @AfterMethod public void removeAllSystemProperties() {
        System.getProperties().remove(GALEN_LOG_LEVEL);
    }
    /*
    
    @Test public void shouldReport_inTestNgFormat_successfully() throws IOException {
        String reportPath = Files.createTempDir().getAbsolutePath() + "/testng-report/report.xml";
        
        String expectedDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "T00:00:00Z";
        
        TestngReportingListener listener = new TestngReportingListener(reportPath);
        ReportingListenerTestUtils.performSampleReporting("Home page on mobile", listener, listener);
        
        String expectedXml = IOUtils.toString(getClass().getResourceAsStream("/expected-reports/testng-report.xml"));
        
        listener.done();
        
        String realXml = FileUtils.readFileToString(new File(reportPath));
        
        Assert.assertEquals(expectedXml.replace("{expected-date}", expectedDate).replace("\\t    ", "\t"),
                realXml
                    .replaceAll("T([0-9]{2}:){2}[0-9]{2}Z", "T00:00:00Z")
                    .replaceAll("duration-ms=\"[0-9]+\"", "duration-ms=\"0\""));
    }*/
    
    @Test public void shouldReport_inHtmlFormat_successfully_andSplitFiles_perTest() throws IOException, TemplateException {
        String reportDirPath = Files.createTempDir().getAbsolutePath() + "/reports";
        
        List<GalenTestInfo> testInfos = new LinkedList<GalenTestInfo>();
        
        GalenTestInfo testInfo = new GalenTestInfo();
        testInfo.setName("Home page test");
        TestReport report = new TestReport();
        LayoutReport layoutReport = new LayoutReport();
        layoutReport.setScreenshotFullPath(File.createTempFile("screenshot", ".png").getAbsolutePath());
        ReportingListenerTestUtils.performSampleReporting("Home page test", null, new LayoutReportListener(layoutReport));
        
        report.addNode(new LayoutReportNode(layoutReport, "check layout"));
        testInfo.setReport(report);
        
        testInfos.add(testInfo);
        new HtmlReportBuilder().build(testInfos, reportDirPath);
        
        
        String expectedGeneralHtml = IOUtils.toString(getClass().getResourceAsStream("/expected-reports/report.html"));
        String realGeneralHtml = FileUtils.readFileToString(new File(reportDirPath + "/report.html"));
        Assert.assertEquals(expectedGeneralHtml, realGeneralHtml);
        
        String expectedSuite1Html = IOUtils.toString(getClass().getResourceAsStream("/expected-reports/test-1.html"));
        String realSuite1Html = FileUtils.readFileToString(new File(reportDirPath + "/report-1-home-page-test.html"));
        
        Assert.assertEquals(expectedSuite1Html, realSuite1Html);
        
        assertThat("Should place screenshot 1 in same folder", new File(reportDirPath + "/report-1-home-page-test-screenshot-1.png").exists(), is(true));
        
        assertThat("Should place css same folder", new File(reportDirPath + "/galen-report.css").exists(), is(true));
        assertThat("Should place js same folder", new File(reportDirPath + "/galen-report.js").exists(), is(true));
        assertThat("Should place jquery same folder", new File(reportDirPath + "/jquery-1.10.2.min.js").exists(), is(true));
    }

    @Test public void shouldReport_toConsole_successfully() throws IOException {
        performConsoleReporting_andCompare("/expected-reports/console.txt");
    }

    @Test public void shouldReport_toConsole_onlySuites_whenLogLevel_is_1() throws IOException {
        System.setProperty(GALEN_LOG_LEVEL, "1");
        performConsoleReporting_andCompare("/expected-reports/console-1.txt");
    }
    
    @Test public void shouldReport_toConsole_onlySuites_andPages_whenLogLevel_is_2() throws IOException {
        System.setProperty(GALEN_LOG_LEVEL, "2");
        performConsoleReporting_andCompare("/expected-reports/console-2.txt");
    }
    
    private void performConsoleReporting_andCompare(String expectedReport) throws IOException, UnsupportedEncodingException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        ConsoleReportingListener listener = new ConsoleReportingListener(ps, ps);
        ReportingListenerTestUtils.performSampleReporting("page1.test", listener, listener);
        
        listener.done();
        String expectedText = IOUtils.toString(getClass().getResourceAsStream(expectedReport)).replace("\\t    ", "\t");
        
        Assert.assertEquals(expectedText, baos.toString("UTF-8"));
    }
}
