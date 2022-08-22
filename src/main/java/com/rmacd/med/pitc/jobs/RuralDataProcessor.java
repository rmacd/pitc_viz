package com.rmacd.med.pitc.jobs;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.rmacd.med.pitc.models.Surgery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RuralDataProcessor implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(RuralDataProcessor.class);

    Pattern datePattern = Pattern.compile("urban_rural_([0-9]{4}]).+");

    final Map<String, Surgery> surgeries;

    public RuralDataProcessor(Map<String, Surgery> surgeries) {
        this.surgeries = surgeries;
    }

    @PostConstruct
    public void postConstruct() {
        this.run();
    }

    @Override
    public void run() {

        // requires files from https://publichealthscotland.scot/publications/general-practice-gp-workforce-and-practice-list-sizes/general-practice-gp-workforce-and-practice-list-sizes-2011-2021/
        // to be saved as a CSV in format of urban_rural_year.csv eg urban_rural_2019.csv

        CSVParser urbanRuralParser = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator('\t').build();
        File folder = new File("./scratch");
        List<File> fileList = Arrays.asList(Objects.requireNonNull(
                folder.listFiles(file -> file.isFile() && file.getName().startsWith("urban_rural"))
        ));
        for (File file : fileList) {
            Matcher m = datePattern.matcher(file.getName());
            String year;
            if (m.matches()) {
                year = m.group(1);
                logger.info("Loading {} data from {}", year, file.getName());
            }
        }

    }
}
