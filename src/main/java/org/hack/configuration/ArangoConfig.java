package org.hack.configuration;

import com.arangodb.ArangoDB;
import com.arangodb.springframework.annotation.EnableArangoRepositories;
import com.arangodb.springframework.config.ArangoConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(ArangoProperties.class)
@EnableArangoRepositories(basePackages = "org.hack")
public class ArangoConfig implements ArangoConfiguration {
    private final ArangoProperties properties;

    @Override
    public ArangoDB.Builder arango() {
        return new ArangoDB.Builder()
                .host(properties.host(), properties.port())
                .user(properties.user())
                .password(properties.password());
    }

    @Override
    public String database() {
        return properties.database();
    }
}
