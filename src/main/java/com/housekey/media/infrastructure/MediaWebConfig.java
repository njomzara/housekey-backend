package com.housekey.media.infrastructure;

import java.nio.file.Path;
import java.time.Duration;

import com.housekey.media.application.MediaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
@EnableConfigurationProperties(MediaProperties.class)
public class MediaWebConfig implements WebMvcConfigurer {

    private final MediaProperties properties;

    public MediaWebConfig(MediaProperties properties) {
        this.properties = properties;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String pattern = properties.storage().publicUrlPrefix() + "/**";
        Path root = properties.storage().root().toAbsolutePath().normalize();

        registry.addResourceHandler(pattern)
                .addResourceLocations(root.toUri().toString())
                .setCachePeriod((int) Duration.ofDays(30).toSeconds())
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
}
