package services;

import play.Logger;
import requests.CastVoteInitRequest;
import responses.CastVoteInitResponse;

import java.util.concurrent.CompletionStage;

public class CastVoteService {
    private static final Logger.ALogger logger = Logger.of(CastVoteService.class);

    public CompletionStage<CastVoteInitResponse> init(CastVoteInitRequest request) {
        logger.info("init(): request = {}", request.toString());
        // TODO: Get voting by id
        // TODO: Determine user id, and whether user is authorized to vote
        // TODO: Create session
        // TODO: Create session token
        return null;
    }
}
