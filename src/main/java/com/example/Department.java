package com.example;

public enum Department {
    BILLING("Payment or subscription issues"),
    TECHNICAL("Bugs or integration problems"),
    SALES("Pricing or account questions");

    private final String description;

    Department(String description) {
        this.description = description;
    }

    String description() {
        return description;
    }

    // The name we send to the API, e.g. "billing"
    String apiName() {
        return name().toLowerCase();
    }

    static Department fromApiName(String apiName) {
        return valueOf(apiName.toUpperCase());
    }
}
