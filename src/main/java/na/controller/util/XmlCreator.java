package na.controller.util;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import na.pojo.News;
import na.pojo.ResultAndError;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component("xmlCreator")
public class XmlCreator implements ResponseCreator {
    private static final Logger logger =
            Logger.getLogger(XmlCreator.class);

    @Override
    public ResponseEntity<String> entity(Object body) {
        String bodyString = (String) body;

        return ResponseEntity.ok().
                header("Content-type",
                "application/xml;charset=utf-8").
                contentLength(bodyString.getBytes(
                        StandardCharsets.UTF_8).length).
                body(bodyString);
    }

    @Override
    public ResultAndError<String> body(Iterable<News> newsList) {
        ResultAndError<String> rae;
        try {
            logger.info("Starting creating response body");

            Document document = DocumentHelper.createDocument();
            document.setXMLEncoding("UTF-8");
            Element root = document.addElement("newsList");
            Element newsTag;
            ByteArrayOutputStream byteStream =
                    new ByteArrayOutputStream();

            for(News news : newsList) {
                newsTag = root.addElement("news");

                if(news.getTitle() != null) {
                    newsTag.addElement("title").
                            addText(news.getTitle());
                }
                if(news.getAuthor() != null) {
                    newsTag.addElement("author").
                            addText(news.getAuthor());
                }
                if(news.getDescription() != null) {
                    newsTag.addElement("description").
                            addText(news.getDescription());
                }
                if(news.getUrl() != null) {
                    newsTag.addElement("url").
                            addText(news.getUrl());
                }
                if(news.getImageUrl() != null) {
                    newsTag.addElement("imageUrl").
                            addText(news.getImageUrl());
                }
            }

            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding("UTF-8");
            XMLWriter writer = new XMLWriter(byteStream, format);
            writer.write(document);

            rae = new ResultAndError<>(byteStream.toString());
        } catch (Exception e) {
            logger.error(Objects.requireNonNullElse(e.getMessage(),
                    e.toString()));

            rae = new ResultAndError<>("responseCreatingError",
                    e.getMessage());
        }

        logger.info("Successfully created");

        return rae;
    }
}
