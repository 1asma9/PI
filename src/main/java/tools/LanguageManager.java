package tools;

import java.util.Locale;
import java.util.ResourceBundle;

public class LanguageManager {

    private static ResourceBundle bundle;
    private static Locale currentLocale = new Locale("en"); // Default English

    static {
        loadLanguage(currentLocale);
    }

    public static void loadLanguage(Locale locale) {
        currentLocale = locale;
        try {
            bundle = ResourceBundle.getBundle("languages.messages", locale);
        } catch (Exception e) {
            System.err.println("Could not load bundle for " + locale + ": " + e.getMessage());
        }
    }

    public static void setLanguageFrench() {
        loadLanguage(new Locale("fr"));
    }

    public static void setLanguageEnglish() {
        loadLanguage(new Locale("en"));
    }

    public static String get(String key) {
        if (bundle == null)
            return key;
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    public static Locale getCurrentLocale() {
        return currentLocale;
    }
}
