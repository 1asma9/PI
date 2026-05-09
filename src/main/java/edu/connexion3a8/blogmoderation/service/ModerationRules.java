package edu.connexion3a8.blogmoderation.service;

import edu.connexion3a8.blogmoderation.domain.*;

public final class ModerationRules {
    public static final int MULTI_REPORT_THRESHOLD = 5;
    public static final double AUTO_DECISION_CONFIDENCE = 0.90;

    private ModerationRules() {}

    public static PriorityLevel resolvePriority(int toxicity, boolean explicitThreat, int reportCount) {
        if (toxicity >= 85 || explicitThreat) return PriorityLevel.CRITICAL;
        if ((toxicity >= 70 && toxicity <= 84) || reportCount >= MULTI_REPORT_THRESHOLD) return PriorityLevel.HIGH;
        if (toxicity >= 40) return PriorityLevel.MEDIUM;
        return PriorityLevel.LOW;
    }

    public static boolean requiresMandatoryReason(ModerationAction action) {
        return action == ModerationAction.HIDE || action == ModerationAction.DELETE || action == ModerationAction.ESCALATE;
    }

    public static boolean canExecute(UserRole role, ModerationAction action) {
        return switch (role) {
            case ADMIN -> true;
            case SENIOR_MODERATOR -> true;
            case MODERATOR -> action != ModerationAction.DELETE && action != ModerationAction.ESCALATE;
        };
    }
}
