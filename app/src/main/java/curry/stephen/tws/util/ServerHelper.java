package curry.stephen.tws.util;

/**
 * Server-related helper class.<br/>
 * Created by lingchong on 16/5/23.
 */
public class ServerHelper {

    private static String defaultServerURL = "http://localhost:8080/";

    private static String currentServerURL = "http://localhost:8080/";

    private static final String LOGIN = "servlet/mLogin";

    private static final String TRANSMITTER_DYNAMIC_INFORMATION = "servlet/transmitterDynamicInformation";

    private static final String TRANSMITTER_TOTAL_INFORMATION = "servlet/transmitterTotalInformation";

    public static String getDefaultServerURL() {
        return defaultServerURL;
    }

    public static void setDefaultServerURL(String defaultServerURL) {
        ServerHelper.defaultServerURL = defaultServerURL;
    }

    public static String getCurrentServerURL() {
        return currentServerURL;
    }

    public static void setCurrentServerURL(String currentServerURL) {
        ServerHelper.currentServerURL = currentServerURL;
    }

    public static String getLoginURI() {
        return currentServerURL + LOGIN;
    }

    public static String getTransmitterDynamicInformationURI() {
        return currentServerURL + TRANSMITTER_DYNAMIC_INFORMATION;
    }

    public static String getTransmitterTotalInformationURI() {
        return currentServerURL + TRANSMITTER_TOTAL_INFORMATION;
    }
}
