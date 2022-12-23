package services.commissionsubs;

import data.entities.JpaVoting;
import data.entities.JpaVotingChannelAccount;
import data.operations.CommissionDbOperations;
import data.operations.VotingDbOperations;
import devote.blockchain.api.Account;
import devote.blockchain.api.VoterAccountOperation;
import devote.blockchain.operations.CommissionBlockchainOperations;
import exceptions.ForbiddenException;
import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.engines.RSAEngine;
import play.Logger;
import requests.CommissionCreateTransactionRequest;
import responses.CommissionAccountCreationResponse;
import services.Base62Conversions;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.runAsync;

public class CommissionCreateTransactionSubService {
    private final CommissionDbOperations commissionDbOperations;
    private final VotingDbOperations votingDbOperations;
    private final CommissionBlockchainOperations commissionBlockchainOperations;
    private final AsymmetricCipherKeyPair envelopeKeyPair;

    public CommissionCreateTransactionSubService(
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

    private static final Logger.ALogger logger = Logger.of(CommissionCreateTransactionSubService.class);

    public CompletionStage<CommissionAccountCreationResponse> createTransaction(CommissionCreateTransactionRequest request) {
        logger.info("createTransaction(): request = {}", request);
        ParsedMessage parsedMessage = new ParsedMessage(request.getMessage());

        Long votingId = Base62Conversions.decode(parsedMessage.votingId);
        AccountCreationCollectedData accountCreationData = new AccountCreationCollectedData();
        accountCreationData.voterPublic = parsedMessage.voterPublic;

        return verifySignatureOfRequest(request)
                .thenCompose(v -> checkIfAlreadyRequestedAccount(request))
                .thenCompose(v -> consumeChannel(votingId, accountCreationData))
                .thenCompose(v -> retrieveVoting(votingId, accountCreationData))
                .thenApply(v -> prepareForBlockchainOperation(accountCreationData))
                .thenCompose(c -> commissionBlockchainOperations.createTransaction(accountCreationData.voting.getNetwork(), c))
                .thenCompose(tx -> storeTransaction(accountCreationData.voting.getId(), request.getRevealedSignatureBase64(), tx))
                .thenApply(CommissionCreateTransactionSubService::toResponse);
    }

    private CompletionStage<Void> verifySignatureOfRequest(CommissionCreateTransactionRequest request) {
        return runAsync(() -> {
            RSAEngine rsaEngine = new RSAEngine();
            rsaEngine.init(false, envelopeKeyPair.getPublic());

            byte[] revealedSignatureBytes = Base64.getDecoder().decode(request.getRevealedSignatureBase64());
            byte[] revealedMessageBytes = request.getMessage().getBytes();

            byte[] signatureDecrypted = rsaEngine.processBlock(revealedSignatureBytes, 0, revealedSignatureBytes.length);

            try {
                byte[] messageHashed = MessageDigest.getInstance("SHA-256").digest(revealedMessageBytes);
                signatureDecrypted = checkIfLeadingZeroIsNeededInSignatureBytes(signatureDecrypted, revealedMessageBytes);

                if (Arrays.equals(messageHashed, signatureDecrypted)) {
                    logger.info("verifySignatureOfRequest(): signature is valid for request: {}.", request.toString());
                } else {
                    logger.warn("verifySignatureOfRequest(): Signature for message is not valid!");
                    logger.warn("messageHashed      = {}", Arrays.toString(messageHashed));
                    logger.warn("signatureDecrypted = {}", Arrays.toString(signatureDecrypted));
                    logger.warn("input sig b64      = {}", request.getRevealedSignatureBase64());
                    throw new ForbiddenException("Signature for message is not valid!");
                }
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private CompletionStage<Void> checkIfAlreadyRequestedAccount(CommissionCreateTransactionRequest request) {
        return commissionDbOperations.doesTransactionExistForSignature(request.getRevealedSignatureBase64())
                .thenAccept(doesExist -> {
                    if (doesExist) {
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

    private VoterAccountOperation.CreateTransactionParams prepareForBlockchainOperation(AccountCreationCollectedData accountCreationData) {
        VoterAccountOperation.CreateTransactionParams params = new VoterAccountOperation.CreateTransactionParams();
        params.issuerAccountPublic = accountCreationData.voting.getIssuerAccountPublic();
        params.assetCode = accountCreationData.voting.getAssetCode();
        params.votesCap = accountCreationData.voting.getVotesCap();
        params.channel = new Account(
                accountCreationData.channelAccount.getAccountSecret(), accountCreationData.channelAccount.getAccountPublic()
        );
        params.voterAccountPublic = accountCreationData.voterPublic;
        params.distribution = new Account(
                accountCreationData.voting.getDistributionAccountSecret(), accountCreationData.voting.getDistributionAccountPublic()
        );
        params.isOnTestNetwork = accountCreationData.voting.getOnTestNetwork() != null && accountCreationData.voting.getOnTestNetwork();

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

    private static byte[] checkIfLeadingZeroIsNeededInSignatureBytes(byte[] signatureBytes, byte[] messageHashBytes) {
        if (signatureBytes.length == messageHashBytes.length - 1) {
            byte[] signatureBytesWithLeadingZero = new byte[messageHashBytes.length];
            System.arraycopy(signatureBytes, 1, signatureBytesWithLeadingZero, 1, signatureBytes.length);
            return signatureBytesWithLeadingZero;
        }

        return signatureBytes;
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
        public String voterPublic;
    }
}
