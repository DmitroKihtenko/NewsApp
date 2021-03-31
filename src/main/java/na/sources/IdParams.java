package na.sources;

public interface IdParams extends UrnParams {
    void setIdsList(Iterable<String> idsList);
    void setPage(int page);
    void setPageSize(int pageSize);

    Integer getPageSize();

    @Override
    IdParams clone();
}
