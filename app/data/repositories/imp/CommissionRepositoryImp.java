package data.repositories.imp;

import data.entities.JpaCommissionInitSession;
import data.repositories.CommissionRepository;

import java.util.Optional;

public class CommissionRepositoryImp implements CommissionRepository {
    @Override
    public Optional<JpaCommissionInitSession> getByVotingAndUserId(Long votingId, String userId) {
        // TODO
        return null;
    }

    @Override
    public JpaCommissionInitSession create(Long votingId, String userId) {
        // TODO
        return null;
    }
}
