import com.opencsv.*;
import com.opencsv.exceptions.CsvException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KibanaFormatter {

    private static final Logger logger = LoggerFactory.getLogger(KibanaFormatter.class);

    public static void main(String[] args) {

        List<String[]> output = new ArrayList<>();
        output.add(new String[]{"date", "month_num", "lat", "lon", "geo", "per_cap"});
        CSVParser kibanaDataP = new CSVParserBuilder().withIgnoreQuotations(false).build();
        try (CSVReader kibanaDataR = new CSVReaderBuilder(new FileReader("./scratch/psor02_grid_buckets.csv"))
                .withSkipLines(1).withCSVParser(kibanaDataP).build()) {

            List<String[]> lines = kibanaDataR.readAll();
            for (String[] line : lines) {
                logger.trace(line[0]);
                JSONObject latLon = (JSONObject) new JSONParser().parse(line[5]);
                String lon = String.format("%.2f", latLon.get("lon"));
                String lat = String.format("%.2f", latLon.get("lat"));
                output.add(new String[]{ line[3], getMonth(line[3]), lat, lon, String.format("%s,%s", lat, lon), line[4] });
            }
        } catch (IOException | CsvException | ParseException e) {
            e.printStackTrace();
        }

        try (CSVWriter writer = new CSVWriter(new FileWriter("./scratch/by_grid.tsv"), '\t', ICSVWriter.NO_QUOTE_CHARACTER, '\\', "\n")) {
            writer.writeAll(output);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        logger.info("finished writing {} records", output.size());
    }

    static String getMonth(String input) {
        return input.substring(5, 7);
    }

}
