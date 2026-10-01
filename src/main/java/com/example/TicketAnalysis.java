package com.example;

public record TicketAnalysis(
        Department department,
        double departmentConfidence,
        Frustration frustration,
        double urgency) {

    private static final double MIN_CONFIDENCE = 0.6;
    private static final double URGENT_ABOVE = 0.8;

    Handling handling() {
        if (departmentConfidence < MIN_CONFIDENCE) {
            return Handling.HUMAN_REVIEW;   // the model isn't sure, so ask a person
        }
        if (urgency > URGENT_ABOVE) {
            return Handling.PRIORITY;
        }
        return Handling.NORMAL;
    }
}
