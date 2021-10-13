package devote.blockchain.api;

public class BlockchainException extends RuntimeException {
    public BlockchainException(String message, Throwable cause) {
        super(message, cause);
    }
}
