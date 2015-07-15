package com.pleasecode.solr.setting;

import java.util.Set;

public class MultiLangIdentifierSetting {

    public final static String MULTI_LANG_ID = "multi-langid";
    public final static String FIELD_PARAM = MULTI_LANG_ID + ".fl";
    public final static String LANG_WHITELIST = MULTI_LANG_ID + ".whitelist";
    public final static String FALLBACK = MULTI_LANG_ID + ".fallback";
    public final static String THRESHOLD = MULTI_LANG_ID + ".threshold";
    public final static String HIDE_PREDEFINED_LANGS = MULTI_LANG_ID + ".hidePreLangs";

    public final static String DELIMITER_BETWEEN_FIELD_PARAM = ",";
    public final static double DEFAULT_LANG_THRESHOLD = 0.5;
    public final static boolean DEFAULT_HIDE_PREDEFINED_LANG = true;
    public final static boolean DEFAULT_ENABLED = true;

    private final Set<String> fieldParams;
    private final Set<String> whiteList;
    private final String fallback;
    private final double threshold;
    private final boolean hidePreLangs;
    private final boolean enabled;

    public MultiLangIdentifierSetting(Set<String> fieldParams, Set<String> whiteList, String fallback,
                                      double threshold, boolean hidePreLangs, boolean enabled) {
        this.fieldParams = fieldParams;
        this.whiteList = whiteList;
        this.fallback = fallback;
        this.threshold = threshold;
        this.hidePreLangs = hidePreLangs;
        this.enabled = enabled;
    }

    public Set<String> getFieldParams() {
        return fieldParams;
    }

    public Set<String> getWhiteList() {
        return whiteList;
    }

    public String getFallback() {
        return fallback;
    }

    public double getThreshold() {
        return threshold;
    }

    public boolean isHidePreLangs() {
        return hidePreLangs;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
