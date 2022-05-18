import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
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
        output.add(new String[]{"gp_code", "date", "medication", "prescribed_num", "practice", "list_sz", "lat", "lng", "per_cap", "geo"});

        CSVParser prescParser = new CSVParserBuilder().withIgnoreQuotations(true).withSeparator(',').build();
        CSVParser surgParser = new CSVParserBuilder().withSeparator('\t').withIgnoreQuotations(true).build();

        try (CSVReader surgReader = new CSVReaderBuilder(new FileReader("./scottish-gp-locations.tsv"))
                .withSkipLines(1).withCSVParser(surgParser).build()) {

            List<String[]> surgeriesList = surgReader.readAll();
            Map<String, String[]> surgeries = new HashMap<>();
            for (String[] strings : surgeriesList) {
                surgeries.put(strings[2], new String[]{strings[0], strings[1], strings[3], strings[9], strings[10]});
            }

            File folder = new File(".");
            List<File> fileList = Arrays.asList(Objects.requireNonNull(
                    folder.listFiles(file -> file.isFile() && file.getName().startsWith("pitc"))
            ));

            for (File file : fileList) {
                try (CSVReader prescReader = new CSVReaderBuilder(new FileReader(file))
                        .withSkipLines(1).withCSVParser(prescParser).build()) {
                    List<String[]> prescriptions = prescReader.readAll();

                    logger.info("{}: loaded {} prescriptions and {} surgeries", file.getName(), prescriptions.size(), surgeriesList.size());

                    for (String[] prescription : prescriptions) {
                        String[] surgery = surgeries.get(prescription[1]);
                        if (null == surgery) continue;
                        String date = LocalDate.of(Integer.parseInt(prescription[8].substring(0, 4)), Integer.parseInt(prescription[8].substring(4, 6)), 1).format(DateTimeFormatter.ISO_LOCAL_DATE);
                        double percent = (Integer.parseInt(prescription[5]) > 0) ? ((Integer.parseInt(prescription[5]) / (Integer.parseInt(surgery[1]) * 1.0)) * 100.0) : 0;
                        output.add(new String[]{
                                prescription[1], date, prescription[3], prescription[5], surgery[0],
                                surgery[1], surgery[3], surgery[4], String.format("%.5f", percent),
                                String.format("%s,%s", surgery[3], surgery[4])
                        });
                    }
                }
            }
        } catch (IOException | CsvException e) {
            logger.error(e.getMessage(), e);
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter("./merged.tsv"), '\t', ICSVWriter.NO_QUOTE_CHARACTER, '\\', "\n")) {
            writer.writeAll(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("finished writing {} records", output.size());

    }

}
