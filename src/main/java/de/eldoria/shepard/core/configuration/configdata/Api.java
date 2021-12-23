package de.eldoria.shepard.core.configuration.configdata;

import lombok.Data;

@Data
public class Api {
    /**
     * Port of the api.
     */
    private Integer port;
    /**
     * Authorization for api access.
     */
    private String authorization;
}
