package com.rmacd.med.pitc.factories;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import com.rmacd.med.pitc.models.Surgery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Component
public class SurgeriesFactory {

    private static final Logger logger = LoggerFactory.getLogger(SurgeriesFactory.class);

    static CSVParser surgParser = new CSVParserBuilder().withSeparator('\t').withIgnoreQuotations(true).build();

    @Bean
    public Map<String, Surgery> getSurgeriesMap() {
        try (
                CSVReader surgReader = new CSVReaderBuilder(new FileReader("./data/scottish-gp-locations.tsv"))
                .withSkipLines(1).withCSVParser(surgParser).build();
        ) {
            List<String[]> surgeriesList = surgReader.readAll();
            logger.info("Loaded {} surgeries from file", surgeriesList.size());
            Map<String, Surgery> surgeries = new HashMap<>();
            for (String[] surgery : surgeriesList) {
                surgeries.put(surgery[0],
                        new Surgery.SurgeryBuilder(surgery[0])
                                .withPostCode(surgery[2])
                                .withLat(Float.parseFloat(surgery[6]))
                                .withLon(Float.parseFloat(surgery[7]))
                                .withListSize(Integer.parseInt(surgery[1]))
                                .build()
                );
            }
            return surgeries;
        } catch (IOException | CsvException e) {
            throw new RuntimeException(e);
        }
    }

}
