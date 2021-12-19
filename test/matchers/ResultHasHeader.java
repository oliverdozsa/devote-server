package matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.mvc.Result;

public class ResultHasHeader extends TypeSafeMatcher<Result> {
    private final String headerName;

    private ResultHasHeader(String headerName) {
        this.headerName = headerName;
    }

    @Override
    protected boolean matchesSafely(Result item) {
        return item.headers().get(headerName) != null;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("\"" + headerName + "\" header to exist");
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("\"" + headerName + "\" header is not present");
    }

    public static ResultHasHeader hasLocationHeader() {
        return new ResultHasHeader("Location");
    }
}
