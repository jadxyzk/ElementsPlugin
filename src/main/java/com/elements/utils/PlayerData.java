package com.elements.utils;

import java.util.UUID;

public class PlayerData {
    private final UUID uuid;
    private Element element;
    private int level; // 1, 2, or 3
    private int kills;
    private int deaths; // deaths since last level (resets on derank)
    private Element lastElement; // to prevent same element assignment

    public PlayerData(UUID uuid, Element element) {
        this.uuid = uuid;
        this.element = element;
        this.level = 1;
        this.kills = 0;
        this.deaths = 0;
        this.lastElement = null;
    }

    public UUID getUuid() { return uuid; }

    public Element getElement() { return element; }
    public void setElement(Element element) {
        this.lastElement = this.element;
        this.element = element;
        this.level = 1;
        this.kills = 0;
        this.deaths = 0;
    }

    public Element getLastElement() { return lastElement; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getKills() { return kills; }
    public void setKills(int kills) { this.kills = kills; }
    public void addKill() { this.kills++; }

    public int getDeaths() { return deaths; }
    public void setDeaths(int deaths) { this.deaths = deaths; }
    public void addDeath() { this.deaths++; }
    public void resetDeaths() { this.deaths = 0; }

    public int getKillsRequired() {
        switch (level) {
            case 1: return 3;
            case 2: return 5;
            case 3: return 10; // already max, won't matter
            default: return 99;
        }
    }

    public boolean isMaxLevel() { return level >= 3; }
}
