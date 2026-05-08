package edu.connexion3a8.blogmoderation.service;

import edu.connexion3a8.blogmoderation.domain.ReportStatus;

import java.util.Set;

public final class ModerationStateMachine {
    private ModerationStateMachine() {}

    public static boolean isValidTransition(ReportStatus from, ReportStatus to) {
        if (from == to) return true;
        return switch (from) {
            case PENDING -> Set.of(ReportStatus.IN_REVIEW).contains(to);
            case IN_REVIEW -> Set.of(ReportStatus.APPROVED, ReportStatus.HIDDEN, ReportStatus.DELETED, ReportStatus.ESCALATED).contains(to);
            case APPROVED, HIDDEN, DELETED, ESCALATED -> false;
        };
    }
}
