package com.pleasecode.solr.langdetect;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.cybozu.labs.langdetect.Language;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.processor.UpdateRequestProcessor;

import java.util.ArrayList;
import java.util.List;

public class MultiLangDetectLanguageIdentifierUpdateProcessor extends MultiLangIdentifierUpdateProcessor {

    public MultiLangDetectLanguageIdentifierUpdateProcessor(SolrQueryRequest req, SolrQueryResponse rsp,
                                                            UpdateRequestProcessor next) {
        super(req, rsp, next);
    }

    @Override
    protected List<DetectedLanguage> detectLanguage(String content) {
        List<DetectedLanguage> detectedLanguages = Lists.newArrayList();
        if (StringUtils.isEmpty(content)) {
            return detectedLanguages;
        }
        try {
            Detector detector = DetectorFactory.create();
            detector.append(content);
            ArrayList<Language> languages = detector.getProbabilities();
            for (Language language : languages) {
                detectedLanguages.add(new DetectedLanguage(language.lang, language.prob));
            }
        } catch (LangDetectException e) {
        }
        return detectedLanguages;
    }
}
