package com.gametout.gametout.enums;

public enum GameEngine {
    UNITY,
    UNREAL,
    GODOT,
    OTHER;

    public String toDisplayName() {
        return switch (this) {
            case UNITY -> "Unity";
            case UNREAL -> "Unreal";
            case GODOT -> "Godot";
            case OTHER -> "Other";
        };
    }
}
