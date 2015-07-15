package com.pleasecode.solr.analysis;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.pleasecode.solr.setting.AnalyzerMode;
import com.pleasecode.solr.setting.MultiLangFieldSetting;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.solr.common.SolrException;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.schema.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * <code>MultiLangTokenizer</code> is the custom tokenizer to support
 * multilingual tokenize.
 */
public class MultiLangTokenizer extends Tokenizer {

    private static final Logger logger = LoggerFactory.getLogger(MultiLangTokenizer.class);
    private static final Stopwatch watcher = new Stopwatch();

    private IndexSchema schema;
    private MultiLangFieldSetting setting;
    private String fieldName;
    private MultiLangReaderWrapper readerWrapper;

    private CharTermAttribute charTermAttribute;
    private OffsetAttribute offsetAttribute;
    private TypeAttribute typeAttribute;
    private PositionIncrementAttribute positionIncrementAttribute;

    private List<Token> tokens;
    private int startingOffset;
    private Map<String, Analyzer> fieldTypeAnalyzers;

    public MultiLangTokenizer(IndexSchema schema, MultiLangFieldSetting setting, String fieldName, Reader input) {
        super(input);
        this.schema = schema;
        this.setting = setting;
        this.fieldName = fieldName;
        initAttributes();
    }

    private void initAttributes() {
        this.charTermAttribute = addAttribute(CharTermAttribute.class);
        this.offsetAttribute = addAttribute(OffsetAttribute.class);
        this.typeAttribute = addAttribute(TypeAttribute.class);
        this.positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
    }

    @Override
    public boolean incrementToken() throws IOException {
        if (null == tokens) {
            if (logger.isDebugEnabled()) {
                watcher.reset();
                watcher.start();
            }
            String data = fetchDataFromReader(this.readerWrapper.getReader());
            if (StringUtils.isEmpty(data)) {
                return false;
            }
            this.tokens = mergeTokenStream(mappingTokenPosition(data));
            if (null == this.tokens) {
                return false;
            }
            if (logger.isDebugEnabled()) {
                watcher.stop();
                logger.debug("Tokenize source '{}' cost {} ms.", data, watcher.elapsed(TimeUnit.MILLISECONDS));
            }
        }

        if (this.tokens.isEmpty()) {
            this.tokens = null;
            return false;
        } else {
            clearAttributes();

            Token firstToken = this.tokens.remove(0);
            this.charTermAttribute.copyBuffer(firstToken.buffer(), 0, firstToken.length());
            this.offsetAttribute.setOffset(firstToken.startOffset() + this.startingOffset, firstToken.endOffset()
                    + this.startingOffset);
            this.typeAttribute.setType(firstToken.type());
            this.positionIncrementAttribute.setPositionIncrement(firstToken.getPositionIncrement());

            return true;
        }
    }

    private List<Token> mergeTokenStream(Map<Integer, List<Token>> tokenPosMap) {
        List<Token> rsList = Lists.newLinkedList();

        int prevPos = 0;
        for (int pos : tokenPosMap.keySet()) {
            int tokenIncIndex = rsList.size();
            List<Token> tokens = tokenPosMap.get(pos);
            for (Token token : tokens) {
                token.setPositionIncrement(0);
                rsList.add(token);
            }

            if (rsList.size() > tokenIncIndex && null != rsList.get(tokenIncIndex)) {
                rsList.get(tokenIncIndex).setPositionIncrement(pos - prevPos);
            }
            prevPos = pos;
        }
        return rsList;
    }

    private Map<Integer, List<Token>> mappingTokenPosition(String data) throws IOException {
        SortedMap<Integer, List<Token>> tokenPosMap = Maps.newTreeMap();

        for (Map.Entry<String, Analyzer> entry : this.fieldTypeAnalyzers.entrySet()) {
            String tokenStreamName = this.fieldName + " " + entry.getKey();
            TokenStream tokenStream = entry.getValue().tokenStream(tokenStreamName, new StringReader(data));
            handleTokenStream(tokenPosMap, tokenStream);
        }

        return tokenPosMap;
    }

    private void handleTokenStream(Map<Integer, List<Token>> tokenPosMap, TokenStream tokenStream) throws IOException {
        tokenStream.reset();
        int pos = 0;

        CharTermAttribute charTermAttribute = getCharTermAttribute(tokenStream);
        OffsetAttribute offsetAttribute = getOffsetAttribute(tokenStream);
        TypeAttribute typeAttribute = getTypeAttribute(tokenStream);
        PositionIncrementAttribute positionIncrementAttribute = getPositionIncrementAttribute(tokenStream);

        while (tokenStream.incrementToken()) {
            if (null == charTermAttribute || null == offsetAttribute) {
                return;
            }
            Token token = new Token(charTermAttribute.buffer(), 0, charTermAttribute.length(),
                    offsetAttribute.startOffset(), offsetAttribute.endOffset());
            if (null != typeAttribute) {
                token.setType(typeAttribute.type());
            }
            pos += null != positionIncrementAttribute ? positionIncrementAttribute.getPositionIncrement() : 1;
            if (!tokenPosMap.containsKey(pos)) {
                tokenPosMap.put(pos, new LinkedList<Token>());
            }
            tokenPosMap.get(pos).add(token);
        }
        tokenStream.close();
    }

    private CharTermAttribute getCharTermAttribute(TokenStream tokenStream) {
        CharTermAttribute charTermAttribute = null;
        if (tokenStream.hasAttribute(CharTermAttribute.class)) {
            charTermAttribute = tokenStream.getAttribute(CharTermAttribute.class);
        }
        return charTermAttribute;
    }

    private OffsetAttribute getOffsetAttribute(TokenStream tokenStream) {
        OffsetAttribute offsetAttribute = null;
        if (tokenStream.hasAttribute(CharTermAttribute.class)) {
            offsetAttribute = tokenStream.getAttribute(OffsetAttribute.class);
        }
        return offsetAttribute;
    }

    private TypeAttribute getTypeAttribute(TokenStream tokenStream) {
        TypeAttribute typeAttribute = null;
        if (tokenStream.hasAttribute(TypeAttribute.class)) {
            typeAttribute = tokenStream.getAttribute(TypeAttribute.class);
        }
        return typeAttribute;
    }

    private PositionIncrementAttribute getPositionIncrementAttribute(TokenStream tokenStream) {
        PositionIncrementAttribute positionIncrementAttribute = null;
        if (tokenStream.hasAttribute(PositionIncrementAttribute.class)) {
            positionIncrementAttribute = tokenStream.getAttribute(PositionIncrementAttribute.class);
        }
        return positionIncrementAttribute;
    }

    private String fetchDataFromReader(Reader reader) throws IOException {
        StringBuffer data = new StringBuffer();
        CharBuffer buffer = CharBuffer.allocate(512);
        while (reader.read(buffer) > 0) {
            buffer.flip();
            data.append(buffer);
            buffer.rewind();
        }
        return data.toString();
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        this.tokens = null;
        if (logger.isDebugEnabled()) {
            watcher.reset();
            watcher.start();
        }
        if (null == this.readerWrapper) {
            this.readerWrapper = new MultiLangReaderWrapper(this.input, this.setting.getKeyFromTextDelimiter(),
                    this.setting.getMultiKeyDelimiter());
        } else {
            this.readerWrapper.wrapReader(this.input);
        }
        if (logger.isDebugEnabled()) {
            watcher.stop();
            logger.debug("The cost time of wrapping input source is {} ms.", watcher.elapsed(TimeUnit.MILLISECONDS));
        }
        this.startingOffset = this.readerWrapper.getStrippingLength();
        this.fieldTypeAnalyzers = getFieldTypeAnalyzers();
    }

    private Map<String, Analyzer> getFieldTypeAnalyzers() {
        Map<String, Analyzer> fieldTypeAnalyzers = Maps.newLinkedHashMap();
        Set<String> langKeys = this.readerWrapper.getLangKeys();
        Map<String, String> fieldTypeMappings = this.setting.getFieldTypeMappings();
        AnalyzerMode analyzerMode = this.setting.getAnalyzerMode();

        for (String langKey : langKeys) {
            String fieldTypeName = langKey;
            if (null != fieldTypeMappings) {
                fieldTypeName = fieldTypeMappings.get(langKey);
            }
            handleFieldTypeAnalyzers(fieldTypeAnalyzers, analyzerMode, fieldTypeName);
        }

        if (fieldTypeAnalyzers.isEmpty()) {
            handleFieldTypeAnalyzers(fieldTypeAnalyzers, analyzerMode, this.setting.getDefaultFieldType());
        }

        if (fieldTypeAnalyzers.isEmpty()) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST,
                    "No fieldTypeMappings and defaultFieldType settings in MultiLangField, at least set one!");
        }
        return fieldTypeAnalyzers;
    }

    private void handleFieldTypeAnalyzers(Map<String, Analyzer> fieldTypeAnalyzers, AnalyzerMode analyzerMode,
                                          String fieldTypeName) {
        if (StringUtils.isEmpty(fieldTypeName)) {
            return;
        }
        FieldType fieldType = this.schema.getFieldTypeByName(fieldTypeName);
        if (null == fieldType && !this.setting.isIgnoreMissingMappings()) {
            throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "Invalid FieldType: " + fieldTypeName);
        }
        if (analyzerMode == AnalyzerMode.query) {
            fieldTypeAnalyzers.put(fieldTypeName, fieldType.getQueryAnalyzer());
        } else if (analyzerMode == AnalyzerMode.multiTerm) {
            fieldTypeAnalyzers.put(fieldTypeName, ((TextField) fieldType).getMultiTermAnalyzer());
        } else {
            fieldTypeAnalyzers.put(fieldTypeName, fieldType.getAnalyzer());
        }
    }

    public IndexSchema getSchema() {
        return schema;
    }

    public MultiLangFieldSetting getSetting() {
        return setting;
    }

    public String getFieldName() {
        return fieldName;
    }
}
