package com.github.lc.oss.commons.testing.web;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.github.lc.oss.commons.testing.AbstractTest;

public class DefaultIntegrationConfigTest extends AbstractTest {
    @Test
    public void test_codeCoverage() {
        DefaultIntegrationConfig config = new DefaultIntegrationConfig();

        Assertions.assertNotNull(config.clock());
        Assertions.assertNotNull(config.cookiePrefixParser());
        Assertions.assertNotNull(config.l10n());
        Assertions.assertNotNull(config.minifier(false));
        Assertions.assertNotNull(config.pathNormalizer());
        Assertions.assertNotNull(config.restService());
        Assertions.assertNotNull(config.testRestTemplateErrorHandler());
        Assertions.assertNotNull(config.themeService());
        Assertions.assertNotNull(config.userLocale());
        Assertions.assertNotNull(config.userTheme());
    }
}
