package uj.wmii.pwj.w7.insurance;

import java.io.*;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FloridaInsurance {
    static private List<String> header;
    static private List<InsuranceEntry> data;

    public static void main(String[] args) {
        readFile("FL_insurance.csv.zip");
        // create file with required data
        count("count.txt");
        tiv2012("tiv2012.txt");
        most_valuable("most_valuable.txt");
    }

    public static void most_valuable(String fileName){
        int countyIndex = header.indexOf("county");
        int tiv2011Index = header.indexOf("tiv_2011");
        int tiv2012Index = header.indexOf("tiv_2012");

         String result = data.stream().
                collect(Collectors.groupingBy(
                        entry -> entry.data().get(countyIndex),
                        Collectors.summingDouble( entry -> Double.parseDouble(entry.data().get(tiv2012Index)) -
                               Double.parseDouble(entry.data().get(tiv2011Index))
                        )
                )).
                 entrySet().stream().
                 sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).
                 limit(10).
                 collect(StringBuilder::new,
                        (sb, entry) -> sb.append(entry.getKey())
                            .append(",")
                            .append(String.format( "%.2f", entry.getValue()))
                            .append("\n"),
                        StringBuilder::append
                        ).
                 insert(0, "country,value\n").
                toString();


         createFile(fileName, result);
    }

    public static void tiv2012(String fileName){
        int columnIndex = header.indexOf("tiv_2012");
        double sum = data.stream().
                mapToDouble(entry -> Double.parseDouble(entry.data().get(columnIndex))).
                sum();
        createFile(fileName, String.valueOf(sum));
    }

    public static void count(String fileName){
        int columnIndex = header.indexOf("county");
        long numberOfCountries = data.stream().
                map(entry -> entry.data().get(columnIndex)).
                distinct().
                count();
        createFile(fileName, String.valueOf(numberOfCountries));
    }

    public static void createFile(String name, String text){
        try {
            if (new File(name).createNewFile()){
                FileWriter myWriter = new FileWriter(name);
                myWriter.write(text);
                myWriter.close();
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    public static void readFile(String fileName){
        try(ZipFile zipFile = new ZipFile(fileName)) {
            ZipEntry zipEntry = zipFile.getEntry("FL_insurance.csv");
            try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(zipFile.getInputStream(zipEntry)))) {

                header = List.of(bufferedReader.readLine().split(","));

                data = bufferedReader.lines().
                        map(line -> List.of(line.split(","))).
                        map(InsuranceEntry::new).
                        toList();
            }catch (IOException e){
                System.err.println("err reading from file");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

record InsuranceEntry(List<String> data){
}
