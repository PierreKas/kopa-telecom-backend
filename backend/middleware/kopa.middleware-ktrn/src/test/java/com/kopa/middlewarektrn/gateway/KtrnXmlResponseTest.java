package com.kopa.middlewarektrn.gateway;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.kopa.middlewarektrn.gateway.dto.KtrnXmlResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for feature 2.1.1 (Searching Balance): KtrnApiClient.call() normalizes all
 * incoming XML tags to lowercase before parsing (XmlTagCaseNormalizer), which must stay in sync
 * with KtrnXmlResponse.Body's localName - this exact mismatch (REMAIN_AMT vs remain_amt) broke
 * silently once when the generic case-insensitive modules were added, caught only by this test.
 */
class KtrnXmlResponseTest {

    private final XmlMapper xmlMapper = new XmlMapper();

    KtrnXmlResponseTest() {
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Test
    void parsesRemainAmtThroughTheSameNormalizationKtrnApiClientApplies() throws Exception {
        // RBSS-CCBS API_ACCESS s2.1.1's own response shape (REMAIN_AMT, as documented).
        String xml = """
                <?xml version='1.0' encoding='UTF-8' standalone='yes' ?>
                <response>
                  <header>
                    <svccode>HP00001</svccode>
                    <opcode>HPOP001</opcode>
                    <chcode>A</chcode>
                    <msgcode>1001</msgcode>
                    <message>Successfully processed.</message>
                  </header>
                  <body>
                    <REMAIN_AMT>15000</REMAIN_AMT>
                  </body>
                </response>
                """;

        KtrnXmlResponse response = xmlMapper.readValue(
                XmlTagCaseNormalizer.toLowerCaseTags(xml), KtrnXmlResponse.class);

        assertEquals("1001", response.getHeader().getMsgcode());
        assertEquals("15000", response.getBody().getRemainAmt());
    }
}
