package requests.voting;

import play.data.validation.Constraints;

public class PageVotingsRequest {
    @Constraints.Min(0)
    private Integer offset;

    @Constraints.Max(50)
    @Constraints.Min(5)
    private Integer limit;

    private Boolean filterByNotTriedToCastVote;

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

    public Boolean getFilterByNotTriedToCastVote() {
        return filterByNotTriedToCastVote;
    }

    public void setFilterByNotTriedToCastVote(Boolean filterByNotTriedToCastVote) {
        this.filterByNotTriedToCastVote = filterByNotTriedToCastVote;
    }

    @Override
    public String toString() {
        return "PageVotingsRequest{" +
                "offset=" + offset +
                ", limit=" + limit +
                ", filterByNotTriedToCastVote=" + filterByNotTriedToCastVote +
                '}';
    }
}
