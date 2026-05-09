package edu.connexion3a8.blogmoderation;

import edu.connexion3a8.blogmoderation.domain.ReportStatus;
import edu.connexion3a8.blogmoderation.service.ModerationStateMachine;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModerationStateMachineTest {
    @Test
    void validTransitions() {
        assertTrue(ModerationStateMachine.isValidTransition(ReportStatus.PENDING, ReportStatus.IN_REVIEW));
        assertTrue(ModerationStateMachine.isValidTransition(ReportStatus.IN_REVIEW, ReportStatus.APPROVED));
        assertTrue(ModerationStateMachine.isValidTransition(ReportStatus.IN_REVIEW, ReportStatus.HIDDEN));
    }

    @Test
    void invalidTransitions() {
        assertFalse(ModerationStateMachine.isValidTransition(ReportStatus.PENDING, ReportStatus.DELETED));
        assertFalse(ModerationStateMachine.isValidTransition(ReportStatus.APPROVED, ReportStatus.IN_REVIEW));
    }
}
