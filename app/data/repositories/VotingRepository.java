package data.repositories;

import data.entities.JpaVoting;
import dto.CreateVotingRequest;

import java.util.List;

public interface VotingRepository {
    Long initalize(CreateVotingRequest request);
    JpaVoting single(Long id);
    void issuerAccountsCreated(Long id, List<String> accounts);
}
