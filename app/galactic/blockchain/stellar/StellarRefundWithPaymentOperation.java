package galactic.blockchain.stellar;

import galactic.blockchain.api.Account;
import org.stellar.sdk.*;
import org.stellar.sdk.responses.AccountResponse;
import play.Logger;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import static utils.StringUtils.redactWithEllipsis;

class StellarRefundWithPaymentOperation {
    private static final Logger.ALogger logger = Logger.of(StellarRefundWithPaymentOperation.class);

    private final StellarServerAndNetwork serverAndNetwork;

    public StellarRefundWithPaymentOperation(StellarServerAndNetwork serverAndNetwork) {
        this.serverAndNetwork = serverAndNetwork;
    }

    public void refund(galactic.blockchain.api.Account destination, List<galactic.blockchain.api.Account> accountsToRefund) throws AccountRequiresMemoException, IOException {
        Server server = serverAndNetwork.getServer();
        StringJoiner accountsToRefundJoiner = new StringJoiner(", ");
        accountsToRefund.forEach(a -> accountsToRefundJoiner.add(redactWithEllipsis(a.publik, 5)));

        String destinationLoggable = redactWithEllipsis(destination.publik, 5);
        logger.info("[STELLAR]: Refunding accounts with payment: {}, to destination: {}", accountsToRefund, destinationLoggable);

        galactic.blockchain.api.Account txExecutorAccount = accountsToRefund.get(0);
        Transaction.Builder transactionBuilder = prepareTransaction(StellarUtils.fromAccount(txExecutorAccount));
        List<KeyPair> signers = new ArrayList<>();

        KeyPair stellarDestinationKeyPair = StellarUtils.fromAccount(destination);
        for (Account accountToRefund : accountsToRefund) {
            AccountResponse source = server.accounts().account(accountToRefund.publik);

            PaymentParams paymentParams = new PaymentParams();
            paymentParams.destination = stellarDestinationKeyPair;
            paymentParams.signers = signers;
            paymentParams.txBuilder = transactionBuilder;
            paymentParams.sourceResponse = source;
            paymentParams.source = StellarUtils.fromAccount(accountToRefund);

            payIfBalanceIsAppropriate(paymentParams);
        }

        submitTransaction(transactionBuilder, signers);
    }

    private void payIfBalanceIsAppropriate(PaymentParams params) {
        BigDecimal xlmBalance = StellarUtils.findXlmBalance(params.sourceResponse.getBalances());
        String loggableSource = redactWithEllipsis(params.sourceResponse.getAccountId(), 5);

        logger.info("[STELLAR]: Refund source balance: {}: {}", loggableSource, xlmBalance);

        if (xlmBalance.compareTo(BigDecimal.valueOf(2)) > 0) {
            BigDecimal amountToPay = xlmBalance.subtract(BigDecimal.valueOf(2));

            String loggableDestination = redactWithEllipsis(params.destination.getAccountId(), 5);
            logger.info("[STELLAR]: Refunding {} XLMs from {} to {}", amountToPay, loggableSource, loggableDestination);

            PaymentOperation.Builder paymentOperationBuilder =
                    new PaymentOperation.Builder(params.destination.getAccountId(), new AssetTypeNative(), amountToPay.toString());
            paymentOperationBuilder.setSourceAccount(params.sourceResponse.getAccountId());

            params.txBuilder.addOperation(paymentOperationBuilder.build());
            params.signers.add(params.source);
        }
    }

    private void submitTransaction(Transaction.Builder txBuilder, List<KeyPair> signers) throws AccountRequiresMemoException, IOException {
        Transaction transaction = txBuilder.build();
        signers.forEach(transaction::sign);

        Server server = serverAndNetwork.getServer();
        StellarSubmitTransaction.submit("refund balances with payment", transaction, server);
    }

    private Transaction.Builder prepareTransaction(KeyPair destination) throws IOException {
        Server server = serverAndNetwork.getServer();
        Network network = serverAndNetwork.getNetwork();

        return StellarUtils.createTransactionBuilder(server, network, destination.getAccountId());
    }

    private static class PaymentParams {
        public KeyPair destination;
        public AccountResponse sourceResponse;
        public KeyPair source;
        public Transaction.Builder txBuilder;
        public List<KeyPair> signers;
    }
}
