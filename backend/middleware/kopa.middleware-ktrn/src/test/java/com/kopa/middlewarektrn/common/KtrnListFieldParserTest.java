package com.kopa.middlewarektrn.common;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KtrnListFieldParserTest {

    @Test
    void parsesProdListFromTheDocsWorkedExample() {
        // RBSS-CCBS API_ACCESS s2.1.3's own example, newlines/indentation as printed in the PDF.
        String prodList = "PD00000574,ORN 500MB BUNDLE(PRE),2300     |\n"
                + "                 PD00000575,ORN 10GB BUNDLE(PRE),39600";

        List<String[]> records = KtrnListFieldParser.parseRecords(prodList, fields -> fields);

        assertEquals(2, records.size());
        assertEquals("PD00000574", records.get(0)[0]);
        assertEquals("ORN 500MB BUNDLE(PRE)", records.get(0)[1]);
        assertEquals("2300", records.get(0)[2]);
        assertEquals("PD00000575", records.get(1)[0]);
    }

    @Test
    void blankInputYieldsEmptyList() {
        assertTrue(KtrnListFieldParser.parseRecords(null, f -> f).isEmpty());
        assertTrue(KtrnListFieldParser.parseRecords("  ", f -> f).isEmpty());
    }
}
