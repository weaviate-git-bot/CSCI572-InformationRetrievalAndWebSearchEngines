package org.ir.analytics.controller;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.ir.analytics.model.OutgoingUrl;
import org.ir.crawling.model.Indicator;
import org.ir.crawling.model.StatHeader;
import org.ir.crawling.model.UrlCrawlStat;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AnalyzeOutgoingUrls implements Analysis {

    private final static OutgoingUrl outgoingUrls = new OutgoingUrl();

    @Override
    public String analyze(Path filepath, Path outputPath) {
        try (Reader reader = Files.newBufferedReader(filepath);
        ) {
            CSVFormat csvFormat = CSVFormat.DEFAULT
                    .builder()
                    .setHeader(StatHeader.URL.value,
                            StatHeader.INDICATOR.value)
                    .setSkipHeaderRecord(true)
                    .build();

            List<CSVRecord> records = csvFormat.parse(reader).getRecords();

            // this is put together hastily and is not a good design but this is a forced assertion
            assert records.size() == AnalyzeFileSize.outgoingUrl.getTotalUrlsExtracted();
            outgoingUrls.setTotalUrlsExtracted(AnalyzeFileSize.outgoingUrl.getTotalUrlsExtracted());

            Set<UrlCrawlStat> uniqueUrls = getUniqueUrls(records);
            outgoingUrls.setUniqueUrlsExtractedCount(uniqueUrls.size());
            outgoingUrls.setUniqueUrlsWithinNewsSiteCount(getUniqueUrlsWithinNewsSite(uniqueUrls).size());
            outgoingUrls.setUniqueUrlsOutsideNewsSiteCount(getUniqueUrlsOutsideNewsSite(uniqueUrls).size());
            return outgoingUrls.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return String.format("%s did not return any output string", this.getClass().getName());
    }

    private static Set<UrlCrawlStat> getUniqueUrls(List<CSVRecord> records) {
        return records
                .stream()
                .map(record -> new UrlCrawlStat(record.get(StatHeader.URL.value), record.get(StatHeader.INDICATOR.value)))
                .collect(Collectors.toSet());
    }

    private static Set<UrlCrawlStat> getUniqueUrlsWithinNewsSite(Set<UrlCrawlStat> records) {
        return records
                .stream()
                .filter(record -> record.getSameDomainIndicator().equals(Indicator.OK.value))
                .collect(Collectors.toSet());
    }

    private static Set<UrlCrawlStat> getUniqueUrlsOutsideNewsSite(Set<UrlCrawlStat> records) {
        return records
                .stream()
                .filter(record -> record.getSameDomainIndicator().equals(Indicator.N_OK.value))
                .collect(Collectors.toSet());
    }

}
