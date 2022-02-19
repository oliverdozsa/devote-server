package services;

import play.Logger;
import requests.PageOfVotingsRequest;
import responses.Page;
import responses.PageOfVotingResponse;
import security.VerifiedJwt;

import java.util.concurrent.CompletionStage;

public class VotingsPagingService {
    private static final Logger.ALogger logger = Logger.of(VotingsPagingService.class);

    public CompletionStage<Page<PageOfVotingResponse>> publicVotings(PageOfVotingsRequest request) {
        // TODO
        return null;
    }

    public CompletionStage<Page<PageOfVotingResponse>> votingsOfVoteCaller(PageOfVotingsRequest request, VerifiedJwt jwt) {
        // TODO
        return null;
    }

    public CompletionStage<Page<PageOfVotingResponse>> votingsOfVoter(PageOfVotingsRequest request, VerifiedJwt jwt) {
        // TODO
        return null;
    }
}
