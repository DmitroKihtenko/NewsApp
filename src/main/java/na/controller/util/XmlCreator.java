package na.controller.util;

import org.apache.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import na.pojo.News;
import na.pojo.ResultAndError;
import na.service.MediaTypeLogic;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
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
                contentType(MediaTypeLogic.
                        createFromString(MediaType.
                                APPLICATION_XML_VALUE)).
                contentLength(bodyString.length()).
                body(bodyString);
    }

    @Override
    public ResultAndError<String> body(Iterable<News> newsList) {
        ResultAndError<String> rae;
        try {
            logger.info("Starting creating response body");

            DocumentBuilderFactory dbf = DocumentBuilderFactory.
                    newInstance();
            DocumentBuilder db  = dbf.newDocumentBuilder();
            Document doc = db.newDocument();
            Element root = doc.createElement("newsList");
            Element newsTag;
            Element newsProperty;

            for(News news : newsList) {
                newsTag = doc.createElement("news");

                if(news.getTitle() != null) {
                    newsProperty = doc.createElement("title");
                    newsProperty.setTextContent(news.getTitle());
                    newsTag.appendChild(newsProperty);
                }
                if(news.getAuthor() != null) {
                    newsProperty = doc.createElement("author");
                    newsProperty.setTextContent(news.getAuthor());
                    newsTag.appendChild(newsProperty);
                }
                if(news.getDescription() != null) {
                    newsProperty = doc.createElement("description");
                    newsProperty.setTextContent(news.getDescription());
                    newsTag.appendChild(newsProperty);
                }
                if(news.getUrl() != null) {
                    newsProperty = doc.createElement("url");
                    newsProperty.setTextContent(news.getUrl());
                    newsTag.appendChild(newsProperty);
                }
                if(news.getImageUrl() != null) {
                    newsProperty = doc.createElement("imageUrl");
                    newsProperty.setTextContent(news.getImageUrl());
                    newsTag.appendChild(newsProperty);
                }

                root.appendChild(newsTag);
            }

            doc.appendChild(root);

            DOMSource domSource = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.transform(domSource, result);

            rae = new ResultAndError<>(writer.toString());
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
