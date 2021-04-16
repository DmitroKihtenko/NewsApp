package na.pojo;

import na.service.Assertions;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

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
        Assertions.isNotNull(title, "Title", logger);
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        Assertions.isNotNull(description, "Description", logger);
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
        Assertions.isNotNull(author, "Author", logger);

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
        if(url != null) {
            try {
                new URL(url).toURI();
            } catch (MalformedURLException | URISyntaxException e) {
                logger.info("It has been set invalid url format");

                throw e;
            }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        News news = (News) o;
        return Objects.equals(title, news.title) &&
                Objects.equals(description, news.description) &&
                Objects.equals(imageUrl, news.imageUrl) &&
                Objects.equals(author, news.author) &&
                Objects.equals(url, news.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, imageUrl, author, url);
    }
}
