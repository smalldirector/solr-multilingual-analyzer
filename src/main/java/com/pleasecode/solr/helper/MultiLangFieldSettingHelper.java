package com.pleasecode.solr.helper;

import com.google.common.collect.Maps;
import com.pleasecode.solr.setting.MultiLangFieldSetting;
import com.pleasecode.solr.setting.AnalyzerMode;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public final class MultiLangFieldSettingHelper {

    public static MultiLangFieldSetting valueOf(Map<String, String> args, AnalyzerMode analyzerMode) {
        String keyFromTextDelimiter = getKeyFromTextDelimiter(args);
        String multiKeyDelimiter = getMultiKeyDelimiter(args);
        String defaultFieldType = getDefaultFieldType(args);
        Map<String, String> fieldTypeMappings = getFieldTypeMappings(args);
        boolean ignoreMissingMappings = getIgnoreMissingMappings(args);
        boolean removeDuplicates = getRemoveDuplicates(args);
        return new MultiLangFieldSetting(analyzerMode, keyFromTextDelimiter, multiKeyDelimiter, defaultFieldType,
                fieldTypeMappings, ignoreMissingMappings, removeDuplicates);
    }

    public static void clearArgs(Map<String, String> args) {
        if (args.containsKey(MultiLangFieldSetting.KEY_FROM_TEXT_DELIMITER)) {
            args.remove(MultiLangFieldSetting.KEY_FROM_TEXT_DELIMITER);
        }
        if (args.containsKey(MultiLangFieldSetting.MULTI_KEY_DELIMITER)) {
            args.remove(MultiLangFieldSetting.MULTI_KEY_DELIMITER);
        }
        if (args.containsKey(MultiLangFieldSetting.DEFAULT_FIELD_TYPE)) {
            args.remove(MultiLangFieldSetting.DEFAULT_FIELD_TYPE);
        }
        if (args.containsKey(MultiLangFieldSetting.FIELD_TYPE_MAPPINGS)) {
            args.remove(MultiLangFieldSetting.FIELD_TYPE_MAPPINGS);
        }
        if (args.containsKey(MultiLangFieldSetting.IGNORE_INVALID_MAPPINGS)) {
            args.remove(MultiLangFieldSetting.IGNORE_INVALID_MAPPINGS);
        }
        if (args.containsKey(MultiLangFieldSetting.REMOVE_DUPLICATES)) {
            args.remove(MultiLangFieldSetting.REMOVE_DUPLICATES);
        }
    }

    private static String getKeyFromTextDelimiter(Map<String, String> args) {
        String keyFromTextDelimiter = MultiLangFieldSetting.DEFAULT_KEY_FROM_TEXT_DELIMITER;
        if (args.containsKey(MultiLangFieldSetting.KEY_FROM_TEXT_DELIMITER)) {
            keyFromTextDelimiter = args.get(MultiLangFieldSetting.KEY_FROM_TEXT_DELIMITER).trim();
        }
        return keyFromTextDelimiter;
    }

    private static String getMultiKeyDelimiter(Map<String, String> args) {
        String multiKeyDelimiter = MultiLangFieldSetting.DEFAULT_MULTI_KEY_DELIMITER;
        if (args.containsKey(MultiLangFieldSetting.MULTI_KEY_DELIMITER)) {
            multiKeyDelimiter = args.get(MultiLangFieldSetting.MULTI_KEY_DELIMITER).trim();
        }
        return multiKeyDelimiter;
    }

    private static String getDefaultFieldType(Map<String, String> args) {
        String defaultFieldType = MultiLangFieldSetting.DEFAULT_DEFAULT_FIELD_TYPE;
        if (args.containsKey(MultiLangFieldSetting.DEFAULT_FIELD_TYPE)) {
            defaultFieldType = args.get(MultiLangFieldSetting.DEFAULT_FIELD_TYPE).trim();
        }
        return defaultFieldType;
    }

    private static Map<String, String> getFieldTypeMappings(Map<String, String> args) {
        Map<String, String> fieldTypeMappings = Maps.newHashMap();
        if (args.containsKey(MultiLangFieldSetting.FIELD_TYPE_MAPPINGS)) {
            String mappingStr = args.get(MultiLangFieldSetting.FIELD_TYPE_MAPPINGS);
            if (StringUtils.isNotEmpty(mappingStr)) {
                String[] mappings = mappingStr.split(MultiLangFieldSetting.DELIMITER_BETWEEN_FIELD_TYPE_MAPPING);
                for (String mapping : mappings) {
                    String[] pairs = mapping.split(MultiLangFieldSetting.DELIMITER_BETWEEN_KEY_AND_FIELD_TYPE);
                    if (pairs.length == 2) {
                        fieldTypeMappings.put(pairs[0].trim(), pairs[1].trim());
                    }
                }
            }
        }
        return fieldTypeMappings;
    }

    private static boolean getIgnoreMissingMappings(Map<String, String> args) {
        boolean ignoreMissingMappings = MultiLangFieldSetting.DEFAULT_IGNORE_INVALID_MAPPINGS;
        if (args.containsKey(MultiLangFieldSetting.IGNORE_INVALID_MAPPINGS)) {
            ignoreMissingMappings = Boolean.valueOf(args.get(MultiLangFieldSetting.IGNORE_INVALID_MAPPINGS).trim());
        }
        return ignoreMissingMappings;
    }

    private static boolean getRemoveDuplicates(Map<String, String> args) {
        boolean removeDuplicates = MultiLangFieldSetting.DEFAULT_REMOVE_DUPLICATES;
        if (args.containsKey(MultiLangFieldSetting.REMOVE_DUPLICATES)) {
            removeDuplicates = Boolean.valueOf(args.get(MultiLangFieldSetting.REMOVE_DUPLICATES).trim());
        }
        return removeDuplicates;
    }
}
