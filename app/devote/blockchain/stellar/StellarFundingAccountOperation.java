package devote.blockchain.stellar;

import devote.blockchain.api.BlockchainConfiguration;
import devote.blockchain.api.BlockchainException;
import devote.blockchain.api.FundingAccountOperation;
import org.stellar.sdk.Server;
import org.stellar.sdk.responses.AccountResponse;
import play.Logger;

import java.io.IOException;
import java.math.BigDecimal;

import static utils.StringUtils.redactWithEllipsis;

public class StellarFundingAccountOperation implements FundingAccountOperation {
    private StellarBlockchainConfiguration configuration;

    private static final Logger.ALogger logger = Logger.of(StellarFundingAccountOperation.class);

    @Override
    public void init(BlockchainConfiguration configuration) {
        this.configuration = (StellarBlockchainConfiguration) configuration;
    }

    @Override
    public boolean doesAccountNotHaveEnoughBalanceForVotesCap(String accountPublic, long votesCap) {
        Server server = configuration.getServer();

        try {
            String loggableAccount = redactWithEllipsis(accountPublic, 5);
            logger.info("[STELLAR]: Getting balance of funding account {}", loggableAccount);

            AccountResponse accountResponse = server.accounts().account(accountPublic);

            BigDecimal xlmBalance = findXlmBalance(accountResponse.getBalances());
            logger.info("[STELLAR]: Funding account balance (XLM): {}", xlmBalance);

            long minRequiredBalance = 4 * votesCap;
            return xlmBalance.compareTo(new BigDecimal(minRequiredBalance)) < 0;
        } catch (IOException e) {
            String logMessage = "[STELLAR]: Failed to get info about funding account!";
            logger.warn(logMessage);
            throw new BlockchainException(logMessage, e);
        }
    }

    private BigDecimal findXlmBalance(AccountResponse.Balance[] balances) {
        AccountResponse.Balance xlm = null;
        for (AccountResponse.Balance balance : balances) {
            if (balance.getAssetType().equals("native")) {
                xlm = balance;
                break;
            }
        }

        if (xlm == null) {
            throw new BlockchainException("Could not find xlm balance!");
        }

        return new BigDecimal(xlm.getBalance());
    }
}
