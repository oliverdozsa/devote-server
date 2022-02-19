package requests;

import play.data.validation.Constraints;

public class PageOfVotingsRequest {
    @Constraints.Min(0)
    private Integer offset;

    @Constraints.Max(50)
    @Constraints.Min(0)
    private Integer limit;
}
