package requests;

import play.data.validation.Constraints;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

public class CreatePollRequest implements Constraints.Validatable<String> {
    @Constraints.Required
    @Constraints.MinLength(2)
    private String question;

    @Constraints.Required
    @Valid
    @Size(min = 2)
    private List<CreatePollOptionRequest> options;

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<CreatePollOptionRequest> getOptions() {
        return options;
    }

    public void setOptions(List<CreatePollOptionRequest> options) {
        this.options = options;
    }

    @Override
    public String validate() {
        if (areOptionCodesNotUnique()) {
            return "Option code are not unique!";
        }

        if (areOptionNamesNotUnique()) {
            return "Option names are not unique!";
        }

        return null;
    }

    @Override
    public String toString() {
        return "PollDto{" +
                "question='" + question + '\'' +
                ", options=" + options +
                '}';
    }

    private boolean areOptionCodesNotUnique() {
        if (options == null || options.isEmpty()) {
            return true;
        }

        List<Integer> uniqueCodes = options.stream()
                .map(CreatePollOptionRequest::getCode)
                .distinct()
                .collect(Collectors.toList());

        return uniqueCodes.size() != options.size();
    }

    private boolean areOptionNamesNotUnique() {
        if (options == null || options.isEmpty()) {
            return true;
        }

        List<String> uniqueNames = options.stream()
                .map(CreatePollOptionRequest::getName)
                .distinct()
                .collect(Collectors.toList());

        return uniqueNames.size() != options.size();
    }
}
