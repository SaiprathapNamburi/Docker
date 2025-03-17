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
            // Extract average times
            double playstoreSplash = getAverage(playstoreFile, 1); // SplashActivityNew
            double playstoreOnboarding = getAverage(playstoreFile, 2); // OnBoardingActivity
            double playstoreHome = getAverage(playstoreFile, 3); // HomeActivity

            double releaseSplash = getAverage(releaseFile, 1);
            double releaseOnboarding = getAverage(releaseFile, 2);
            double releaseHome = getAverage(releaseFile, 3);

            // Writing comparison results to CSV
            List<String> lines = Arrays.asList(
                "Type,Splash (ms),Onboarding (ms),Home (ms)",
                "Playstore," + playstoreSplash + "," + playstoreOnboarding + "," + playstoreHome,
                "Release," + releaseSplash + "," + releaseOnboarding + "," + releaseHome
            );
            Files.write(Paths.get(outputFile), lines);

            System.out.println("\n✅ Comparison file generated successfully!");
        } catch (IOException e) {
            System.err.println("❌ Error processing files: " + e.getMessage());
        }
    }

    public static double getAverage(String filePath, int columnIndex) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));

        if (lines.size() <= 1) {
            System.out.println("⚠ No valid data in " + filePath);
            return 0.0;
        }

        // Extract and filter valid values
        List<Double> values = lines.stream()
                .skip(1) // Skip header
                .map(line -> line.split("\\s*,\\s*")) // Split using commas
                .filter(parts -> parts.length > columnIndex && !parts[columnIndex].equalsIgnoreCase("N/A")) // Ignore "N/A"
                .map(parts -> {
                    try {
                        return Double.parseDouble(parts[columnIndex].trim()); // Convert to double
                    } catch (NumberFormatException e) {
                        System.out.println("⚠ Invalid number: " + Arrays.toString(parts));
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        System.out.println("📊 Extracted values for column " + columnIndex + " from " + filePath + ": " + values);

        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
