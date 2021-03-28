package controller.util;

import org.apache.http.HttpResponse;
import org.apache.log4j.Logger;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import pojo.News;
import pojo.ResultAndError;
import service.Http;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Objects;

public class DocxCreator implements ResponseCreator {
    private static final Logger logger = Logger.getLogger(DocxCreator.class);
    private final static String newsTemplatePath;
    private final static int imagePixelsWidth;
    private int connectionTimeout;

    private int tempWidthValue;
    private int tempHeightValue;

    static {
        newsTemplatePath = "/NewsTemplate.dotx";
        imagePixelsWidth = 400;
    }

    public DocxCreator() {
        connectionTimeout = 3000;
    }

    private void generateImageSize(ByteArrayOutputStream imageStream) throws IOException {
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageStream.toByteArray()));
        float coefficient = (float)imagePixelsWidth / image.getWidth();
        tempHeightValue = (int)((float)image.getHeight() * coefficient);
        tempWidthValue = Units.toEMU(imagePixelsWidth);
        tempHeightValue = Units.toEMU(tempHeightValue);
    }

    private InputStream getImage(String uri) throws IOException {
        HttpResponse response = Http.getResponse(uri, connectionTimeout);
        int statusCode = response.getStatusLine().getStatusCode();

        if(statusCode < 200 || statusCode >= 300) {
            throw new IOException(
                    "News image source returned status code " + statusCode
            );
        }

        return response.getEntity().getContent();
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        if(connectionTimeout <= 0) {
            throw new IllegalArgumentException(
                    "Timeout connection parameter has non-positive value"
            );
        }
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public ResponseEntity<ByteArrayResource> entity(Object body) {
        ByteArrayResource resourceBody = (ByteArrayResource) body;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=newsList.docx")
                .contentType(new MediaType("application",
                        "vnd.openxmlformats-officedocument." +
                                "wordprocessingml.document"))
                .contentLength(resourceBody.contentLength())
                .body(resourceBody);
    }

    @Override
    public ResultAndError<ByteArrayResource> body(
            Iterable<News> newsList) {
        ResultAndError<ByteArrayResource> rae;
        try {
            logger.info("Starting creating response body");

            XWPFDocument template = new XWPFDocument(getClass().
                    getResourceAsStream(newsTemplatePath));
            XWPFDocument doc = new XWPFDocument();

            XWPFStyles docStyles = doc.createStyles();
            docStyles.setStyles(template.getStyle());
            ByteArrayOutputStream cloneStream;

            XWPFParagraph paragraph;

            for(News news : newsList) {
                paragraph = doc.createParagraph();
                paragraph.setStyle("NewsTitle");
                paragraph.createRun().setText(news.getTitle());

                if(news.getImageUrl() != null) {
                    try {
                        cloneStream = new
                                ByteArrayOutputStream();
                        getImage(news.getImageUrl()).
                                transferTo(cloneStream);
                        generateImageSize(cloneStream);

                        paragraph = doc.createParagraph();
                        paragraph.setStyle("Image");
                        paragraph.createRun().addPicture(new
                                        ByteArrayInputStream(
                                        cloneStream.toByteArray()),
                                XWPFDocument.PICTURE_TYPE_JPEG,
                                "picture.jpg", tempWidthValue,
                                tempHeightValue);
                    } catch (Exception e) {
                        logger.warn("News image include error: " +
                                Objects.requireNonNullElse(
                                        e.getMessage(),
                                        e.toString()));
                    }
                }

                if(news.getDescription() != null) {
                    paragraph = doc.createParagraph();
                    paragraph.setStyle("Description");
                    paragraph.createRun().setText(news.getDescription());
                }

                if(news.getAuthor() != null) {
                    paragraph = doc.createParagraph();
                    paragraph.setStyle("Author");
                    paragraph.createRun().setText(news.getAuthor());
                }

                if(news.getUrl() != null) {
                    paragraph = doc.createParagraph();
                    paragraph.setStyle("Link");
                    paragraph.createRun().setText(news.getUrl());
                }
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            doc.write(os);
            return new ResultAndError<>(new ByteArrayResource(os.
                    toByteArray()));
        } catch (Exception e) {
            logger.error(e.toString());

            rae = new ResultAndError<>("responseCreatingError",
                    e.getMessage());
        }

        logger.info("Successfully created");

        return rae;
    }
}
