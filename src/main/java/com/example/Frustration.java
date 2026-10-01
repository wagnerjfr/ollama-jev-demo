package com.example;

// Frustration levels, from lowest to highest. The order matters!
public enum Frustration {
    CALM("Calm, just stating facts"),
    FRUSTRATED("Frustrated but civil"),
    VERY_ANGRY("Very angry, strong language");

    private final String description;

    Frustration(String description) {
        this.description = description;
    }

    String description() {
        return description;
    }

    // Turns the model's score (like 1.6) into the nearest level
    static Frustration fromScore(double score) {
        int index = (int) Math.round(score);
        index = Math.max(0, Math.min(index, values().length - 1));
        return values()[index];
    }
}
