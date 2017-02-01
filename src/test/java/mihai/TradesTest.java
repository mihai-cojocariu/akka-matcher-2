package mihai;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import mihai.actors.SupervisorActor;
import mihai.dto.CcpTrade;
import mihai.dto.Trade;
import mihai.messages.CcpTradesMessage;
import mihai.messages.GetCcpTradesMessage;
import mihai.messages.GetTradesMessage;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;
import mihai.messages.TradesMessage;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * Created by mcojocariu on 1/31/2017.
 */
public class TradesTest {
    static ActorSystem system;
    LoggingAdapter log = Logging.getLogger(system, this);

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        system.shutdown();
        system.awaitTermination(Duration.create("10 seconds"));
    }

    @Test
    public void canAddATrade() {
        new JavaTestKit(system) {{
            final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor1");

            final Trade trade = Trade.aTrade();
            supervisor.tell(new NewTradeMessage(trade), getTestActor());
            final Trade trade1 = Trade.aTrade();
            supervisor.tell(new NewTradeMessage(trade1), getTestActor());

            supervisor.tell(new GetTradesMessage(UUID.randomUUID().toString()), getTestActor());

            final TradesMessage tradesMessage = expectMsgClass(TradesMessage.class);

            assertEquals(true, tradesMessage.getTrades().contains(trade));
            assertEquals(true, tradesMessage.getTrades().contains(trade1));
            assertEquals(2, tradesMessage.getTrades().size());
        }};
    }

    @Test
    public void canAddACcpTrade() {
        new JavaTestKit(system) {{
            final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor2");

            final CcpTrade ccpTrade = CcpTrade.aCcpTrade();
            supervisor.tell(new NewCcpTradeMessage(ccpTrade), getTestActor());
            supervisor.tell(new GetCcpTradesMessage(UUID.randomUUID().toString()), getTestActor());

            final CcpTradesMessage ccpTrades = expectMsgClass(CcpTradesMessage.class);

            assertEquals(ccpTrade, ccpTrades.getCcpTrades().get(0));
            assertEquals(1, ccpTrades.getCcpTrades().size());
        }};
    }
}
