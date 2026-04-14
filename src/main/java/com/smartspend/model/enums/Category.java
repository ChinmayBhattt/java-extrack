package com.smartspend.model.enums;

/**
 * Expense categories used across the application.
 * Each category has a display name and an associated icon (for UI).
 */
public enum Category {
    FOOD("Food & Dining", "🍔"),
    TRANSPORT("Transport", "🚗"),
    HEALTH("Health & Medical", "💊"),
    ENTERTAINMENT("Entertainment", "🎬"),
    EDUCATION("Education", "📚"),
    UTILITIES("Utilities & Bills", "💡"),
    SHOPPING("Shopping", "🛍️"),
    HOUSING("Housing & Rent", "🏠"),
    TRAVEL("Travel", "✈️"),
    OTHER("Other", "📦");

    private final String displayName;
    private final String emoji;

    Category(String displayName, String emoji) {
        this.displayName = displayName;
        this.emoji = emoji;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmoji() {
        return emoji;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
