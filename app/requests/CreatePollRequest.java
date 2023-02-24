package requests;

import play.data.validation.Constraints;

import javax.validation.Valid;
import javax.validation.constraints.Size;
import java.util.List;
import java.util.stream.Collectors;

@Constraints.Validate
public class CreatePollRequest implements Constraints.Validatable<String> {
    @Constraints.Required
    @Constraints.MinLength(2)
    @Constraints.MaxLength(1000)
    private String question;

    @Constraints.MaxLength(1000)
    private String description;

    @Constraints.Required
    @Valid
    @Size(min = 2, max = 99)
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String validate() {
        if(areOptionsEmpty()) {
            return "Options are empty!";
        }

        if (areOptionCodesNotUnique()) {
            return "Option codes are not unique!";
        }

        if (areOptionNamesNotUnique()) {
            return "Option names are not unique!";
        }

        return null;
    }

    @Override
    public String toString() {
        return "CreatePollRequest{" +
                "question='" + question + '\'' +
                ", description='" + description + '\'' +
                ", options=" + options +
                '}';
    }

    private boolean areOptionsEmpty() {
        if (options == null || options.isEmpty()) {
            return true;
        }

        return false;
    }

    private boolean areOptionCodesNotUnique() {
        List<Integer> uniqueCodes = options.stream()
                .map(CreatePollOptionRequest::getCode)
                .distinct()
                .collect(Collectors.toList());

        return uniqueCodes.size() != options.size();
    }

    private boolean areOptionNamesNotUnique() {
        List<String> uniqueNames = options.stream()
                .map(CreatePollOptionRequest::getName)
                .distinct()
                .collect(Collectors.toList());

        return uniqueNames.size() != options.size();
    }
}
