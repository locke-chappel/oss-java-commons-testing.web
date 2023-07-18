package io.github.lc.oss.commons.testing.web;

import java.util.Collection;
import java.util.Locale;

import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;

import io.github.lc.oss.commons.l10n.L10N;
import io.github.lc.oss.commons.l10n.UserLocale;
import io.github.lc.oss.commons.l10n.Variable;
import io.github.lc.oss.commons.serialization.Message;
import io.github.lc.oss.commons.testing.AbstractMockTest;

public abstract class AbstractLocaleMockTest extends AbstractMockTest {
    @Mock
    private L10N l10n;
    @Mock
    private UserLocale userLocale;

    protected void expectLocale() {
        Mockito.when(this.getUserLocale().getLocale()).thenReturn(Locale.ENGLISH);
    }

    protected void expectFieldVar(String id) {
        Mockito.when(this.getL10n().getText(Locale.ENGLISH, id)).thenReturn(id + "-value");
    }

    protected void expectMessage(Message message) {
        String id = this.getMessageId(message);

        Mockito.when(this.getL10n().getText(//
                ArgumentMatchers.eq(Locale.ENGLISH), //
                ArgumentMatchers.eq(id), //
                ArgumentMatchers.any(Variable[].class))). //
                thenReturn(id + "-value");
    }

    protected void assertMessage(Message expected, Collection<Message> actual) {
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(1, actual.size());
        this.assertMessage(expected, actual.iterator().next());
    }

    protected void assertMessage(Message expected, Message actual) {
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.getCategory(), actual.getCategory());
        Assertions.assertEquals(expected.getSeverity(), actual.getSeverity());
        Assertions.assertEquals(expected.getNumber(), actual.getNumber());
        Assertions.assertEquals(this.getMessageId(expected) + "-value", actual.getText());
    }

    protected void assertMessages(Collection<Message> expected, Collection<Message> actual) {
        Assertions.assertNotNull(expected);
        Assertions.assertNotNull(actual);
        Assertions.assertEquals(expected.size(), actual.size());
        for (Message e : expected) {
            Message match = actual.stream(). //
                    filter(a -> a.getCategory() == e.getCategory()). //
                    filter(a -> a.getSeverity() == e.getSeverity()). //
                    filter(a -> a.getNumber() == e.getNumber()). //
                    findAny(). //
                    orElse(null);
            this.assertMessage(e, match);
        }
    }

    protected String getMessageId(Message message) {
        return String.format(String.format( //
                "messages.%s.%s.%d", //
                message.getCategory(), //
                message.getSeverity(), //
                message.getNumber()));
    }

    protected L10N getL10n() {
        return this.l10n;
    }

    protected UserLocale getUserLocale() {
        return this.userLocale;
    }
}
