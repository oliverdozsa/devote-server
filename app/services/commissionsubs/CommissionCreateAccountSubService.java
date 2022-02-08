package services.commissionsubs;

import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.entities.JpaVotingIssuerAccount;
import data.operations.CommissionDbOperations;
import data.operations.VotingDbOperations;
import devote.blockchain.api.Issuer;
import devote.blockchain.api.Account;
import devote.blockchain.api.VoterAccountOperation;
import devote.blockchain.operations.CommissionBlockchainOperations;
import exceptions.ForbiddenException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.RSAEngine;
import play.Logger;
import requests.CommissionAccountCreationRequest;
import responses.CommissionAccountCreationResponse;
import services.Base62Conversions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;

public class CommissionCreateAccountSubService {
    private final CommissionDbOperations commissionDbOperations;
    private final VotingDbOperations votingDbOperations;
    private final CommissionBlockchainOperations commissionBlockchainOperations;
    private final AsymmetricCipherKeyPair envelopeKeyPair;

    public CommissionCreateAccountSubService(
            CommissionDbOperations commissionDbOperations,
            VotingDbOperations votingDbOperations,
            CommissionBlockchainOperations commissionBlockchainOperations,
            AsymmetricCipherKeyPair envelopeKeyPair
    ) {
        this.commissionDbOperations = commissionDbOperations;
        this.votingDbOperations = votingDbOperations;
        this.commissionBlockchainOperations = commissionBlockchainOperations;
        this.envelopeKeyPair = envelopeKeyPair;
    }

    private static final Logger.ALogger logger = Logger.of(CommissionCreateAccountSubService.class);

    public CompletionStage<CommissionAccountCreationResponse> createAccount(CommissionAccountCreationRequest request) {
        logger.info("createAccount(): request = {}", request);
        ParsedMessage parsedMessage = new ParsedMessage(request.getMessage());

        Long votingId = Base62Conversions.decode(parsedMessage.votingId);
        AccountCreationCollectedData accountCreationData = new AccountCreationCollectedData();
        accountCreationData.voterPublic = parsedMessage.voterPublic;

        return verifySignatureOfRequest(request)
                .thenCompose(v -> checkIfAlreadyRequestedAccount(request))
                .thenCompose(v -> consumeChannel(votingId, accountCreationData))
                .thenCompose(v -> retrieveVoting(votingId, accountCreationData))
                .thenCompose(v -> selectAnIssuer(votingId, accountCreationData))
                .thenApply(v -> prepareForBlockchainOperation(accountCreationData))
                .thenCompose(c -> commissionBlockchainOperations.createTransaction(accountCreationData.voting.getNetwork(), c))
                .thenCompose(tx -> storeTransaction(accountCreationData.voting.getId(), request.getRevealedSignatureBase64(), tx))
                .thenApply(CommissionCreateAccountSubService::toResponse);
    }

    private CompletionStage<Void> verifySignatureOfRequest(CommissionAccountCreationRequest request) {
        return runAsync(() -> {
            RSAEngine rsaEngine = new RSAEngine();
            rsaEngine.init(false, envelopeKeyPair.getPublic());

            byte[] revealedSignatureBytes = Base64.getDecoder().decode(request.getRevealedSignatureBase64());
            byte[] revealedMessageBytes = request.getMessage().getBytes();

            byte[] signatureDecrypted = rsaEngine.processBlock(revealedSignatureBytes, 0, revealedSignatureBytes.length);
            try {
                byte[] messageHashed = MessageDigest.getInstance("SHA-256").digest(revealedMessageBytes);
                if(Arrays.equals(messageHashed, signatureDecrypted)) {
                    logger.info("verifySignatureOfRequest(): signature is valid for request: {}.", request.toString());
                } else {
                    logger.warn("verifySignatureOfRequest(): Signature for message is not valid!");
                    throw new ForbiddenException("Signature for message is not valid!");
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletionStage<Void> checkIfAlreadyRequestedAccount(CommissionAccountCreationRequest request) {
        return commissionDbOperations.doesTransactionExistForSignature(request.getRevealedSignatureBase64())
                .thenAccept(doesExist -> {
                    if(doesExist) {
                        throw new ForbiddenException("Account was requested before!");
                    }
                });
    }

    private CompletionStage<Void> consumeChannel(Long votingId, AccountCreationCollectedData collectedData) {
        return commissionDbOperations.consumeOneChannel(votingId)
                .thenAccept(c -> collectedData.channelAccount = c);
    }

    private CompletionStage<Void> retrieveVoting(Long votingId, AccountCreationCollectedData collectedData) {
        return votingDbOperations.single(votingId)
                .thenAccept(v -> collectedData.voting = v);
    }

    private CompletionStage<Void> selectAnIssuer(Long votingId, AccountCreationCollectedData collectedData) {
        return commissionDbOperations.selectAnIssuer(votingId)
                .thenAccept(i -> collectedData.issuer = i);
    }

    private VoterAccountOperation.CreateTransactionParams prepareForBlockchainOperation(AccountCreationCollectedData accountCreationData) {
        VoterAccountOperation.CreateTransactionParams params = new VoterAccountOperation.CreateTransactionParams();
        params.issuer = toIssuer(accountCreationData.issuer);
        params.channel = new Account(
                accountCreationData.channelAccount.getAccountSecret(), accountCreationData.channelAccount.getAccountPublic()
        );
        params.voterAccountPublic = accountCreationData.voterPublic;
        params.distribution = new Account(
                accountCreationData.voting.getDistributionAccountSecret(), accountCreationData.voting.getDistributionAccountPublic()
        );

        return params;
    }

    private CompletionStage<String> storeTransaction(Long votingId, String signature, String transaction) {
        return commissionDbOperations.storeTransaction(votingId, signature, transaction)
                .thenApply(v -> transaction);
    }

    private static CommissionAccountCreationResponse toResponse(String transaction) {
        CommissionAccountCreationResponse response = new CommissionAccountCreationResponse();
        response.setTransaction(transaction);

        return response;
    }

    private static Issuer toIssuer(JpaVotingIssuerAccount jpaVotingIssuer) {
        Account account = new Account(jpaVotingIssuer.getAccountSecret(), jpaVotingIssuer.getAccountPublic());
        return new Issuer(account, jpaVotingIssuer.getVotesCap(), jpaVotingIssuer.getAssetCode());
    }

    private static class ParsedMessage {
        public final String votingId;
        public final String voterPublic;

        public ParsedMessage(String rawMessage) {
            String[] parts = rawMessage.split("\\|");
            votingId = parts[0];
            voterPublic = parts[1];
        }
    }

    private static class AccountCreationCollectedData {
        public JpaVotingChannelAccount channelAccount;
        public JpaVoting voting;
        public JpaVotingIssuerAccount issuer;
        public String voterPublic;
    }
}
