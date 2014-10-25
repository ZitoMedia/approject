package com.zitomedia.repo.web.scripts.workflow;

/**
 * Created by drq on 9/3/14.
 */
public enum DirectionType {
    ASC("asc"),
    DES("des"),
    UNKNOWN("unknown");

    private String value;

    /**
     *
     * @param value
     */
    DirectionType(String value) {
        this.value = value;
    }

    /**
     *
     * @return
     */
    public String toString() {
        return this.value;
    }

    /**
     *
     * @param text
     * @return
     */
    public static DirectionType fromString(String text) {
        if (text != null) {
            for (DirectionType b : DirectionType.values()) {
                if (text.equalsIgnoreCase(b.value)) {
                    return b;
                }
            }
        }
        return UNKNOWN;
    }
}
