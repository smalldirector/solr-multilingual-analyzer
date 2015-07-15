package com.pleasecode.solr.helper;

import com.google.common.collect.Sets;
import com.pleasecode.solr.setting.MultiLangIdentifierSetting;
import org.apache.solr.common.params.SolrParams;

import java.util.Set;

public final class MultiLangIdentifierSettingHelper {

    public static MultiLangIdentifierSetting valueOf(SolrParams params) {
        if (null == params) {
            return null;
        }
        Set<String> fieldParams = getFieldParams(params);
        Set<String> whitelist = getWhitelist(params);
        String fallback = getFallback(params);
        double threshold = getThreshold(params);
        boolean hidePreLangs = isHidePreLangs(params);
        boolean enabled = isEnabled(params);
        return new MultiLangIdentifierSetting(fieldParams, whitelist, fallback, threshold, hidePreLangs, enabled);
    }

    private static Set<String> getFieldParams(SolrParams params) {
        Set<String> fieldParams = Sets.newHashSet();
        String[] predefinedFields = params.get(MultiLangIdentifierSetting.FIELD_PARAM, "").split(
                MultiLangIdentifierSetting.DELIMITER_BETWEEN_FIELD_PARAM);
        for (String predefinedField : predefinedFields) {
            fieldParams.add(predefinedField.trim());
        }
        return fieldParams;
    }

    private static Set<String> getWhitelist(SolrParams params) {
        Set<String> whiteList = Sets.newHashSet();
        String[] predefinedLangs = params.get(MultiLangIdentifierSetting.LANG_WHITELIST, "").split(
                MultiLangIdentifierSetting.DELIMITER_BETWEEN_FIELD_PARAM);
        for (String predefinedLang : predefinedLangs) {
            whiteList.add(predefinedLang.trim());
        }
        return whiteList;
    }

    private static String getFallback(SolrParams params) {
        return params.get(MultiLangIdentifierSetting.FALLBACK);
    }

    private static double getThreshold(SolrParams params) {
        return params
                .getDouble(MultiLangIdentifierSetting.THRESHOLD, MultiLangIdentifierSetting.DEFAULT_LANG_THRESHOLD);
    }

    private static boolean isHidePreLangs(SolrParams params) {
        return params.getBool(MultiLangIdentifierSetting.HIDE_PREDEFINED_LANGS,
                MultiLangIdentifierSetting.DEFAULT_HIDE_PREDEFINED_LANG);
    }

    private static boolean isEnabled(SolrParams params) {
        return params.getBool(MultiLangIdentifierSetting.MULTI_LANG_ID, MultiLangIdentifierSetting.DEFAULT_ENABLED);
    }
}