package com.example.solveit_backend;

import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Properties properties = new Properties();

    // Bloco estático para carregar as propriedades UMA VEZ quando a classe é usada
    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("ERRO: Desculpe, não foi possível encontrar o arquivo config.properties");
            }
            properties.load(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDbUrl() {
        return properties.getProperty("db.url");
    }

    public static String getDbUsername() {
        return properties.getProperty("db.user");
    }

    public static String getDbPassword() {
        return properties.getProperty("db.password");
    }
}
