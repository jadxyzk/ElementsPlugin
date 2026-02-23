package com.elements.utils;

public enum Element {
    FIRE,
    WATER,
    EARTH,
    AIR;

    public String getDisplayName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }

    public String getColor() {
        switch (this) {
            case FIRE: return "§c";
            case WATER: return "§b";
            case EARTH: return "§a";
            case AIR: return "§f";
            default: return "§7";
        }
    }

    public String getColoredName() {
        return getColor() + getDisplayName();
    }
}
