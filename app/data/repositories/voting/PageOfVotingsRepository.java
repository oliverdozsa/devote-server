package data.repositories.voting;

import data.entities.voting.JpaVoting;
import responses.Page;

public interface PageOfVotingsRepository {
    Page<JpaVoting> pageOfPublic(int offset, int limit);
    Page<JpaVoting> votingsOfVoteCaller(int offset, int limit, String userId);
    Page<JpaVoting> votingsOfVoter(int offset, int limit, String userId);
    Page<JpaVoting> votingsOfVoterFilteredByNotTriedToCastVote(int offset, int limit, String userId);
}
