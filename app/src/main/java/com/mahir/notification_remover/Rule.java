package com.mahir.notification_remover;

public class Rule {
    public enum Type { APP, KEYWORD }

    public final Type type;
    public final String value;       // package name or keyword text
    public final String displayName; // human-readable: app name or keyword

    public Rule(Type type, String value, String displayName) {
        this.type = type;
        this.value = value;
        this.displayName = displayName;
    }
}
