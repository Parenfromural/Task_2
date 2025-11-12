package utils;

import java.io.InputStream;
import java.util.Properties;

public class ApiConfig {
    private static Properties props = new Properties();

    static {
        try (InputStream in = ApiConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Дорогой, у тебя конфиг не подгружается", e);
        }
    }

    public static String getBaseUri() {
        return props.getProperty("baseUri");
    }
}