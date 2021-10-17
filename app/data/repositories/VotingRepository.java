package data.repositories;

import data.entities.JpaVoting;
import dto.CreateVotingRequest;

public interface VotingRepository {
    Long initalize(CreateVotingRequest request);
    JpaVoting single(Long id);
}
