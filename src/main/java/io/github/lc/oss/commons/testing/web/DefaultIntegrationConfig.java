package io.github.lc.oss.commons.testing.web;

import java.time.Clock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.annotation.RequestScope;

import io.github.lc.oss.commons.l10n.L10N;
import io.github.lc.oss.commons.l10n.UserLocale;
import io.github.lc.oss.commons.util.PathNormalizer;
import io.github.lc.oss.commons.web.controllers.UserTheme;
import io.github.lc.oss.commons.web.resources.Minifier;
import io.github.lc.oss.commons.web.resources.MinifierService;
import io.github.lc.oss.commons.web.services.ThemeService;
import io.github.lc.oss.commons.web.util.CookiePrefixParser;

public class DefaultIntegrationConfig {
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }

    @Bean
    public CookiePrefixParser cookiePrefixParser() {
        return new CookiePrefixParser();
    }

    @Bean
    public L10N l10n() {
        return new L10N();
    }

    @Bean
    public Minifier minifier(@Value("${application.services.minifier.enabled:true}") boolean enabled) {
        MinifierService service = new MinifierService();
        service.setEnabled(enabled);
        return service;
    }

    @Bean
    public PathNormalizer pathNormalizer() {
        return new PathNormalizer();
    }

    @Bean
    public RestService restService() {
        return new RestService();
    }

    @Bean
    public TestRestTemplateErrorHandler testRestTemplateErrorHandler() {
        return new TestRestTemplateErrorHandler();
    }

    @Bean
    public ThemeService themeService() {
        return new ThemeService();
    }

    @Bean
    @RequestScope
    public UserLocale userLocale() {
        return new UserLocale();
    }

    @Bean
    @RequestScope
    public UserTheme userTheme() {
        return new UserTheme();
    }
}
