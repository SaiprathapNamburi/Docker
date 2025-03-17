import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class AppStartTimeExtractor {

    public static void grantNotificationPermission(String packageName) throws IOException, InterruptedException {
        System.out.println("🔔 Granting Notification Permission...");
        new ProcessBuilder("adb", "shell", "pm", "grant", packageName, "android.permission.POST_NOTIFICATIONS").start().waitFor();
        System.out.println("✅ Notification Permission Granted!");
    }

    public static void uninstallApp(String packageName) throws IOException, InterruptedException {
        System.out.println("🗑️ Uninstalling App...");
        new ProcessBuilder("adb", "uninstall", packageName).start().waitFor();
        System.out.println("✅ App Uninstalled Successfully!");
    }

    public static void installApp(String apkPath) throws IOException, InterruptedException {
        System.out.println("📥 Installing App from: " + apkPath);
        Process process = new ProcessBuilder("adb", "install", apkPath).start();
        process.waitFor();
        System.out.println("✅ App Installed Successfully!");
    }

    public static Map<String, String> getAppStartTimes() throws IOException {
        List<String> command = Arrays.asList("adb", "logcat", "-d");
        Process process = new ProcessBuilder(command).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String splashRegex = "Displayed com\\.eloelo/\\.splash\\.view\\.SplashActivityNew .*?: \\+(?:(\\d+)s)?(\\d+)ms";
        String onboardingRegex = "Displayed com\\.eloelo/\\.splash\\.view\\.OnBoardingActivity .*?: \\+(?:(\\d+)s)?(\\d+)ms";
        String homeRegex = "Displayed com\\.eloelo/\\.HomeActivity .*?: \\+(?:(\\d+)s)?(\\d+)ms";

        Pattern splashPattern = Pattern.compile(splashRegex);
        Pattern onboardingPattern = Pattern.compile(onboardingRegex);
        Pattern homePattern = Pattern.compile(homeRegex);

        String line;
        Map<String, String> activityTimes = new HashMap<>();
        activityTimes.put("SplashActivityNew", "N/A");
        activityTimes.put("OnBoardingActivity", "N/A");
        activityTimes.put("HomeActivity", "N/A");

        while ((line = reader.readLine()) != null) {
            Matcher splashMatcher = splashPattern.matcher(line);
            Matcher onboardingMatcher = onboardingPattern.matcher(line);
            Matcher homeMatcher = homePattern.matcher(line);

            if (splashMatcher.find()) {
                int seconds = splashMatcher.group(1) != null ? Integer.parseInt(splashMatcher.group(1)) : 0;
                int milliseconds = Integer.parseInt(splashMatcher.group(2));
                activityTimes.put("SplashActivityNew", String.valueOf(seconds * 1000 + milliseconds));
            }
            if (onboardingMatcher.find()) {
                int seconds = onboardingMatcher.group(1) != null ? Integer.parseInt(onboardingMatcher.group(1)) : 0;
                int milliseconds = Integer.parseInt(onboardingMatcher.group(2));
                activityTimes.put("OnBoardingActivity", String.valueOf(seconds * 1000 + milliseconds));
            }
            if (homeMatcher.find()) {
                int seconds = homeMatcher.group(1) != null ? Integer.parseInt(homeMatcher.group(1)) : 0;
                int milliseconds = Integer.parseInt(homeMatcher.group(2));
                activityTimes.put("HomeActivity", String.valueOf(seconds * 1000 + milliseconds));
            }
        }

        return activityTimes;
    }

    public static void writeToCSV(String filePath, Map<String, String> times, int iteration, boolean isFirstRun) {
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());

            if (isFirstRun) {
                Files.write(path, "".getBytes(), StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
            }

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
                if (isFirstRun) {
                    writer.write("Iteration,SplashActivityNew,OnBoardingActivity,HomeActivity\n");
                }
                writer.write(iteration + "," + times.get("SplashActivityNew") + "," + times.get("OnBoardingActivity") + "," + times.get("HomeActivity") + "\n");
                System.out.println("✅ Data written for iteration " + iteration);
            }
        } catch (IOException e) {
            System.err.println("❌ Error writing to CSV: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void closeAppCompletely(String packageName) throws IOException, InterruptedException {
        System.out.println("❌ Closing app completely...");
        new ProcessBuilder("adb", "shell", "am", "force-stop", packageName).start();
        Thread.sleep(1000);

        System.out.println("❌ Killing background processes...");
        new ProcessBuilder("adb", "shell", "pkill", "-f", packageName).start();
        Thread.sleep(1000);

        System.out.println("🧹 Clearing ADB logs...");
        new ProcessBuilder("adb", "logcat", "-c").start().waitFor();
        Thread.sleep(1000);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        String packageName = "com.eloelo";
        String activityName = ".splash.view.SplashActivityNew";
        String buildType = "Playstore";
        String filePath = "data/" + buildType + "_AppStartTime.csv";
        String apkPath = "\"C:\\Users\\User\\Downloads\\eloelo_200024813_Mar-12-2025-release.apk\""; // Update with actual APK path

        uninstallApp(packageName);
        installApp(apkPath);
        grantNotificationPermission(packageName);

        boolean isFirstRun = true;

        for (int i = 1; i <= 5; i++) {
            System.out.println("🚀 Iteration " + i + " - Launching app...");
            closeAppCompletely(packageName);
            
            new ProcessBuilder("adb", "shell", "am", "start", "-n", packageName + "/" + activityName).start();
            Thread.sleep(5000);
            
            Map<String, String> times = getAppStartTimes();
            System.out.println("🕒 Extracted Times (Run " + i + "): " + times);
            
            writeToCSV(filePath, times, i, isFirstRun);
            isFirstRun = false;
            
            closeAppCompletely(packageName);
        }
        
        System.out.println("✅ All readings captured successfully!");
    }
}
