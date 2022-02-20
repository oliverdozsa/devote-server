package requests;

import play.data.validation.Constraints;

public class PageVotingsRequest {
    @Constraints.Min(0)
    private Integer offset;

    @Constraints.Max(50)
    @Constraints.Min(5)
    private Integer limit;

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return "PageOfVotingsRequest{" +
                "offset=" + offset +
                ", limit=" + limit +
                '}';
    }
}
