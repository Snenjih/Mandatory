package de.snenjih.mandatory.cosmetics.api;

public enum CosmeticType {
    CAPE, HAT, WINGS, PARTICLES, ARMOR_SKIN;

    public String id() {
        return name().toLowerCase();
    }

    public static CosmeticType fromId(String id) {
        for (CosmeticType t : values()) {
            if (t.id().equals(id)) return t;
        }
        return null;
    }
}
