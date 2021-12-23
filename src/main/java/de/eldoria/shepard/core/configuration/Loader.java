package de.eldoria.shepard.core.configuration;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Class to load the config from a specific path.
 */
@Slf4j
@UtilityClass
public final class Loader {
    /**
     * Load the config from config path.
     *
     * @return config object
     * @throws IOException If the file could not be found or serialized.
     */
    public static Config loadConfig() throws IOException {
        String home = new File(".").getAbsoluteFile().getParentFile().toString();

        Yaml yaml = new Yaml(new Constructor(Config.class));

        Path configFile = Paths.get(home, System.getProperty("shepard.config"));

        Config config = yaml.load(Files.newInputStream(configFile));
        log.info("Config loaded");
        return config;
    }
}
