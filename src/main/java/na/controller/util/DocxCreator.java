package na.controller.util;

import na.error.ResponseHandleException;
import org.apache.log4j.Logger;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import na.pojo.News;
import na.pojo.ResultAndError;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import na.controller.services.ImageLookupService;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component("docxCreator")
public class DocxCreator implements ResponseCreator {
    private static final Logger logger =
            Logger.getLogger(DocxCreator.class);

    private int tempWidthValue;
    private int tempHeightValue;

    private ImageLookupService imageLookupService;
    private String newsTemplatePath;
    private int imagePixelsWidth;

    @Autowired
    public DocxCreator(ImageLookupService imageLookupService,
                       @Value("${newsTemplatePath}") String newsTemplatePath) {
        if(imageLookupService == null) {
            logger.error("Image lookup service has null value");

            throw new IllegalArgumentException(
                    "Image lookup service has null value"
            );
        }
        this.imageLookupService = imageLookupService;

        if(newsTemplatePath == null) {
            throw new IllegalArgumentException(
                    "News template path parameter has null value"
            );
        }
        this.newsTemplatePath = newsTemplatePath;

        imagePixelsWidth = 400;
    }

    @Autowired
    public void setImagePixelsWidth(@Value("${newsImagePixelsWidth}") int imagePixelsWidth) {
        if(imagePixelsWidth <= 0) {
            throw new IllegalArgumentException(
                    "Image width parameter has non-positive value"
            );
        }
        this.imagePixelsWidth = imagePixelsWidth;
    }

    private void generateImageSize(ByteArrayInputStream imageStream)
            throws IOException {
        BufferedImage image = ImageIO.read(imageStream);
        float coefficient = (float)imagePixelsWidth / image.getWidth();
        tempHeightValue = (int)((float)image.getHeight() * coefficient);
        tempWidthValue = Units.toEMU(imagePixelsWidth);
        tempHeightValue = Units.toEMU(tempHeightValue);
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

            Map<Integer, ByteArrayOutputStream> imagesData;
            ByteArrayOutputStream imageStream;
            int newsIndex = 1;
            XWPFDocument template;

            try {
                template = new XWPFDocument(getClass().
                        getResourceAsStream(newsTemplatePath));
            } catch (Exception e) {
                logger.error("Error while creating word document " +
                        "file. " + newsTemplatePath + " template " +
                        "file can't be loaded");

                throw new ResponseHandleException("responseError",
                        newsTemplatePath + " template file can't " +
                                "be loaded");
            }

            XWPFDocument doc = new XWPFDocument();

            XWPFStyles docStyles = doc.createStyles();
            docStyles.setStyles(template.getStyle());

            XWPFParagraph paragraph;

            imagesData = imageLookupService.asyncRequests(newsList);

            for(News news : newsList) {
                paragraph = doc.createParagraph();
                paragraph.setStyle("NewsTitle");
                paragraph.createRun().setText(news.getTitle());

                if(imagesData.containsKey(newsIndex)) {
                    try {
                        imageStream = imagesData.get(newsIndex);
                        generateImageSize(new ByteArrayInputStream(
                                imageStream.toByteArray()));

                        paragraph = doc.createParagraph();
                        paragraph.setStyle("Image");
                        paragraph.createRun().addPicture(new
                                        ByteArrayInputStream(
                                        imageStream.toByteArray()),
                                XWPFDocument.PICTURE_TYPE_JPEG,
                                "picture.jpg", tempWidthValue,
                                tempHeightValue);
                    } catch (Exception e) {
                        logger.warn("News image include error: " +
                                e.toString());
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
                newsIndex++;
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            doc.write(os);
            rae = new ResultAndError<>(new ByteArrayResource(os.
                    toByteArray()));
        } catch (Exception e) {
            logger.error(e.toString());

            rae = new ResultAndError<>("responseCreatingError",
                    e.toString());

            return rae;
        }

        logger.info("Successfully created");

        return rae;
    }
}
