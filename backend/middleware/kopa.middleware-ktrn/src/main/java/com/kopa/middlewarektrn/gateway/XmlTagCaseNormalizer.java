package com.kopa.middlewarektrn.gateway;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lowercases XML element tag names only (not attribute values or text content) before parsing.
 * Needed because the RBSS-CCBS API_ACCESS doc's own examples are inconsistent about tag case -
 * s2.1.1 uses &lt;response&gt;/&lt;header&gt;, later sections use &lt;RESPONSE&gt;/&lt;HEADER&gt;
 * (confirmed by KtrnGenericXmlResponseTest) - so the real gateway's casing can't be assumed
 * either way. Jackson's {@code MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES} does not apply
 * to jackson-dataformat-xml's annotation-based property resolution, so this text-level
 * normalization runs before the XmlMapper ever sees the payload instead.
 */
final class XmlTagCaseNormalizer {

    private static final Pattern TAG_NAME = Pattern.compile("</?([A-Za-z_][\\w.-]*)");

    private XmlTagCaseNormalizer() {
    }

    static String toLowerCaseTags(String xml) {
        Matcher matcher = TAG_NAME.matcher(xml);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String whole = matcher.group();
            String tag = matcher.group(1);
            String replacement = whole.substring(0, whole.length() - tag.length()) + tag.toLowerCase();
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
