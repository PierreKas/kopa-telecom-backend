package com.kopa.middlewarektrn.gateway.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

/** Maps the RBSS-CCBS API_ACCESS &lt;response&gt; XML (s2.1.1 Response Item/Schema) to Java. */
@JacksonXmlRootElement(localName = "response")
@Getter
@Setter
public class KtrnXmlResponse {

    @JacksonXmlProperty(localName = "header")
    private Header header;

    @JacksonXmlProperty(localName = "body")
    private Body body;

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

    @Getter
    @Setter
    public static class Body {
        // Lowercase localName: KtrnApiClient normalizes all incoming tag names to lowercase
        // before parsing (see XmlTagCaseNormalizer) - the doc's own examples spell this
        // REMAIN_AMT, but the raw XML is lowercased before this class ever sees it.
        @JacksonXmlProperty(localName = "remain_amt")
        private String remainAmt;
    }
}
