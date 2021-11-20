package dto;

import com.typesafe.config.Config;
import play.data.validation.Constraints;
import validation.ValidatableWithConfig;
import validation.ValidateWithConfig;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@ValidateWithConfig
public class CreateVotingRequest implements ValidatableWithConfig<String> {
    @Constraints.Required
    private String network;

    @Constraints.Required
    @Constraints.Min(2)
    private Long votesCap;

    @Constraints.Required
    @Constraints.MinLength(2)
    private String title;

    @Constraints.Pattern("[0-9a-z]+")
    @Constraints.MaxLength(8)
    @Constraints.MinLength(2)
    private String tokenIdentifier;

    private Instant encryptedUntil;

    @Constraints.Required
    private Instant startDate;

    @Constraints.Required
    private Instant endDate;

    private Authorization authorization;

    private List<@Constraints.Email String> authorizationEmailOptions;

    @Constraints.MinLength(2)
    private String authorizationKeybaseOptions;

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Long getVotesCap() {
        return votesCap;
    }

    public void setVotesCap(Long votesCap) {
        this.votesCap = votesCap;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTokenIdentifier() {
        return tokenIdentifier;
    }

    public void setTokenIdentifier(String tokenIdentifier) {
        this.tokenIdentifier = tokenIdentifier;
    }

    public Instant getEncryptedUntil() {
        return encryptedUntil;
    }

    public void setEncryptedUntil(Instant encryptedUntil) {
        this.encryptedUntil = encryptedUntil;
    }

    public Instant getStartDate() {
        return startDate;
    }

    public void setStartDate(Instant startDate) {
        this.startDate = startDate;
    }

    public Instant getEndDate() {
        return endDate;
    }

    public void setEndDate(Instant endDate) {
        this.endDate = endDate;
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(Authorization authorization) {
        this.authorization = authorization;
    }

    public List<String> getAuthorizationEmailOptions() {
        return authorizationEmailOptions;
    }

    public void setAuthorizationEmailOptions(List<String> authorizationEmailOptions) {
        this.authorizationEmailOptions = authorizationEmailOptions;
    }

    public String getAuthorizationKeybaseOptions() {
        return authorizationKeybaseOptions;
    }

    public void setAuthorizationKeybaseOptions(String authorizationKeybaseOptions) {
        this.authorizationKeybaseOptions = authorizationKeybaseOptions;
    }

    @Override
    public String validate(Config config) {
        Integer minTimeInterval = config.getInt("devote.vote.related.min.time.interval.sec");

        if (isEncryptedNotValid(minTimeInterval)) {
            return "If encryption is turned on, encrypted until must be at least " +
                    minTimeInterval + " seconds from now!";
        }

        if (isAuthorizationNotValid()) {
            return "Invalid authorization! If EMAILS is given, check email addresses!";
        }

        if (isStartEndDateNotValid(minTimeInterval)) {
            return "The minimum difference between start and end date should be " +
                    minTimeInterval + " seconds, and voting must end in the future!";
        }

        return null;
    }

    private boolean isEncryptedNotValid(Integer minTimeIntervalSec) {
        if (encryptedUntil == null) {
            return false;
        }

        Instant now = Instant.now();
        long gapSecs = Duration.between(now, encryptedUntil).toMillis() / 1000;

        return gapSecs < minTimeIntervalSec;
    }

    private boolean isAuthorizationNotValid() {
        if (authorization == Authorization.EMAILS &&
                (authorizationEmailOptions == null || authorizationEmailOptions.isEmpty())) {
            return true;
        }

        return authorization == Authorization.KEYBASE && authorizationKeybaseOptions == null;
    }

    private boolean isStartEndDateNotValid(Integer minTimeIntervalSecs) {
        if (startDate == null || endDate == null) {
            return true;
        }

        long gapMillis = Duration.between(startDate, endDate).toMillis();
        long minTimeIntervalMillis = minTimeIntervalSecs * 1000;
        return gapMillis < minTimeIntervalMillis;
    }

    @Override
    public String toString() {
        return "CreateVotingRequest{" +
                "network='" + network + '\'' +
                ", votesCap=" + votesCap +
                ", title='" + title + '\'' +
                ", tokenIdentifier='" + tokenIdentifier + '\'' +
                ", encryptedUntil=" + encryptedUntil +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", authorization=" + authorization +
                ", authorizationEmailOptions=" + authorizationEmailOptions +
                ", authorizationKeybaseOptions='" + authorizationKeybaseOptions + '\'' +
                '}';
    }

    public enum Authorization {
        OPEN,
        EMAILS,
        DOMAIN,
        IP,
        COOKIE,
        CODE,
        KEYBASE
    }
}
