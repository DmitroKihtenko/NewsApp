package na.pojo;

import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class News {
    private static final Logger logger = Logger.getLogger(News.class);

    private String title;
    private String description;
    private String imageUrl;
    private String author;
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if(title == null) {
            logger.error("Title parameter has null value");

            throw new IllegalArgumentException(
                    "Title parameter has null value"
            );
        }
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if(description == null) {
            logger.error("Description parameter has null value");

            throw new IllegalArgumentException(
                    "Description parameter has null value"
            );
        }
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String url) throws MalformedURLException, URISyntaxException {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            logger.info("It has been set invalid url format");

            throw e;
        }
        this.imageUrl = url;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        if(author == null) {
            logger.error("Author parameter has null value");

            throw new IllegalArgumentException(
                    "Author parameter has null value");
        }
        if(!author.equals("")) {
            this.author = author;
        } else {
            logger.info("It has been set empty string author " +
                    "parameter");
        }
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) throws MalformedURLException, URISyntaxException {
        try {
            new URL(url).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            logger.info("It has been set invalid url format");

            throw e;
        }
        this.url = url;
    }

    @Override
    public String toString() {
        return "News{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", author='" + author + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
