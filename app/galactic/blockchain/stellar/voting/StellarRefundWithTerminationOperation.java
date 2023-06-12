package galactic.blockchain.stellar.voting;

import galactic.blockchain.api.Account;
import galactic.blockchain.stellar.StellarServerAndNetwork;
import galactic.blockchain.stellar.StellarSubmitTransaction;
import galactic.blockchain.stellar.StellarUtils;
import org.stellar.sdk.*;
import org.stellar.sdk.requests.ErrorResponse;
import play.Logger;

import java.io.IOException;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static utils.StringUtils.redactWithEllipsis;

public class StellarRefundWithTerminationOperation {
    private static final Logger.ALogger logger = Logger.of(StellarRefundWithTerminationOperation.class);

    private final StellarServerAndNetwork serverAndNetwork;

    public StellarRefundWithTerminationOperation(StellarServerAndNetwork serverAndNetwork) {
        this.serverAndNetwork = serverAndNetwork;
    }

    public void refund(galactic.blockchain.api.Account destination, List<galactic.blockchain.api.Account> accountsToTerminate) throws IOException, AccountRequiresMemoException {
        List<galactic.blockchain.api.Account> filteredAccountsToTerminate = filterAlreadyMergedAccountsFrom(accountsToTerminate);

        String destinationLoggable = redactWithEllipsis(destination.publik, 5);
        StringJoiner accountsToTerminateLoggable = new StringJoiner(", ");
        filteredAccountsToTerminate.forEach(a -> accountsToTerminateLoggable.add(redactWithEllipsis(a.publik, 5)));

        if (filteredAccountsToTerminate.size() == 0) {
            logger.info("[STELLAR]: No accounts to merge.");
            return;
        }

        logger.info("[STELLAR]: Merging accounts: {}, to destination: {}", accountsToTerminateLoggable, destinationLoggable);

        galactic.blockchain.api.Account txExecutorAccount = filteredAccountsToTerminate.get(0);
        Transaction.Builder transactionBuilder = prepareTransaction(StellarUtils.fromAccount(txExecutorAccount));

        List<KeyPair> stellarKeyPairsToMerge = filteredAccountsToTerminate.stream()
                .map(StellarUtils::fromAccount)
                .collect(Collectors.toList());
        KeyPair stellarDestinationKeyPair = StellarUtils.fromAccount(destination);

        mergeAccounts(stellarKeyPairsToMerge, stellarDestinationKeyPair, transactionBuilder);

        submitTransaction(transactionBuilder, stellarKeyPairsToMerge);
    }

    private Transaction.Builder prepareTransaction(KeyPair destination) throws IOException {
        Server server = serverAndNetwork.getServer();
        Network network = serverAndNetwork.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, destination.getAccountId());
    }

    private void mergeAccounts(List<KeyPair> accountsToMerge, KeyPair destination, Transaction.Builder txBuilder) {
        accountsToMerge.forEach(a -> {
            AccountMergeOperation.Builder accountMergeOpBuilder = new AccountMergeOperation.Builder(destination.getAccountId())
                    .setSourceAccount(a.getAccountId());

            txBuilder.addOperation(accountMergeOpBuilder.build());
        });
    }

    private void submitTransaction(Transaction.Builder txBuilder, List<KeyPair> signers) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        signers.forEach(transaction::sign);

        Server server = serverAndNetwork.getServer();
        StellarSubmitTransaction.submit("refund balances with termination", transaction, server);
    }

    private List<galactic.blockchain.api.Account> filterAlreadyMergedAccountsFrom(List<galactic.blockchain.api.Account> accounts) {
        List<galactic.blockchain.api.Account> alreadyMergedAccounts = accounts.stream()
                .filter(a -> isAccountAlreadyMerged(a.publik))
                .collect(Collectors.toList());

        List<String> alreadyMergedAccountIds = alreadyMergedAccounts.stream()
                .map(a -> a.publik)
                .collect(Collectors.toList());

        List<Account> notYetMergedAccounts = accounts.stream()
                .filter(a -> !alreadyMergedAccountIds.contains(a.publik))
                .collect(Collectors.toList());

        if (alreadyMergedAccounts.size() > 0) {
            StringJoiner accountsAlreadyMergedJoiner = new StringJoiner(", ");
            alreadyMergedAccounts.forEach(a -> accountsAlreadyMergedJoiner.add(redactWithEllipsis(a.publik, 5)));
            logger.info("[STELLAR]: Accounts already merged: {}; they will be ignored", accountsAlreadyMergedJoiner);
        }

        return notYetMergedAccounts;
    }

    private boolean isAccountAlreadyMerged(String accountId) {
        Server server = serverAndNetwork.getServer();

        try {
            server.accounts().account(accountId);
        } catch (ErrorResponse e) {
            if (e.getCode() == 404) {
                return true;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return false;
    }
}
