package uk.gov.ons.validation.util;

import java.util.Map;
import java.util.ResourceBundle;

public final class PropertiesUtil {


    public static final String WRANGLER_NAME = "WRANGLER_NAME";

    private static final Map<String, String> environmentVars = System.getenv();
    private static final ResourceBundle bundle = ResourceBundle.getBundle("application");

    private PropertiesUtil(){}

    public static String getProperty(String key) {
        if (environmentVars.containsKey(key)) {
            return environmentVars.get(key);
        } else {
            return bundle.getString(key);
        }
    }

}
