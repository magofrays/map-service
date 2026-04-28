package org.hack.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.data.arango")
public record ArangoProperties(
        String host,
        Integer port,
        String user,
        String password,
        String database
) {}
