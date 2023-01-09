package data.repositories;

public interface VoterRepository {
    void userAuthenticated(String email, String userId);
    boolean doesParticipateInVoting(String userId, Long votingId);
}
