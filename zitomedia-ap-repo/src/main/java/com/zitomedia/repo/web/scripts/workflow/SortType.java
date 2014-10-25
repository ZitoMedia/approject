package com.zitomedia.repo.web.scripts.workflow;

/**
 * Created by drq on 9/3/14.
 */
public enum SortType {
    DUE("due"),
    PRIORITY("priority"),
    NAME("name"),
    UNKNOWN("unknown");

    private String value;

    /**
     *
     * @param value
     */
    SortType(String value) {
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
    public static SortType fromString(String text) {
        if (text != null) {
            for (SortType b : SortType.values()) {
                if (text.equalsIgnoreCase(b.value)) {
                    return b;
                }
            }
        }
        return UNKNOWN;
    }
}
