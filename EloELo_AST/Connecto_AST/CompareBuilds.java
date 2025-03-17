import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class CompareBuilds {
    public static void main(String[] args) {
        String playstoreFile = "data/Playstore_AppStartTime.csv";
        String releaseFile = "data/Release_AppStartTime.csv";
        String outputFile = "data/comparison.csv";

        try {
            double playstoreAvg = getAverage(playstoreFile);
            double releaseAvg = getAverage(releaseFile);

            // Writing to CSV
            List<String> lines = Arrays.asList(
                "Type,Average App Start Time (ms)",
                "Playstore," + playstoreAvg,
                "Release," + releaseAvg
            );
            Files.write(Paths.get(outputFile), lines);

            System.out.println("Comparison file generated successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getAverage(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<Double> values = lines.stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())  // Remove empty lines
                .map(Double::parseDouble)
                .collect(Collectors.toList());

        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
