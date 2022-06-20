import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import models.Surgery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

// sources:
// https://www.opendata.nhs.scot/dataset/prescriptions-in-the-community
// https://www.isdscotland.org/Health-Topics/Prescribing-and-Medicines/_docs/Open_Data_Glossary_of_Terms.pdf
// https://www.isdscotland.org/Health-Topics/General-Practice/Workforce-and-Practice-Populations/
//
public class Runner {

    private static final Logger logger = LoggerFactory.getLogger(Runner.class);

    // ugly single function that does everything
    public static void main(String[] args) {
        logger.info("Running merge ...");
        List<String[]> output = new ArrayList<>();
        output.add(new String[]{"gp_code", "date", "medication", "prescribed_num", "units_total", "list_sz", "lat", "lng", "per_cap_presc", "per_cap_unit", "geo"});

        CSVParser prescParser = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(',').build();
        CSVParser surgParser = new CSVParserBuilder().withSeparator('\t').withIgnoreQuotations(true).build();

        try (CSVReader surgReader = new CSVReaderBuilder(new FileReader("./data/scottish-gp-locations.tsv"))
                        .withSkipLines(1).withCSVParser(surgParser).build();
             CSVWriter writer = new CSVWriter(new FileWriter("./merged.tsv"), '\t', ICSVWriter.NO_QUOTE_CHARACTER, '\\', "\n");
                ) {

            List<String[]> surgeriesList = surgReader.readAll();
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

            File folder = new File("./scratch");
            List<File> fileList = Arrays.asList(Objects.requireNonNull(
                    folder.listFiles(file -> file.isFile() && file.getName().startsWith("pitc"))
            ));

            Long totalRecords = 0L;

            for (File file : fileList) {
                try (CSVReader prescReader = new CSVReaderBuilder(new FileReader(file))
                        .withSkipLines(1).withCSVParser(prescParser).build()) {
                    List<String[]> prescriptions = prescReader.readAll();

                    logger.info("{}: loaded {} prescriptions and {} surgeries", file.getName(), prescriptions.size(), surgeriesList.size());

                    for (String[] prescription : prescriptions) {
                        Surgery surgery = surgeries.get(prescription[1]);
                        if (null == surgery) continue;
                        String date = LocalDate.of(Integer.parseInt(prescription[8].substring(0, 4)), Integer.parseInt(prescription[8].substring(4, 6)), 1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                        double perCapPresc = (Integer.parseInt(prescription[5]) > 0) ? ((Integer.parseInt(prescription[5]) / (surgery.getListSize() * 1.0)) * 100.0) : 0;
                        double perCapUnit = (Math.round(Float.parseFloat(prescription[6])) > 0) ? ((Math.round(Float.parseFloat(prescription[6])) / (surgery.getListSize() * 1.0))) : 0d;
                        output.add(new String[]{
                                surgery.getCode(), date, prescription[3], prescription[5], String.valueOf(Math.round(Float.parseFloat(prescription[6]))), String.valueOf(surgery.getListSize()),
                                String.valueOf(surgery.getLat()), String.valueOf(surgery.getLon()),
                                String.format("%.5f", perCapPresc),
                                String.format("%.5f", perCapUnit),
                                String.format("%s,%s", surgery.getLat(), surgery.getLon())
                        });
                    }
                }
                writer.writeAll(output);
                totalRecords += output.size();
                output.clear();
            }
            logger.info("finished writing {} records", totalRecords);
        } catch (IOException | CsvException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
