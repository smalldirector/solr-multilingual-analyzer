package com.pleasecode.solr.helper;

import com.google.common.base.Strings;
import org.apache.lucene.analysis.charfilter.HTMLStripCharFilter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

public final class HTMLScriptCharFilterHelper {

    public static String filterHTML(String source) throws IOException {
        if (Strings.isNullOrEmpty(source)) {
            return null;
        }
        return filterHTML(new StringReader(source));
    }

    public static String filterHTML(Reader source) throws IOException {
        if (source == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        HTMLStripCharFilter reader = new HTMLStripCharFilter(source);
        int ch;
        while ((ch = reader.read()) != -1) {
            builder.append((char) ch);
        }
        return builder.toString();
    }
}
