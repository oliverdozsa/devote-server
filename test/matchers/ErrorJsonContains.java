package matchers;

import com.fasterxml.jackson.databind.JsonNode;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import play.libs.Json;
import play.mvc.Result;

import static play.test.Helpers.contentAsString;

public class ErrorJsonContains extends TypeSafeMatcher<Result> {
    private final String field;
    private final String expectedErrorSubErrorMessage;
    private String actualErrorMessage;

    private ErrorJsonContains(String field, String expected) {
        this.field = field;
        this.expectedErrorSubErrorMessage = expected;
    }

    private ErrorJsonContains(String subString) {
        this(null, subString);
    }

    @Override
    protected boolean matchesSafely(Result item) {
        String jsonStr = contentAsString(item);
        JsonNode json = Json.parse(jsonStr);
        if (isGlobal()) {
            actualErrorMessage = json.get("").get(0).asText();
        } else {
            actualErrorMessage = json.get(field).get(0).asText();
        }
        return actualErrorMessage.contains(expectedErrorSubErrorMessage);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("error message to contain \"" + expectedErrorSubErrorMessage + "\" for field \"" + field + "\"");
    }

    @Override
    protected void describeMismatchSafely(Result item, Description mismatchDescription) {
        mismatchDescription.appendText("error message was ").appendValue(actualErrorMessage);
    }

    private boolean isGlobal() {
        return field == null;
    }

    public static ErrorJsonContains containsErrorForField(String field, String errorMessagePart) {
        return new ErrorJsonContains(field, errorMessagePart);
    }

    public static ErrorJsonContains containsGlobalError(String errorMessagePart) {
        return new ErrorJsonContains(errorMessagePart);
    }
}
