package com.pleasecode.solr.langdetect;

public class DetectedLanguage {

    private final String langCode;
    private final double certainty;

    public DetectedLanguage(String langCode, double certainty) {
        this.langCode = langCode;
        this.certainty = certainty;
    }

    public String getLangCode() {
        return langCode;
    }

    public double getCertainty() {
        return certainty;
    }
}
