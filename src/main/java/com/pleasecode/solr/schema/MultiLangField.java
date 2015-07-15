package com.pleasecode.solr.schema;

import com.pleasecode.solr.analysis.MultiLangAnalyzer;
import com.pleasecode.solr.helper.MultiLangFieldSettingHelper;
import com.pleasecode.solr.setting.MultiLangFieldSetting;
import com.pleasecode.solr.setting.AnalyzerMode;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.PreAnalyzedField;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.schema.TextField;

import java.io.StringReader;
import java.util.Map;

/**
 * <code>MultiLangField</code> is the custom type for configurable multilingual
 * text analysis.
 */
public class MultiLangField extends TextField {

    @Override
    protected void init(IndexSchema schema, Map<String, String> args) {
        super.init(schema, args);
        this.setAnalyzer(new MultiLangAnalyzer(schema, MultiLangFieldSettingHelper.valueOf(args, AnalyzerMode.index)));
        this.setQueryAnalyzer(new MultiLangAnalyzer(schema, MultiLangFieldSettingHelper.valueOf(args,
                AnalyzerMode.query)));
        this.setMultiTermAnalyzer(new MultiLangAnalyzer(schema, MultiLangFieldSettingHelper.valueOf(args,
                AnalyzerMode.multiTerm)));
        MultiLangFieldSettingHelper.clearArgs(args);
    }

    @Override
    public IndexableField createField(SchemaField field, Object value, float boost) {
        String source = String.valueOf(value);
        String target = hidePredefinedLangs(source);
        if (source.equals(target)) {
            return super.createField(field, value, boost);
        }
        return handleIndexableField(field, source, target, boost);
    }

    private IndexableField handleIndexableField(SchemaField field, String source, String target, float boost) {
        if (StringUtils.isEmpty(source) || StringUtils.isEmpty(target)) {
            return null;
        }
        FieldType fieldType = PreAnalyzedField.createFieldType(field);
        if (null == fieldType) {
            return null;
        }
        Field indexableField = null;
        if (field.stored()) {
            indexableField = new Field(field.getName(), target, fieldType);
        }
        if (field.indexed()) {
            TokenStream tokenStream = ((MultiLangAnalyzer) this.getAnalyzer()).createComponents(field.getName(),
                    new StringReader(source)).getTokenStream();
            if (null != indexableField) {
                indexableField.setTokenStream(tokenStream);
            } else {
                indexableField = new Field(field.getName(), tokenStream, fieldType);
            }
        }
        if (null != indexableField) {
            indexableField.setBoost(boost);
        }
        return indexableField;
    }

    private String hidePredefinedLangs(String value) {
        if (StringUtils.isNotEmpty(value)) {
            String hiddenFlagEnd = MultiLangFieldSetting.DEFAULT_KEY_FROM_TEXT_DELIMITER
                    + MultiLangFieldSetting.RIGHT_DELIMITER_OF_HIDDEN_FLAG;
            if (value.startsWith(MultiLangFieldSetting.LEFT_DELIMITER_OF_HIDDEN_FLAG) && value.contains(hiddenFlagEnd)) {
                value = value.substring(value.indexOf(hiddenFlagEnd) + hiddenFlagEnd.length(), value.length());
            }
        }
        return value;
    }
}
