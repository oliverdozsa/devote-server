package executioncontexts;

import akka.actor.ActorSystem;
import play.libs.concurrent.CustomExecutionContext;

import javax.inject.Inject;

public class BlockchainExecutionContext extends CustomExecutionContext {
    @Inject
    public BlockchainExecutionContext(ActorSystem actorSystem) {
        super(actorSystem, "blockchain.dispatcher");
    }
}
