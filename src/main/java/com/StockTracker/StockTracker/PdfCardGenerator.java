package com.StockTracker.StockTracker;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PdfCardGenerator {

    public InputStream generateCard(String taiwaneseName, String member_id, String uniqueId) throws IOException, WriterException {

        // Load the background image from the resources folder inside the JAR
        InputStream backgroundStream = getClass().getResourceAsStream("/output/card_background.jpg");

        if (backgroundStream == null) {
            throw new IOException("Background image not found in resources");
        }

        // Convert InputStream to BufferedImage
        BufferedImage bufferedImage = ImageIO.read(backgroundStream);

        String uniqueUrl = "https://member.stsa.tw/authorizedMember/" + uniqueId;

        // Create a new document
        PDDocument document = new PDDocument();

        // Adjust the page size to make it flatter
        PDRectangle cardSize = new PDRectangle(400, 250); // Flatter dimensions
        PDPage page = new PDPage(cardSize);
        document.addPage(page);

        // Load Noto Sans TC font from the resources folder
        PDType0Font font = PDType0Font.load(document, getClass().getResourceAsStream("/font/NotoSansTC-VariableFont_wght.ttf"));

        // Create a content stream to draw on the page
        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Convert the BufferedImage to PDImageXObject
        PDImageXObject backgroundImage = LosslessFactory.createFromImage(document, bufferedImage);

        // Add the background image to cover the entire page
        contentStream.drawImage(backgroundImage, 0, 0, page.getMediaBox().getWidth(), page.getMediaBox().getHeight());

        // Generate the QR code in-memory
        BufferedImage qrCodeImage = generateQRCodeImage(uniqueUrl);

        // Convert BufferedImage to PDImageXObject
        PDImageXObject qrCodeXObject = LosslessFactory.createFromImage(document, qrCodeImage);

        // Add the QR code in the top right corner
        contentStream.drawImage(qrCodeXObject, page.getMediaBox().getWidth() - 138, page.getMediaBox().getHeight() - 150, 100, 100); // Adjust size and position

        // Set the text color to white
        contentStream.setNonStrokingColor(Color.WHITE);

        // Add the fake member number above the name
        contentStream.beginText();
        contentStream.setFont(font, 18);
        contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 138, 50); // Position above the name
        contentStream.showText(member_id);
        contentStream.endText();

        // Add "會員" in front of the Taiwanese name
        contentStream.beginText();
        contentStream.setFont(font, 18);
        contentStream.newLineAtOffset(page.getMediaBox().getWidth() - 138, 20); // Adjust the position for the name
        contentStream.showText("會員 " + taiwaneseName); // Add "會員" before the name
        contentStream.endText();

        contentStream.close();

        // Save the document to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        document.save(outputStream);
        document.close();

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private BufferedImage generateQRCodeImage(String data) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 200, 200);
        return MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig());
    }
}
