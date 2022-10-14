package requests;

import play.data.validation.Constraints;

public class CreatePollOptionRequest {
    @Constraints.Required
    @Constraints.MinLength(2)
    @Constraints.MaxLength(1000)
    private String name;

    @Constraints.Required
    @Constraints.Min(1)
    @Constraints.Max(99)
    private Integer code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "PollOptionDto{" +
                "name='" + name + '\'' +
                ", code=" + code +
                '}';
    }
}
