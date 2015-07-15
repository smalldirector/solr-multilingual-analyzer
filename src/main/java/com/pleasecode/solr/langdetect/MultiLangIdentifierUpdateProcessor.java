package com.pleasecode.solr.langdetect;

import com.google.common.collect.Sets;
import com.pleasecode.solr.analysis.MultiLangAnalyzer;
import com.pleasecode.solr.helper.HTMLScriptCharFilterHelper;
import com.pleasecode.solr.helper.MultiLangIdentifierSettingHelper;
import com.pleasecode.solr.schema.MultiLangField;
import com.pleasecode.solr.setting.MultiLangFieldSetting;
import com.pleasecode.solr.setting.MultiLangIdentifierSetting;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.SolrInputField;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public abstract class MultiLangIdentifierUpdateProcessor extends UpdateRequestProcessor {

    private final static float DEFAULT_BOOST_OF_SOLR_INPUT_DOCUMENT = 1.0F;

    private IndexSchema schema;
    private MultiLangIdentifierSetting setting;

    public MultiLangIdentifierUpdateProcessor(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        super(next);
        this.schema = req.getSchema();
        this.setting = MultiLangIdentifierSettingHelper.valueOf(req.getParams());
    }

    @Override
    public void processAdd(AddUpdateCommand cmd) throws IOException {
        if (this.setting.isEnabled()) {
            process(cmd.getSolrInputDocument());
        }
        super.processAdd(cmd);
    }

    public SolrInputDocument process(SolrInputDocument doc) {
        Set<String> fieldParams = this.setting.getFieldParams();
        if (null != fieldParams && !fieldParams.isEmpty()) {
            for (String fieldParam : fieldParams) {
                SchemaField schemaField = this.schema.getField(fieldParam);
                if (null != schemaField && schemaField.getType() instanceof MultiLangField) {
                    try {
                        predefineLangToMultiLangField(doc, fieldParam);
                    } catch (IOException e) {
                        throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, e);
                    }
                }
            }
        } else {
            throw new SolrException(
                    SolrException.ErrorCode.BAD_REQUEST,
                    "Missing or faulty configuration of MultiLangIdentifierUpdateProcessor. Input fields must be specified as a comma separated list");
        }

        return doc;
    }

    private void predefineLangToMultiLangField(SolrInputDocument doc, String fieldParam) throws IOException {
        SolrInputField inputField = doc.getField(fieldParam);
        SolrInputField newInputField = new SolrInputField(fieldParam);

        MultiLangField multiLangField = (MultiLangField) this.schema.getFieldType(fieldParam);
        MultiLangFieldSetting multiLangFieldSetting = ((MultiLangAnalyzer) multiLangField.getAnalyzer()).getSetting();
        if (null != inputField && null != inputField.getValues()) {
            for (Object value : inputField.getValues()) {
                if (value instanceof String) {
                    String source = HTMLScriptCharFilterHelper.filterHTML((String) value);
                    List<DetectedLanguage> detectedLanguages = detectLanguage(source);
                    Set<String> langSet = resolveLanguage(detectedLanguages, this.setting.getFallback());

                    StringBuffer predefinedLangs = new StringBuffer();
                    for (String lang : langSet) {
                        if (multiLangFieldSetting.isIgnoreMissingMappings()
                                || multiLangFieldSetting.getFieldTypeMappings().containsKey(lang)) {
                            if (predefinedLangs.length() > 0) {
                                predefinedLangs.append(multiLangFieldSetting.getMultiKeyDelimiter());
                            }
                            predefinedLangs.append(lang);
                        }
                    }
                    if (predefinedLangs.length() > 0) {
                        predefinedLangs.append(multiLangFieldSetting.getKeyFromTextDelimiter());
                    }

                    if (this.setting.isHidePreLangs()) {
                        predefinedLangs.insert(0, MultiLangFieldSetting.LEFT_DELIMITER_OF_HIDDEN_FLAG);
                        predefinedLangs.append(MultiLangFieldSetting.RIGHT_DELIMITER_OF_HIDDEN_FLAG);
                    }

                    String newValue = predefinedLangs + source;
                    newInputField.addValue(newValue, DEFAULT_BOOST_OF_SOLR_INPUT_DOCUMENT);
                    newInputField.setBoost(inputField.getBoost());

                    doc.remove(inputField);
                    doc.put(fieldParam, newInputField);
                }
            }
        }
    }

    public Set<String> resolveLanguage(List<DetectedLanguage> languages, String fallbackLang) {
        Set<String> langSet = Sets.newHashSet();
        if (null == languages || languages.isEmpty()) {
            langSet.add(fallbackLang);
        } else {
            Set<String> whitelist = this.setting.getWhiteList();
            for (DetectedLanguage lang : languages) {
                String langCode = lang.getLangCode();
                if (whitelist.isEmpty() || whitelist.contains(langCode)) {
                    if (lang.getCertainty() >= this.setting.getThreshold()) {
                        langSet.add(langCode);
                    }
                }
            }
            if (langSet.isEmpty()) {
                langSet.add(fallbackLang);
            }
        }
        return langSet;
    }

    /**
     * Detects language(s) from a string. Classes wishing to implement their own
     * language detection module should override this method.
     *
     * @param content The content to identify
     * @return List of detected language(s) according to RFC-3066
     */
    protected abstract List<DetectedLanguage> detectLanguage(String content);
}
