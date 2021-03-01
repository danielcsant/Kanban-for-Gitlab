package com.danielcsant.gitlab;

import com.danielcsant.gitlab.service.InitializeExpeditesMetricsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

public class InitializeExpedites {

    private final static Logger LOGGER = LoggerFactory.getLogger("com.danielcsant.gitlab.InitializeExpedites");

    public static void main(String[] args) throws Exception {
        LOGGER.info("Loading configuration");
        Properties prop = new Properties();
        try (InputStream input = App.class.getClassLoader().getResourceAsStream("app.properties")) {
            // load a properties file
            prop.load(input);
        } catch (IOException ex) {
            throw new Exception("Configure app.properties file");
        }

        LOGGER.info("Initializing services");
        String personalAccessToken = prop.getProperty("personalAccessToken");
        String hostUrl = prop.getProperty("hostUrl");
        String [] teams = prop.getProperty("teams").split(",");

        InitializeExpeditesMetricsService initializeExpeditesMetricsService
                = new InitializeExpeditesMetricsService(hostUrl, personalAccessToken);

        initializeExpeditesMetricsService.init(teams);
    }

}
