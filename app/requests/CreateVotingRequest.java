package requests;

import com.typesafe.config.Config;
import play.data.validation.Constraints;
import utils.StringUtils;
import validation.ValidatableWithConfig;
import validation.ValidateWithConfig;

import javax.validation.Valid;
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

    @Constraints.Required
    @Valid
    private List<CreatePollRequest> polls;

    @Constraints.Required
    private Visibility visibility;

    @Constraints.Required
    private String fundingAccountPublic;

    @Constraints.Required
    private String fundingAccountSecret;

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

    @Override
    public String validate(Config config) {
        Integer minTimeInterval = config.getInt("devote.vote.related.min.time.interval.sec");

        if (isEncryptedNotValid(minTimeInterval)) {
            return "If encryption is needed, encrypted until must be at least " +
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

        return false;
    }

    private boolean isStartEndDateNotValid(Integer minTimeIntervalSecs) {
        if (startDate == null || endDate == null) {
            return true;
        }

        long gapMillis = Duration.between(startDate, endDate).toMillis();
        long minTimeIntervalMillis = minTimeIntervalSecs * 1000;
        return gapMillis < minTimeIntervalMillis;
    }

    public List<CreatePollRequest> getPolls() {
        return polls;
    }

    public void setPolls(List<CreatePollRequest> polls) {
        this.polls = polls;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public String getFundingAccountPublic() {
        return fundingAccountPublic;
    }

    public void setFundingAccountPublic(String fundingAccountPublic) {
        this.fundingAccountPublic = fundingAccountPublic;
    }

    public String getFundingAccountSecret() {
        return fundingAccountSecret;
    }

    public void setFundingAccountSecret(String fundingAccountSecret) {
        this.fundingAccountSecret = fundingAccountSecret;
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
                ", polls=" + polls +
                ", visibility=" + visibility +
                ", fundingAccountPublic='" + StringUtils.redactWithEllipsis(fundingAccountPublic, 5) + '\'' +
                ", fundingAccountSecret='***'" +
                '}';
    }

    public enum Authorization {
        EMAILS
    }

    public enum Visibility {
        PUBLIC,
        UNLISTED,
        PRIVATE
    }
}
