package com.pleasecode.solr.langdetect;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.LangDetectLanguageIdentifierUpdateProcessorFactory;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.apache.solr.util.SolrPluginUtils;

public class MultiLangDetectLanguageIdentifierUpdateProcessorFactory extends
        LangDetectLanguageIdentifierUpdateProcessorFactory {

    @Override
    public UpdateRequestProcessor getInstance(SolrQueryRequest req, SolrQueryResponse rsp, UpdateRequestProcessor next) {
        if (req != null) {
            SolrPluginUtils.setDefaults(req, defaults, appends, invariants);
        }
        return new MultiLangDetectLanguageIdentifierUpdateProcessor(req, rsp, next);
    }
}
