package com.danielcsant.gitlab.repository;

import com.danielcsant.gitlab.App;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class MysqlConnection {

    private final static Logger LOGGER = Logger.getLogger("com.danielcsant.gitlab.repository.MysqlConnection");

    public static Connection connect() {

        Properties prop = new Properties();

        InputStream is = MysqlConnection.class.getClassLoader().getResourceAsStream("app.properties");
        try {
            prop.load(is);
        } catch (IOException e) {
            LOGGER.severe(e.getMessage());
            return null;
        }

        String hostUrl = prop.getProperty("mysqlHost");
        String user = prop.getProperty("mysqlUser");
        String pass = prop.getProperty("mysqlPass");
        String database = prop.getProperty("mysqlDatabase");

        Connection con = null;

        String url = "jdbc:mysql://" + hostUrl + ":3306/" + database + "?user=" + user
                + "&password=" + pass;
        try {
            con = DriverManager.getConnection(url);
            if (con != null) {
                System.out.println("Connected");
            }
        } catch (SQLException e) {
            System.out.println("Can't connect to the database");
            e.printStackTrace();
        }
        return con;
    }

}
