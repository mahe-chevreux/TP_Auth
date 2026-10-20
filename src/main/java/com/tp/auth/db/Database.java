package com.tp.auth.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Database {

    public static Connection getConnection() throws Exception {
        Properties props = new Properties();

        try (InputStream in = Database.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (in == null) {
                throw new IllegalStateException("config.properties introuvable");
            }

            props.load(in);
        }

        return DriverManager.getConnection(
                props.getProperty("db.url"),
                props.getProperty("db.user"),
                props.getProperty("db.password"));
    }

    // Test rapide de la connexion
    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("Connexion réussie !");
        } catch (Exception e) {
            System.out.println("Erreur : " + e.getMessage());
        }
    }
}