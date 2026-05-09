package edu.connexion3a8.blogmoderation.domain;

import java.util.List;

public class ModerationAnalysis {
    private final int toxicityScore;
    private final boolean explicitThreat;
    private final List<String> categories;
    private final List<String> sensitiveWords;
    private final String language;
    private final double confidence;
    private final ModerationAction recommendation;
    private final String justification;

    public ModerationAnalysis(int toxicityScore, boolean explicitThreat, List<String> categories, List<String> sensitiveWords,
                              String language, double confidence, ModerationAction recommendation, String justification) {
        this.toxicityScore = toxicityScore;
        this.explicitThreat = explicitThreat;
        this.categories = categories;
        this.sensitiveWords = sensitiveWords;
        this.language = language;
        this.confidence = confidence;
        this.recommendation = recommendation;
        this.justification = justification;
    }

    public int getToxicityScore() { return toxicityScore; }
    public boolean isExplicitThreat() { return explicitThreat; }
    public List<String> getCategories() { return categories; }
    public List<String> getSensitiveWords() { return sensitiveWords; }
    public String getLanguage() { return language; }
    public double getConfidence() { return confidence; }
    public ModerationAction getRecommendation() { return recommendation; }
    public String getJustification() { return justification; }
}
