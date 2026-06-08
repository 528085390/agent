package com.li.liaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.li.liaiagent.tools.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * PDF 生成工具类
 */
public class PDFGenerationTool {


    @Tool(description = "generate pdf with content and file name",returnDirect = true)
    public String generatePDF(@ToolParam(description = "content to included in the pdf") String content,
                              @ToolParam(description = "file name to save the pdf") String fileName) {
        String fileDir = FileConstant.FILE_SAVE_PATH + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try{
            FileUtil.mkdir(fileDir);
            try(PdfWriter writer = new PdfWriter(filePath);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);
            ) {
                PdfFont font = PdfFontFactory.createFont("STSongStd-Light", "UniGB-UCS2-H");
                document.setFont(font);

                // 添加段落
                Paragraph paragraph = new Paragraph(content);
                document.add(paragraph);
            }
            return "PDF generated successfully: " + filePath;

        } catch (Exception e) {
            return "Error generating PDF: " + e.getMessage();
        }

    }
}
