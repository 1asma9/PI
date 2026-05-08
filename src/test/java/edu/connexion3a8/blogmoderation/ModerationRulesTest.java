package edu.connexion3a8.blogmoderation;

import edu.connexion3a8.blogmoderation.domain.PriorityLevel;
import edu.connexion3a8.blogmoderation.service.ModerationRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ModerationRulesTest {
    @Test
    void shouldResolveCriticalWhenToxicityHigh() {
        assertEquals(PriorityLevel.CRITICAL, ModerationRules.resolvePriority(90, false, 1));
    }

    @Test
    void shouldResolveCriticalWhenExplicitThreat() {
        assertEquals(PriorityLevel.CRITICAL, ModerationRules.resolvePriority(30, true, 1));
    }

    @Test
    void shouldResolveHighWhenMultiReports() {
        assertEquals(PriorityLevel.HIGH, ModerationRules.resolvePriority(20, false, 6));
    }

    @Test
    void shouldResolveMediumWhenRange40To69() {
        assertEquals(PriorityLevel.MEDIUM, ModerationRules.resolvePriority(55, false, 1));
    }
}
