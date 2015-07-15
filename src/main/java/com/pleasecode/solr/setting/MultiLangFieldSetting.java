package com.pleasecode.solr.setting;

import java.util.Map;

public class MultiLangFieldSetting {

    public final static String FIELD_TYPE_MAPPINGS = "fieldTypeMappings";
    public final static String DEFAULT_FIELD_TYPE = "defaultFieldType";
    public final static String IGNORE_INVALID_MAPPINGS = "ignoreMissingMappings";
    public final static String KEY_FROM_TEXT_DELIMITER = "keyFromTextDelimiter";
    public final static String MULTI_KEY_DELIMITER = "multiKeyDelimiter";
    public final static String REMOVE_DUPLICATES = "removeDuplicates";

    public final static String DELIMITER_BETWEEN_FIELD_TYPE_MAPPING = ",";
    public final static String DELIMITER_BETWEEN_KEY_AND_FIELD_TYPE = ":";
    public final static String LEFT_DELIMITER_OF_HIDDEN_FLAG = "[";
    public final static String RIGHT_DELIMITER_OF_HIDDEN_FLAG = "]";
    public final static String DEFAULT_KEY_FROM_TEXT_DELIMITER = "|";
    public final static String DEFAULT_MULTI_KEY_DELIMITER = ",";
    public final static boolean DEFAULT_IGNORE_INVALID_MAPPINGS = false;
    public final static boolean DEFAULT_REMOVE_DUPLICATES = true;
    public final static String DEFAULT_DEFAULT_FIELD_TYPE = "";

    private final AnalyzerMode analyzerMode;
    private final String keyFromTextDelimiter;
    private final String multiKeyDelimiter;
    private final String defaultFieldType;
    private final Map<String, String> fieldTypeMappings;
    private final boolean ignoreMissingMappings;
    private final boolean removeDuplicates;

    public MultiLangFieldSetting(AnalyzerMode analyzerMode, String keyFromTextDelimiter, String multiKeyDelimiter,
                                 String defaultFieldType, Map<String, String> fieldTypeMappings, boolean ignoreMissingMappings,
                                 boolean removeDuplicates) {
        this.analyzerMode = analyzerMode;
        this.keyFromTextDelimiter = keyFromTextDelimiter;
        this.multiKeyDelimiter = multiKeyDelimiter;
        this.defaultFieldType = defaultFieldType;
        this.fieldTypeMappings = fieldTypeMappings;
        this.ignoreMissingMappings = ignoreMissingMappings;
        this.removeDuplicates = removeDuplicates;
    }

    public AnalyzerMode getAnalyzerMode() {
        return analyzerMode;
    }

    public String getKeyFromTextDelimiter() {
        return keyFromTextDelimiter;
    }

    public String getMultiKeyDelimiter() {
        return multiKeyDelimiter;
    }

    public String getDefaultFieldType() {
        return defaultFieldType;
    }

    public Map<String, String> getFieldTypeMappings() {
        return fieldTypeMappings;
    }

    public boolean isIgnoreMissingMappings() {
        return ignoreMissingMappings;
    }

    public boolean isRemoveDuplicates() {
        return removeDuplicates;
    }
}
