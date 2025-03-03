package org.ir.analytics.controller;

import org.ir.cli.Props;
import org.ir.crawling.controller.CrawlerController;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class CollateAnalysis {
    static final Props props = Props.getInstance();
    private static final Path REPORT = Path.of(props.getOUTPUT_DIR(), "CrawlReport_latimes.txt");
    private static final Path FETCH_CSV = Path.of(props.getOUTPUT_DIR(), props.getOUTPUT_FETCH());
    private static final Path URLS_CSV = Path.of(props.getOUTPUT_DIR(), props.getOUTPUT_URL());
    private static final Path VISIT_CSV = Path.of(props.getOUTPUT_DIR(), props.getOUTPUT_VISIT());
    public static void main(String[] args) {

        performInitialCleanUpAndCreateNewReportFile();

        AnalyzeStatic analyzeStatic = new AnalyzeStatic();
        AnalyzeFetch analyzeFetch = new AnalyzeFetch();
        AnalyzeOutgoingUrls analyzeOutgoingUrls = new AnalyzeOutgoingUrls();
        AnalyzeStatusCode analyzeStatusCode = new AnalyzeStatusCode();
        AnalyzeFileSize analyzeFileSize = new AnalyzeFileSize();

        final String staticData = analyzeStatic.analyze(null, REPORT);
        final String fetchData = analyzeFetch.analyze(FETCH_CSV, REPORT);
        final String fileSizeData = analyzeFileSize.analyze(VISIT_CSV, REPORT);
        final String outgoingUrlData = analyzeOutgoingUrls.analyze(URLS_CSV, REPORT); // TODO: confirm that #total urls extracted is the sum of #outgoing links; #total urls extracted is the sum of all values in column 3 of visit.csv
        final String statusCodeData = analyzeStatusCode.analyze(FETCH_CSV, REPORT);

        StringBuilder sb = new StringBuilder();
        sb.append(staticData);
        sb.append(fetchData);
        sb.append(outgoingUrlData);
        sb.append(statusCodeData);
        sb.append(fileSizeData);
        CollateAnalysis.outputAnalysisToFile(REPORT, sb.toString());
    }

    private static void performInitialCleanUpAndCreateNewReportFile() {
        try {
            Files.deleteIfExists(REPORT);
            Files.createDirectories(REPORT.getParent());
            Files.createFile(REPORT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void outputAnalysisToFile(Path filepath, String data) {
        try {
            Files.writeString(filepath, data, StandardOpenOption.APPEND);
            System.out.println(String.format("Completed analysis. Writing results to output file [%s]", filepath.getFileName()));
        } catch (Exception e) {
            e.printStackTrace();;
        }
    }
}
