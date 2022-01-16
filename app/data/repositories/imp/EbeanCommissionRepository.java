package data.repositories.imp;

import data.entities.JpaCommissionSession;
import data.entities.JpaStoredTransaction;
import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.repositories.CommissionRepository;
import exceptions.InternalErrorException;
import exceptions.NotFoundException;
import io.ebean.Ebean;
import io.ebean.EbeanServer;
import play.Logger;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static data.repositories.imp.EbeanRepositoryUtils.assertEntityExists;
import static utils.StringUtils.redact;
import static utils.StringUtils.redactWithEllipsis;

public class EbeanCommissionRepository implements CommissionRepository {
    private final EbeanServer ebeanServer;

    private static final Logger.ALogger logger = Logger.of(EbeanCommissionRepository.class);

    @Inject
    public EbeanCommissionRepository(EbeanConfig ebeanConfig) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    @Override
    public Optional<JpaCommissionSession> getByVotingIdAndUserId(Long votingId, String userId) {
        logger.info("getByVotingAndUserId(): votingId = {}, userId = {}", votingId, userId);

        JpaCommissionSession entity = ebeanServer.createQuery(JpaCommissionSession.class)
                .where()
                .eq("voting.id", votingId)
                .eq("userId", userId)
                .findOne();

        return Optional.ofNullable(entity);
    }

    @Override
    public JpaCommissionSession createSession(Long votingId, String userId) {
        logger.info("createSession(): votingId = {}, userId = {}", votingId, userId);

        assertEntityExists(ebeanServer, JpaVoting.class, votingId);

        JpaCommissionSession initSession = new JpaCommissionSession();
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);
        initSession.setVoting(voting);
        initSession.setUserId(userId);

        ebeanServer.save(initSession);
        return initSession;
    }

    @Override
    public Boolean hasAlreadySignedAnEnvelope(String userId, Long votingId) {
        JpaCommissionSession commissionSession = find(userId, votingId);
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

        commissionSession.setEnvelopeSignature(signature);
        ebeanServer.update(commissionSession);
    }

    @Override
    public JpaVotingChannelAccount consumeOneChannel(Long votingId) {
        logger.info("consumeOneChannel(): votingId = {}", votingId);
        Optional<JpaVotingChannelAccount> optionalJpaVotingChannelAccount = ebeanServer.createQuery(JpaVotingChannelAccount.class)
                .where()
                .eq("isConsumed", false)
                .setMaxRows(1)
                .findOneOrEmpty();

        if (optionalJpaVotingChannelAccount.isPresent()) {
            JpaVotingChannelAccount channelAccount = optionalJpaVotingChannelAccount.get();
            channelAccount.setConsumed(true);
            ebeanServer.update(channelAccount);

            logger.info("consumeOneChannel(): successfully consumed a channel! id = {}", channelAccount.getId());

            return channelAccount;
        } else if (areAllChannelAccountsCreated(votingId)) {
            logger.warn("Could not find a free channel account!");
            throw new InternalErrorException("Could not find a free channel account!");
        } else {
            logger.warn("Could not find a free channel account! Please try again later!");
            throw new InternalErrorException("Could not find a free channel account! Please try again later!");
        }
    }

    @Override
    public JpaVotingIssuerAccount selectAnIssuer(Long votingId) {
        logger.info("selectAnIssuer(): votingId = {}", votingId);

        assertEntityExists(ebeanServer, JpaVoting.class, votingId);
        List<JpaVotingIssuerAccount> issuers = ebeanServer.createQuery(JpaVotingIssuerAccount.class)
                .where()
                .eq("voting.id", votingId)
                .findList();

        if (issuers == null || issuers.size() == 0) {
            throw new InternalErrorException("Not found any issuer account for voting: " + votingId);
        }

        int randomIssuerIndex = ThreadLocalRandom.current().nextInt(issuers.size());
        JpaVotingIssuerAccount randomIssuer = issuers.get(randomIssuerIndex);

        logger.info("selectAnIssuer(): selected issuer: {}", randomIssuer.getId());
        return issuers.get(randomIssuerIndex);
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

        if(!optionalJpaStoredTransaction.isPresent()) {
            throw new NotFoundException("Not found transaction for signature: " + redactedSignature);
        }

        return optionalJpaStoredTransaction.get();
    }

    private JpaCommissionSession find(String userId, Long votingId) {
        return ebeanServer.createQuery(JpaCommissionSession.class)
                .where()
                .eq("userId", userId)
                .eq("voting.id", votingId)
                .findOne();
    }

    private boolean areAllChannelAccountsCreated(Long votingId) {
        assertEntityExists(ebeanServer, JpaVoting.class, votingId);
        JpaVoting voting = ebeanServer.find(JpaVoting.class, votingId);

        for (JpaVotingIssuerAccount issuer : voting.getIssuerAccounts()) {
            if (issuer.getChannelAccountProgress().getNumOfAccountsLeftToCreate() > 0) {
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
}
