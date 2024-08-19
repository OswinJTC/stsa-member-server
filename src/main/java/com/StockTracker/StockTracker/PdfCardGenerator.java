package com.StockTracker.StockTracker;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PdfCardGenerator {

    public void generateCard(String name, String school, String logoUrl, String qrCodeUrl, String outputFileName) throws IOException {
        // Download images to local paths
        String logoPath = downloadImage(logoUrl, "logo.png");
        String qrCodePath = downloadImage(qrCodeUrl, "qrCode.png");

        // Create a new document
        PDDocument document = new PDDocument();

        // Set the page size to be horizontal (like a real name card)
        PDPage page = new PDPage(new PDRectangle(PDRectangle.A6.getHeight(), PDRectangle.A6.getWidth()));
        document.addPage(page);

        // Load Noto Sans TC font from the resources folder
        PDType0Font font = PDType0Font.load(document, getClass().getResourceAsStream("/font/NotoSansTC-VariableFont_wght.ttf"));

        // Create a content stream to draw on the page
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Set the background color to light brown
        contentStream.setNonStrokingColor(new Color(210, 180, 140)); // Light brown color
        contentStream.addRect(0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());
        contentStream.fill();

        // Add the logo in the top left corner
        PDImageXObject logo = PDImageXObject.createFromFile(logoPath, document);
        contentStream.drawImage(logo, 20, page.getMediaBox().getHeight() - 80, 120, 120); // Adjust size and position

        // Set the text color to black
        contentStream.setNonStrokingColor(Color.BLACK);

        // Add the name and school information on the right of the logo
        contentStream.beginText();
        contentStream.setFont(font, 24);
        contentStream.newLineAtOffset(100, page.getMediaBox().getHeight() - 40);
        contentStream.showText(name);
        contentStream.newLineAtOffset(0, -30);
        contentStream.showText(school);
        contentStream.endText();

        // Add the QR code below the information
        PDImageXObject qrCode = PDImageXObject.createFromFile(qrCodePath, document);
        contentStream.drawImage(qrCode, 100, page.getMediaBox().getHeight() - 160, 80, 80); // Adjust size and position

        contentStream.close();

        // Save the document to a file with the provided name
        String outputFilePath = "output/" + outputFileName + ".pdf";
        document.save(outputFilePath);
        document.close();

        // Print the path to the console
        System.out.println("PDF saved at: " + outputFilePath);
    }

    private String downloadImage(String imageUrl, String outputFileName) throws IOException {
        URL url = new URL(imageUrl);
        try (InputStream in = url.openStream()) {
            if (in == null) {
                throw new IOException("Failed to open stream for URL: " + imageUrl);
            }
            Path outputPath = Path.of("output", outputFileName);
            Files.createDirectories(outputPath.getParent());
            Files.copy(in, outputPath, StandardCopyOption.REPLACE_EXISTING);
            return outputPath.toString();
        } catch (Exception e) {
            System.err.println("Failed to download image from URL: " + imageUrl);
            throw e;
        }
    }

}
