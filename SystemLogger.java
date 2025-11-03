import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class SystemLogger {

    private static final Logger logger = Logger.getLogger(SystemLogger.class.getName());
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy h:mm:ss a");

    static {
        try {
            FileHandler fileHandler = new FileHandler("activity.log", true); // Append to file
            fileHandler.setFormatter(new CustomFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Disable console output
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logInfo(String message) {
        logger.log(Level.INFO, message);
    }

    public static void logWarning(String message) {
        logger.log(Level.WARNING, message);
    }

    public static void logError(String message) {
        logger.log(Level.SEVERE, message);
    }

    private static class CustomFormatter extends Formatter {
        @Override
        public String format(LogRecord record) {
            String timestamp = dateFormat.format(new Date(record.getMillis()));
            String level = record.getLevel().toString();
            String message = record.getMessage();
            String className = record.getSourceClassName();
            String methodName = record.getSourceMethodName();

            return String.format("%s %s %s\n%s: %s\n",
                    timestamp, className, methodName, level, message);
        }
    }
}
