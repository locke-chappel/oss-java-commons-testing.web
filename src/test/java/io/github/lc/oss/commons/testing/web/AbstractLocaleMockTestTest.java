package io.github.lc.oss.commons.testing.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.opentest4j.AssertionFailedError;

import io.github.lc.oss.commons.l10n.L10N;
import io.github.lc.oss.commons.l10n.UserLocale;
import io.github.lc.oss.commons.serialization.Message;
import io.github.lc.oss.commons.testing.AbstractTest;

public class AbstractLocaleMockTestTest extends AbstractTest {
    private static class TestClass extends AbstractLocaleMockTest {
    }

    private static class TestMessage implements Message {
        @Override
        public Category getCategory() {
            return Message.Categories.Application;
        }

        @Override
        public Severity getSeverity() {
            return Message.Severities.Error;
        }

        @Override
        public int getNumber() {
            return -1009;
        }

        @Override
        public String getText() {
            return String.format("messages.%s.%s.%d-value", this.getCategory(), this.getSeverity(), this.getNumber());
        }
    }

    private L10N l10n;
    private UserLocale userLocale;

    private AbstractLocaleMockTest test;

    @BeforeEach
    private void setup() {
        this.test = new TestClass();

        this.l10n = Mockito.mock(L10N.class);
        this.userLocale = Mockito.mock(UserLocale.class);

        this.setField("l10n", this.l10n, this.test);
        this.setField("userLocale", this.userLocale, this.test);
    }

    @Test
    public void test_expectLocale() {
        this.test.expectLocale();
    }

    @Test
    public void test_expectFieldVar() {
        this.test.expectFieldVar("id");
    }

    @Test
    public void test_expectMessage() {
        Message m = new TestMessage();

        this.test.expectMessage(m);
    }

    @Test
    public void test_assertMessage_null() {
        try {
            this.test.assertMessage(new TestMessage(), (Collection<Message>) null);
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessage_null_v2() {
        try {
            this.test.assertMessage(null, Arrays.asList(new TestMessage()));
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessage_emptyCollection() {
        try {
            this.test.assertMessage(null, new ArrayList<>());
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <1> but was: <0>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessage() {
        this.test.assertMessage(new TestMessage(), Arrays.asList(new TestMessage()));
    }

    @Test
    public void test_assertMessage_mismatch() {
        try {
            this.test.assertMessage(new TestMessage() {
                @Override
                public int getNumber() {
                    return 9001;
                }
            }, Arrays.asList(new TestMessage()));
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <9001> but was: <-1009>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessages_nulls() {
        try {
            this.test.assertMessages(null, null);
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessages_nulls_v2() {
        try {
            this.test.assertMessages(new ArrayList<>(), null);
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessages_size_mismatch() {
        try {
            this.test.assertMessages(new ArrayList<>(), Arrays.asList(new TestMessage()));
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: <0> but was: <1>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessages_message_mismatch_category() {
        try {
            this.test.assertMessages(Arrays.asList(new TestMessage() {
                @Override
                public Category getCategory() {
                    return new Category() {
                        @Override
                        public String name() {
                            return "Other";
                        }
                    };
                }
            }), Arrays.asList(new TestMessage()));
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessages_message_mismatch_severity() {
        try {
            this.test.assertMessages(Arrays.asList(new TestMessage() {
                @Override
                public Severity getSeverity() {
                    return Message.Severities.Success;
                }
            }), Arrays.asList(new TestMessage()));
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessages_message_mismatch_number() {
        try {
            this.test.assertMessages(Arrays.asList(new TestMessage() {
                @Override
                public int getNumber() {
                    return 9001;
                }
            }), Arrays.asList(new TestMessage()));
            Assertions.fail("Expected Exception");
        } catch (AssertionFailedError ex) {
            Assertions.assertEquals("expected: not <null>", ex.getMessage());
        }
    }

    @Test
    public void test_assertMessages_matching() {
        this.test.assertMessages(Arrays.asList(new TestMessage()), Arrays.asList(new TestMessage()));
    }
}
