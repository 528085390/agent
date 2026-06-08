package com.li.liaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PDFGenerationTest {

    @Test
    void generatePDF() {
        PDFGenerationTool pdfGeneration = new PDFGenerationTool();
        String result = pdfGeneration.generatePDF("Hello, this is a test PDF 你好 这是一个测试pdf.", "test.pdf");
        assertNotNull(result);
    }

}