import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class AppStartTimeExtractor {

    // Function to get app start time from logs
    public static String getAppStartTime() throws IOException {
        List<String> command = Arrays.asList("adb", "logcat", "-d");
        ProcessBuilder builder = new ProcessBuilder(command);
        Process process = builder.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        // Regex patterns for different log formats
        String standardRegex = "Displayed .*: \\\\+(?:(\\\\d+)s)?(\\\\d+)ms"; 
        String motoRegex = "MotoDisplayed .*?,(\\\\d+)";
        

        Pattern standardPattern = Pattern.compile(standardRegex);
        Pattern motoPattern = Pattern.compile(motoRegex);

        String line;
        String displayedTime = null;
        String motoDisplayedTime = null;

        while ((line = reader.readLine()) != null) {
            Matcher standardMatcher = standardPattern.matcher(line);
            Matcher motoMatcher = motoPattern.matcher(line);

            if (standardMatcher.find()) {
                int seconds = standardMatcher.group(1) != null ? Integer.parseInt(standardMatcher.group(1)) : 0;
                int milliseconds = Integer.parseInt(standardMatcher.group(2));
                displayedTime = String.valueOf(seconds * 1000 + milliseconds);  // Convert to ms
            } else if (motoMatcher.find()) {
                motoDisplayedTime = motoMatcher.group(1);  // Capture Moto time but don't prioritize it
            }
        }

        // Prefer "Displayed" over "MotoDisplayed" if both exist
        if (displayedTime != null) {
            return displayedTime;
        } else if (motoDisplayedTime != null) {
            return motoDisplayedTime;
        }
        return "N/A";  // If no match found
    }

    // Function to write the readings to a CSV file
    public static void writeToCSV(String filePath, String startTime) {
        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(filePath), StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(startTime + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String packageName = "com.connecto";  // Package name of Connecto app
        String activityName = ".MainActivity"; // Launch activity
        String buildType = "Release";  // Change to "Release" for release build
        String filePath = "data/" + buildType + "_AppStartTime.csv";

        for (int i = 1; i <= 5; i++) {
            System.out.println("Iteration " + i + " - Launching app...");

            // Clear logs before launching
            new ProcessBuilder("adb", "logcat", "-c").start().waitFor();

            // Launch app
            new ProcessBuilder("adb", "shell", "am", "start", "-n", packageName + "/" + activityName).start();
            Thread.sleep(5000); // Wait for the app to load

            // Get start time
            String startTime = getAppStartTime();
            System.out.println("App Start Time (Run " + i + "): " + startTime + "ms");

            // Store in CSV
            writeToCSV(filePath, startTime);

            // Close app completely
            System.out.println("Closing app...");
            new ProcessBuilder("adb", "shell", "am", "kill", packageName).start(); // Kill foreground app
            Thread.sleep(1000); // Short wait
            new ProcessBuilder("adb", "shell", "am", "force-stop", packageName).start(); // Ensure background processes are stopped
            Thread.sleep(2000); // Wait before next iteration
        }

        System.out.println("✅ All readings captured successfully!");
    }
}
