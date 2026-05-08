package edu.connexion3a8.blogmoderation.dto;

public class ModerationKpi {
    private final double avgResolutionHours;
    private final double approvalRate;
    private final long pendingCount;
    private final long escalatedCount;
    private final double aiPrecision;

    public ModerationKpi(double avgResolutionHours, double approvalRate, long pendingCount, long escalatedCount, double aiPrecision) {
        this.avgResolutionHours = avgResolutionHours;
        this.approvalRate = approvalRate;
        this.pendingCount = pendingCount;
        this.escalatedCount = escalatedCount;
        this.aiPrecision = aiPrecision;
    }

    public double getAvgResolutionHours() { return avgResolutionHours; }
    public double getApprovalRate() { return approvalRate; }
    public long getPendingCount() { return pendingCount; }
    public long getEscalatedCount() { return escalatedCount; }
    public double getAiPrecision() { return aiPrecision; }
}
