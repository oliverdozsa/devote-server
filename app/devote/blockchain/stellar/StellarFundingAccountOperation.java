package devote.blockchain.stellar;

import devote.blockchain.api.Account;
import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.FundingAccountOperation;
import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import play.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;

import static devote.blockchain.stellar.StellarUtils.findXlmBalance;
import static utils.StringUtils.redactWithEllipsis;

public class StellarFundingAccountOperation implements FundingAccountOperation {
    private StellarBlockchainConfiguration configuration;
    private StellarServerAndNetwork serverAndNetwork;

    private static final Logger.ALogger logger = Logger.of(StellarFundingAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
        serverAndNetwork = StellarServerAndNetwork.create(this.configuration);
    }

    @Override
    public void useTestNet() {
        serverAndNetwork = StellarServerAndNetwork.createForTestNet(this.configuration);
    }

    @Override
    public boolean doesNotHaveEnoughBalanceForVotesCap(String accountPublic, long votesCap) {
        Server server = serverAndNetwork.getServer();

        try {
            String loggableAccount = redactWithEllipsis(accountPublic, 5);
            logger.info("[STELLAR]: Getting balance of funding account {}", loggableAccount);

            AccountResponse accountResponse = server.accounts().account(accountPublic);

            BigDecimal xlmBalance = findXlmBalance(accountResponse.getBalances());
            logger.info("[STELLAR]: Funding account balance: {} XLM", xlmBalance);

            long minRequiredBalance = getMinRequiredBalanceBasedOn(votesCap);
            logger.info("[STELLAR]: The minimum required balance for funding account is: {} XLM", minRequiredBalance);

            return xlmBalance.compareTo(new BigDecimal(minRequiredBalance)) < 0;
        } catch (IOException e) {
            String logMessage = "[STELLAR]: Failed to get info about funding account!";
            logger.warn(logMessage);
            throw new BlockchainException(logMessage, e);
        }
    }

    @Override
    public Account createAndFundInternalFrom(Account userGivenFunding) {
        KeyPair userGivenFundingKeyPair = StellarUtils.fromAccount(userGivenFunding);

        Server server = serverAndNetwork.getServer();

        try {
            AccountResponse accountResponse = server.accounts().account(userGivenFunding.publik);
            BigDecimal userGivenFundingBalance = StellarUtils.findXlmBalance(accountResponse.getBalances());

            KeyPair internalFundingKeyPair = KeyPair.random();
            String internalFundingStartingBalance = Long.toString(userGivenFundingBalance.longValue() - 10);

            logger.info("[STELLAR]: About to create internal funding account {} with balance {}",
                    redactWithEllipsis(internalFundingKeyPair.getAccountId(), 5),
                    internalFundingStartingBalance);

            CreateAccountOperation createAccount = new CreateAccountOperation.Builder(internalFundingKeyPair.getAccountId(), internalFundingStartingBalance)
                    .build();

            Transaction.Builder txBuilder = prepareTransaction(userGivenFundingKeyPair);
            txBuilder.addOperation(createAccount);

            submitTransaction(txBuilder, userGivenFundingKeyPair);

            return StellarUtils.toAccount(internalFundingKeyPair);
        } catch (Exception e) {
            String logMessage = "[STELLAR]: Failed to create internal funding account!";
            logger.warn(logMessage);
            throw new BlockchainException(logMessage, e);
        }
    }

    private long getMinRequiredBalanceBasedOn(long votesCap) {
        return 4 * votesCap + 10 * StellarChannelGeneratorAccountOperation.calcNumOfAccountsNeededBasedOn(configuration) + 60;
    }

    private Transaction.Builder prepareTransaction(KeyPair funding) throws IOException {
        Server server = serverAndNetwork.getServer();
        Network network = serverAndNetwork.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, funding.getAccountId());
    }

    private void submitTransaction(Transaction.Builder txBuilder, KeyPair... signers) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        Arrays.stream(signers).forEach(transaction::sign);

        Server server = serverAndNetwork.getServer();
        StellarSubmitTransaction.submit("internal funding", transaction, server);
    }
}
