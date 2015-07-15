package com.pleasecode.solr.analysis;

import com.pleasecode.solr.setting.MultiLangFieldSetting;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.miscellaneous.RemoveDuplicatesTokenFilter;
import org.apache.solr.schema.IndexSchema;

import java.io.Reader;

/**
 * <code>MultiLangAnalyzer</code> is the custom analyzer to support multilingual analysis.
 */
public class MultiLangAnalyzer extends Analyzer {

    private final IndexSchema schema;
    private final MultiLangFieldSetting setting;

    public MultiLangAnalyzer(IndexSchema schema, MultiLangFieldSetting setting) {
        super(Analyzer.PER_FIELD_REUSE_STRATEGY);
        this.schema = schema;
        this.setting = setting;
    }

    @Override
    public TokenStreamComponents createComponents(String fieldName, Reader reader) {
        MultiLangTokenizer tokenizer = new MultiLangTokenizer(schema, setting, fieldName, reader);
        Tokenizer source = tokenizer;
        TokenStream result = tokenizer;
        if (setting.isRemoveDuplicates()) {
            result = new RemoveDuplicatesTokenFilter(result);
        }
        return new TokenStreamComponents(source, result);
    }

    public IndexSchema getSchema() {
        return schema;
    }

    public MultiLangFieldSetting getSetting() {
        return setting;
    }
}
