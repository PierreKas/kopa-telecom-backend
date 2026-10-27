package com.kopa.middlewarektrn.gateway.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Generic KTRN &lt;response&gt; shape for every operation whose &lt;body&gt; isn't just the
 * single REMAIN_AMT field {@link KtrnXmlResponse} models - the body's children vary per
 * operation (PROD_CNT/PROD_NM/..., RESULT, CUST_ID/CUST_NM, ...), so it's parsed into a flat
 * String map instead of a typed class per operation. Each module's ServiceImpl reads only the
 * keys that operation's doc says it returns.
 */
@JacksonXmlRootElement(localName = "response")
@Getter
@Setter
public class KtrnGenericXmlResponse {

    @JacksonXmlProperty(localName = "header")
    private Header header;

    @JacksonXmlProperty(localName = "body")
    private Map<String, String> body;

    @Getter
    @Setter
    public static class Header {
        @JacksonXmlProperty(localName = "svccode")
        private String svccode;

        @JacksonXmlProperty(localName = "opcode")
        private String opcode;

        @JacksonXmlProperty(localName = "chcode")
        private String chcode;

        @JacksonXmlProperty(localName = "msgcode")
        private String msgcode;

        @JacksonXmlProperty(localName = "message")
        private String message;
    }

    /** Null-safe, case-insensitive lookup by the field name as written in the RBSS-CCBS doc (e.g.
     *  {@code "PROD_CNT"}) - the incoming XML's tags are lowercased before parsing (see
     *  XmlTagCaseNormalizer), so the map's actual keys are always lowercase regardless of what
     *  the gateway sent; callers shouldn't have to remember that. KTRN also omits fields entirely
     *  rather than sending them empty on error responses, hence the null-safety. */
    public String bodyValue(String key) {
        return body == null || key == null ? null : body.get(key.toLowerCase());
    }
}
