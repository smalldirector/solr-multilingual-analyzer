solr-multilingual-analyzer[![Build Status](https://api.travis-ci.org/smalldirector/solr-multilingual-analyzer.svg)](https://travis-ci.org/smalldirector/solr-multilingual-analyzer)
============================

A new solr multilingual index and search architecture, it can support index and search across multiple languages at the same time in the same field.

### Feature

* Supports any combination of languages in one field.
* Support true multilingual searching, including mixed-language phrases.
* Not require store duplicated contents per language, reduce index size.
* Reduce fields count to be searched per request, increasing search speed.
* Support language detection([language-detection](https://code.google.com/p/language-detection/) plugin).
* Easy to be used, it can be used in any solr server as a plugin.

### Configuration
To use this plugin, you need to config ```field``` and ```fieldType``` in the ```schema.xml```, and config language detection in the ```solrconfig.xml```.

##### FieldType

In addition to support all of solr's default attributes, such as ```sortMissingLast```, ```positionIncrementGap```, etc,
we support some customize attributes for this special MultiLangField.

* Specify ```class``` value as this customize field type, ```class="com.pleasecode.solr.schema.MultiLangField"```.
* Specify ```fieldTypeMappings``` value, ex, ```en:text_en, zh-cn:text_smartcn, ja:text_ja```, use comma to split different field type configuration,
and use colon to split lang code and the mapping field type which was defined in ```schema.xml```.

The standard of lang codes that support [language-detection](https://code.google.com/p/language-detection/) plugin:

```
"af", "ar", "bg", "bn", "cs", "da", "de", "el", "en", "es", "et", "fa", "fi", "fr", "gu",
"he", "hi", "hr", "hu", "id", "it", "ja", "kn", "ko", "lt", "lv", "mk", "ml", "mr", "ne",
"nl", "no", "pa", "pl", "pt", "ro", "ru", "sk", "sl", "so", "sq", "sv", "sw", "ta", "te",
"th", "tl", "tr", "uk", "ur", "vi", "zh-cn", "zh-tw"
```

The following is the example code:

```xml
<fieldType name="multi_lang"
           class="com.pleasecode.solr.schema.MultiLangField"
           sortMissingLast="true"
           removeDuplicates="true"
           defaultFieldType="text_general"
           fieldTypeMappings="en:text_en, zh-cn:text_ik, ja:text_ja"/>
```

##### Field

Set type attribute of the field as MultiLangField type,

For example:

```xml
<field name="content" type="multi_lang" indexed="true" stored="true" omitNorms="true"/>
```

##### Language Identification

* ```multi-langid```: Enables/disables language detection, default is ```true```.
* ```multi-langid.fl```: A comma-delimited list of fields to be used in language identification. ```Required!!!```
* ```multi-langid.whitelist```: Specifies a list of allowed language identification codes.
If you specify langid.map, you can use the whitelist to ensure that you only index documents into fields that exist in your schema.
* ```multi-langid.fallback```: Specifies a language code to use if no language is detected or if no language is found in langid.fallbackFields.
If no fallback is defined, the final language code will be an empty string, which could cause unexpected behavior.
* ```multi-langid.threshold```: Specifies a threshold value between 0 and 1 that the language identification score must reach before being accepted.
With longer text fields, a high threshold such as 0.8 will yield better results. For shorter text fields, you may need to lower the threshold. Default is ```0.5```.
* ```multi-langid.hidePreLangs```: If true it will hide the predefined lang codes, otherwise, it will display the lang codes. Default is ```true```.

The following is the example code:

```xml
<updateRequestProcessorChain name="multi-langid">
  <processor class="com.pleasecode.solr.langdetect.MultiLangDetectLanguageIdentifierUpdateProcessorFactory">
        <lst name="defaults">
            <str name="multi-langid">true</str>
            <str name="multi-langid.fl">subject,content</str>
            <str name="multi-langid.whitelist">zh-cn,ja,en</str>
            <str name="multi-langid.fallback">en</str>
            <str name="multi-langid.threshold">0.8</str>
            <str name="multi-langid.hidePreLangs">false</str>
        </lst>
    </processor>
    <processor class="solr.LogUpdateProcessorFactory" />
    <processor class="solr.RunUpdateProcessorFactory" />
</updateRequestProcessorChain>

<requestHandler name="/update" class="solr.UpdateRequestHandler">
    <lst name="invariants">
      <str name="update.chain">multi-langid</str>
    </lst>
</requestHandler>
```
