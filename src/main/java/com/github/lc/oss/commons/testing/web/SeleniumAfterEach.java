package com.github.lc.oss.commons.testing.web;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class SeleniumAfterEach implements AfterEachCallback {
    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        AbstractSeleniumTest test = (AbstractSeleniumTest) context.getTestInstance().orElse(null);
        Throwable ex = context.getExecutionException().orElse(null);
        if (ex != null) {
            String name = test.getClass().getCanonicalName() + "-" + //
                    context.getTestMethod().get().getName() + "-" //
                    + Long.toString(System.currentTimeMillis());
            test.screenShot(name);
        }

        test.getDriver().quit();
    }
}
