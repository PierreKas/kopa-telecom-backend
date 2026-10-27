package com.kopa.middlewarektrn.gateway;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kopa.middlewarektrn.gateway.dto.KtrnGenericXmlResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Confirms the XmlTagCaseNormalizer + XmlMapper pipeline every new KTRN module (activation,
 * identity, registration, msisdn) relies on: a &lt;body&gt; with arbitrary/varying child elements
 * deserializes into a Map&lt;String,String&gt;, regardless of whether the source XML uses
 * lowercase or uppercase tags - the RBSS-CCBS API_ACCESS doc itself is inconsistent about this
 * (s2.1.1 uses lowercase, later sections use uppercase).
 */
class KtrnGenericXmlResponseTest {

    private final XmlMapper xmlMapper = new XmlMapper();

    KtrnGenericXmlResponseTest() {
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private KtrnGenericXmlResponse parse(String xml) throws Exception {
        return xmlMapper.readValue(XmlTagCaseNormalizer.toLowerCaseTags(xml), KtrnGenericXmlResponse.class);
    }

    @Test
    void parsesArbitraryBodyFieldsIntoAMap_lowercaseTags() throws Exception {
        // RBSS-CCBS API_ACCESS s2.5.1 (price plan search) response shape.
        String xml = """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <response>
                  <header>
                    <svccode>HP00005</svccode>
                    <opcode>HPOP301</opcode>
                    <chcode>A</chcode>
                    <msgcode>1001</msgcode>
                    <message>Successfully processed.</message>
                  </header>
                  <body>
                    <SO_ID>01</SO_ID>
                    <MSISDN>0771000050</MSISDN>
                    <PROD_CNT>2</PROD_CNT>
                    <PROD_NM>ORN 500MB BUNDLE(PRE),ORN 20GB BUNDLE(PRE)</PROD_NM>
                    <PROD_CD>PD00000574,PD00000575</PROD_CD>
                    <PROD_GRP>a,a</PROD_GRP>
                  </body>
                </response>
                """;

        KtrnGenericXmlResponse response = parse(xml);

        assertEquals("HP00005", response.getHeader().getSvccode());
        assertEquals("HPOP301", response.getHeader().getOpcode());
        assertEquals("1001", response.getHeader().getMsgcode());
        assertEquals("2", response.bodyValue("PROD_CNT"));
        assertEquals("PD00000574,PD00000575", response.bodyValue("PROD_CD"));
        assertEquals("ORN 500MB BUNDLE(PRE),ORN 20GB BUNDLE(PRE)", response.bodyValue("PROD_NM"));
        assertNull(response.bodyValue("NOT_PRESENT"));
    }

    @Test
    void parsesASingleFieldResultBody_uppercaseTags() throws Exception {
        // RBSS-CCBS API_ACCESS s2.7.1 (MSISDN status) response shape - note ALL-CAPS tags, unlike
        // the example above, exactly as the doc itself is inconsistent.
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <RESPONSE>
                    <HEADER>
                        <SVCODE>HP00005</SVCODE>
                        <OPCODE>HPOP601</OPCODE>
                        <CHCODE>A</CHCODE>
                        <MSGCODE>1001</MSGCODE>
                        <MESSAGE>IT HAS BEEN SUCCESSFULLY PROCESSED.</MESSAGE>
                    </HEADER>
                    <BODY>
                        <RESULT>Y</RESULT>
                    </BODY>
                </RESPONSE>
                """;

        KtrnGenericXmlResponse response = parse(xml);

        assertEquals("1001", response.getHeader().getMsgcode());
        assertEquals("Y", response.bodyValue("RESULT"));
    }
}
