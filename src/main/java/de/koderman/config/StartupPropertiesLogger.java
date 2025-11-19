package de.koderman.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;

@Slf4j
@Component
public class StartupPropertiesLogger {

    private final Environment env;

    @Value("${app.room.max-rooms:2500}")
    private int maxRooms;

    public StartupPropertiesLogger(Environment env) {
        this.env = env;
    }

    @PostConstruct
    public void logProperties() {
        // Log selected application properties at startup for transparency
        log.info("=== Application Configuration on Startup ===");
        log.info("app.room.max-rooms = {}", maxRooms);
        log.info("server.forward-headers-strategy = {}", env.getProperty("server.forward-headers-strategy"));
        log.info("server.tomcat.remote-ip-header = {}", env.getProperty("server.tomcat.remote-ip-header"));
        log.info("server.tomcat.protocol-header = {}", env.getProperty("server.tomcat.protocol-header"));
        log.info("server.tomcat.use-relative-redirects = {}", env.getProperty("server.tomcat.use-relative-redirects"));

        // Optionally list active profiles
        String[] profiles = env.getActiveProfiles();
        log.info("Active profiles: {}", profiles.length == 0 ? "(none)" : Arrays.toString(profiles));
        log.info("============================================");
    }
}