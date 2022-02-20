package data.repositories;

import data.entities.JpaVoting;
import responses.Page;

public interface PageOfVotingsRepository {
    Page<JpaVoting> pageOfPublic(int offset, int limit);
    Page<JpaVoting> votingsOfVoteCaller(int offset, int limit, String userId);
    Page<JpaVoting> votingsOfVoter(int offset, int limit, String userId);
}
