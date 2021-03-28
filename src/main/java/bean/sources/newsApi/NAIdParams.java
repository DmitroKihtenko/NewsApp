package bean.sources.newsApi;

import bean.sources.IdParams;
import org.apache.log4j.Logger;

import java.util.LinkedList;

public class NAIdParams extends NAKeyParams implements IdParams {
    private static final Logger logger =
            Logger.getLogger(NAIdParams.class);

    private Iterable<String> idsList;
    private Integer page;
    private Integer pageSize;

    public NAIdParams() {
        idsList = new LinkedList<>();
        page = 1;
        pageSize = 100;
    }

    @Override
    public void setIdsList(Iterable<String> idsList) {
        if(idsList == null) {
            logger.warn("Ids list parameter has null value");

            throw new IllegalArgumentException(
                    "Ids list parameter has null value"
            );
        }
        this.idsList = idsList;
    }

    @Override
    public void setPage(int page) {
        if(page <= 0) {
            logger.error("Page parameter has non-positive value");

            throw new IllegalArgumentException(
                    "Page parameter has non-positive value"
            );
        }
        this.page = page;
    }

    @Override
    public void setPageSize(int pageSize) {
        if(pageSize <= 0) {
            logger.error("Page size parameter has non-positive value");

            throw new IllegalArgumentException(
                    "Page size parameter has non-positive value"
            );
        }
        this.pageSize = pageSize;
    }

    @Override
    public Integer getPageSize() {
        return pageSize;
    }

    @Override
    public String getUrnString() {
        StringBuilder paramsLine = new
                StringBuilder("/everything");
        boolean firstId = true;

        for(String id : idsList) {
            if(firstId) {
                paramsLine.append("?sources=").append(id);
                firstId = false;
            } else {
                paramsLine.append(",").append(id);
            }
        }

        if(pageSize != null) {
            if(firstId) {
                paramsLine.append("?pageSize=").append(pageSize);
                firstId = false;
            } else {
                paramsLine.append("&pageSize=").append(pageSize);
            }
        }

        if(page != null) {
            if(firstId) {
                paramsLine.append("?page=").append(page);
                firstId = false;
            } else {
                paramsLine.append("&page=").append(page);
            }
        }

        if(apiKey != null) {
            if(firstId) {
                paramsLine.append("?apiKey=").append(apiKey);
            } else {
                paramsLine.append("&apiKey=").append(apiKey);
            }
        }

        return paramsLine.toString();
    }
}
