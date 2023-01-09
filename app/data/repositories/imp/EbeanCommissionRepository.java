package data.repositories.imp;

import data.entities.JpaChannelGeneratorAccount;
import data.entities.JpaCommissionSession;
import data.entities.JpaStoredTransaction;
import data.entities.JpaVoter;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.repositories.CommissionRepository;
import exceptions.InternalErrorException;
import exceptions.NotFoundException;
import io.ebean.EbeanServer;
import play.Logger;

import javax.inject.Inject;
import java.util.Optional;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;
import static utils.StringUtils.redact;
import static utils.StringUtils.redactWithEllipsis;

public class EbeanCommissionRepository implements CommissionRepository {
    private final EbeanServer ebeanServer;

    private static final Logger.ALogger logger = Logger.of(EbeanCommissionRepository.class);

    @Inject
    public EbeanCommissionRepository(EbeanServer ebeanServer) {
        this.ebeanServer = ebeanServer;
    }

    @Override
    public Optional<JpaCommissionSession> getByVotingIdAndUserId(Long votingId, String userId) {
        logger.info("getByVotingAndUserId(): votingId = {}, userId = {}", votingId, userId);

        JpaCommissionSession entity = find(userId, votingId);

        return Optional.ofNullable(entity);
    }

    @Override
    public JpaCommissionSession createSession(Long votingId, String userId) {
        logger.info("createSession(): votingId = {}, userId = {}", votingId, userId);

        assertEntityExists(ebeanServer, JpaVoting.class, votingId);

        JpaVoter voter = findVoterWith(userId);
        if (voter == null) {
            throw new NotFoundException("Not found user with id: " + userId);
        }

        JpaCommissionSession initSession = new JpaCommissionSession();
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);
        initSession.setVoting(voting);
        initSession.setVoter(voter);

        ebeanServer.save(initSession);
        return initSession;
    }

    @Override
    public Boolean hasAlreadySignedAnEnvelope(String userId, Long votingId) {
        logger.info("hasAlreadySignedAnEnvelope(): votingId = {}, userId = {}", votingId, userId);

        JpaCommissionSession commissionSession = find(userId, votingId);

        if(commissionSession == null) {
            throw new NotFoundException("Not found session for userId = " + userId + " and votingId = " + votingId);
        }

        boolean hasAlreadySigned = commissionSession.getEnvelopeSignature() != null;

        logger.info("hasAlreadySignedAnEnvelope(): User {} has {} already signed an envelope in voting {}",
                userId, hasAlreadySigned ? "" : "not", votingId);

        return hasAlreadySigned;
    }

    @Override
    public void storeEnvelopeSignature(String userId, Long votingId, String signature) {
        logger.info("storeEnvelopeSignature(): userId = {}, votingId = {}, signature = {}",
                userId, votingId, redactWithEllipsis(signature, 5));
        JpaCommissionSession commissionSession = find(userId, votingId);

        if (commissionSession == null) {
            throw new NotFoundException("Not found session for user: " + userId + " in voting: " + votingId);
        }

        commissionSession.setEnvelopeSignature(signature);
        ebeanServer.update(commissionSession);
    }

    @Override
    public JpaVotingChannelAccount consumeOneChannel(Long votingId) {
        logger.info("consumeOneChannel(): votingId = {}", votingId);
        Optional<JpaVotingChannelAccount> optionalJpaVotingChannelAccount = ebeanServer.createQuery(JpaVotingChannelAccount.class)
                .where()
                .eq("isConsumed", false)
                .eq("voting.id", votingId)
                .setMaxRows(1)
                .findOneOrEmpty();

        if (optionalJpaVotingChannelAccount.isPresent()) {
            JpaVotingChannelAccount channelAccount = optionalJpaVotingChannelAccount.get();
            channelAccount.setConsumed(true);
            ebeanServer.update(channelAccount);

            logger.info("consumeOneChannel(): successfully consumed a channel! id = {}", channelAccount.getId());

            return channelAccount;
        } else if (areAllChannelAccountsCreated(votingId)) {
            String errorMessage = "Could not find a free channel account!";
            logger.warn("consumeOneChannel(): {}", errorMessage);
            throw new InternalErrorException(errorMessage);
        } else {
            String errorMessage = "Could not find a free channel account! Please try again later!";
            logger.warn("consumeOneChannel(): {}", errorMessage);
            throw new InternalErrorException(errorMessage);
        }
    }

    @Override
    public void storeTransactionForRevealedSignature(Long votingId, String signature, String transaction) {
        String signatureToLog = redactWithEllipsis(signature, 5);
        String transactionToLog = redactWithEllipsis(signature, 5);
        logger.info("storeTransactionForRevealedSignature(): votingId = {}, signatureToLog = {}, transactionToLog = {}",
                votingId, signatureToLog, transactionToLog);

        JpaVoting voting = ebeanServer.getReference(JpaVoting.class, votingId);

        JpaStoredTransaction storedTransaction = new JpaStoredTransaction();
        storedTransaction.setSignature(signature);
        storedTransaction.setSignatureFootPrint(toSignatureFootPrint(signature));
        storedTransaction.setTransaction(transaction);
        storedTransaction.setVoting(voting);
        ebeanServer.save(storedTransaction);
    }

    @Override
    public boolean doesTransactionExistForSignature(String signature) {
        Optional<JpaStoredTransaction> optionalJpaStoredTransaction = findStoredTransaction(signature);
        logger.info("doesTransactionExistForSignature(): Transaction does {} exist for signature = {}",
                optionalJpaStoredTransaction.isPresent() ? "" : "not", redactWithEllipsis(signature, 5));
        return optionalJpaStoredTransaction.isPresent();
    }

    @Override
    public JpaStoredTransaction getTransaction(String signature) {
        String redactedSignature = redactWithEllipsis(signature, 5);
        logger.info("getTransaction(): signature = {}", redactedSignature);
        Optional<JpaStoredTransaction> optionalJpaStoredTransaction = findStoredTransaction(signature);

        if (!optionalJpaStoredTransaction.isPresent()) {
            throw new NotFoundException("Not found transaction for signature: " + redactedSignature);
        }

        return optionalJpaStoredTransaction.get();
    }

    @Override
    public JpaCommissionSession getCommissionSessionWithExistingEnvelopeSignature(Long votingId, String userId) {
        logger.info("getCommissionSessionWithExistingEnvelopeSignature(): votingId = {}, userId = {}", votingId, userId);

        JpaCommissionSession commissionSession = find(userId, votingId);

        if (commissionSession == null) {
            String errorMessage = String.format("Not found commission session with voting id = %d, user = %s", votingId, userId);
            logger.warn("getCommissionSession()" + errorMessage);
            throw new NotFoundException(errorMessage);
        } else if (commissionSession.getEnvelopeSignature() == null || commissionSession.getEnvelopeSignature().length() == 0) {
            String errorMessage = String.format("Found commission session with voting id = %d, user = %s, but envelope signature is empty.",
                    votingId, userId);
            logger.warn("getCommissionSession()" + errorMessage);
            throw new NotFoundException(errorMessage);
        }
        {
            return commissionSession;
        }
    }

    @Override
    public boolean isVotingInitializedProperly(Long votingId) {
        logger.info("isVotingInitializedProperly(): votingId = {}", votingId);

        assertEntityExists(ebeanServer, JpaVoting.class, votingId);
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);

        return voting.getDistributionAccountPublic() != null && voting.getDistributionAccountPublic().length() > 0 &&
                voting.getIssuerAccountPublic() != null && voting.getIssuerAccountPublic().length() > 0 &&
                voting.getBallotAccountPublic() != null && voting.getBallotAccountPublic().length() > 0;
    }

    private JpaCommissionSession find(String userId, Long votingId) {
        return ebeanServer.createQuery(JpaCommissionSession.class)
                .where()
                .eq("voter.voterIds.id", userId)
                .eq("voting.id", votingId)
                .findOne();
    }

    private boolean areAllChannelAccountsCreated(Long votingId) {
        assertEntityExists(ebeanServer, JpaVoting.class, votingId);
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);

        for (JpaChannelGeneratorAccount channelGenerator : voting.getChannelGeneratorAccounts()) {
            if (channelGenerator.getChannelAccountProgress().getNumOfAccountsLeftToCreate() > 0) {
                return false;
            }
        }

        return true;
    }

    private Optional<JpaStoredTransaction> findStoredTransaction(String signature) {
        return ebeanServer.createQuery(JpaStoredTransaction.class)
                .where()
                // Index on clob is not supported, so assume this condition helps to shrink the search space
                .eq("signatureFootPrint", toSignatureFootPrint(signature))
                .eq("signature", signature)
                .findOneOrEmpty();
    }

    private static String toSignatureFootPrint(String signature) {
        return redact(signature, 255);
    }

    private JpaVoter findVoterWith(String userId) {
        return ebeanServer.createQuery(JpaVoter.class)
                .where()
                .eq("voterIds.id", userId)
                .findOne();
    }
}
