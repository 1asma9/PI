package edu.connexion3a8.services;

// Classe pour stocker le score
public class BlogSentimentScore {
    private int positive;
    private int negative;
    private int neutral;
    private int total;
    private int globalScore; // 0-100

    // Getters et Setters
    public int getPositive() { return positive; }
    public void setPositive(int positive) { this.positive = positive; }

    public int getNegative() { return negative; }
    public void setNegative(int negative) { this.negative = negative; }

    public int getNeutral() { return neutral; }
    public void setNeutral(int neutral) { this.neutral = neutral; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getGlobalScore() { return globalScore; }
    public void setGlobalScore(int globalScore) { this.globalScore = globalScore; }

    public String getEmoji() {
        if (globalScore >= 70) return "😊"; // Très positif
        if (globalScore >= 40) return "😐"; // Neutre
        return "😞"; // Négatif
    }
}
