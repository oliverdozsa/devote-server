package units.requests.voting;

import org.junit.Test;
import requests.voting.CreatePollOptionRequest;
import requests.voting.CreatePollRequest;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class CreatePollRequestTest {
    @Test
    public void testOptionCodesEmpty() {
        // Given
        CreatePollRequest pollRequest = new CreatePollRequest();
        pollRequest.setQuestion("Favourite color?");
        pollRequest.setOptions(new ArrayList<>());

        // When
        String errorMessage = pollRequest.validate();

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("Options are empty!"));
    }

    @Test
    public void testOptionsNotUnique() {
        // Given
        CreatePollRequest pollRequest = new CreatePollRequest();
        pollRequest.setQuestion("Favourite color?");

        List<CreatePollOptionRequest> pollOptions =
                createPollOptions(
                        new Integer[]{1, 2, 1},
                        new String[]{"red", "blue", "green"}
                );

        pollRequest.setOptions(pollOptions);

        // When
        String errorMessage = pollRequest.validate();

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("Option codes are not unique!"));
    }

    @Test
    public void testOptionNamesNotUnique() {
        // Given
        CreatePollRequest pollRequest = new CreatePollRequest();
        pollRequest.setQuestion("Favourite color?");

        List<CreatePollOptionRequest> pollOptions =
                createPollOptions(
                        new Integer[]{1, 2, 3},
                        new String[]{"red", "blue", "red"}
                );

        pollRequest.setOptions(pollOptions);

        // When
        String errorMessage = pollRequest.validate();

        // Then
        assertThat(errorMessage, notNullValue());
        assertThat(errorMessage, containsString("Option names are not unique!"));
    }

    private static List<CreatePollOptionRequest> createPollOptions(Integer[] codes, String names[]) {
        List<CreatePollOptionRequest> pollOptions = new ArrayList<>();

        for (int i = 0; i < codes.length; i++) {
            pollOptions.add(createPollOption(codes[i], names[i]));
        }

        return pollOptions;
    }

    private static CreatePollOptionRequest createPollOption(Integer code, String name) {
        CreatePollOptionRequest pollOption = new CreatePollOptionRequest();
        pollOption.setCode(code);
        pollOption.setName(name);

        return pollOption;
    }
}
