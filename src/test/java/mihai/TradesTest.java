package mihai;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.testkit.JavaTestKit;
import akka.testkit.TestActorRef;
import mihai.actors.SupervisorActor;
import mihai.dto.CcpTrade;
import mihai.dto.Trade;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;
import mihai.messages.TradesRequest;
import mihai.messages.TradesResponseMessage;
import mihai.utils.RequestType;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.runner.RunWith;

/**
 * Created by mcojocariu on 1/31/2017.
 */

@RunWith(ConcordionRunner.class)
public class TradesTest {
    static ActorSystem system;
   // LoggingAdapter log = Logging.getLogger(system, this);

    @BeforeClass
    public static void setup() {
        system = ActorSystem.create();
    }

    @AfterClass
    public static void teardown() {
        system.shutdown();
        system.awaitTermination(Duration.create("10 seconds"));
    }


    public Integer canAddATrade() {
        CompletableFuture<Integer> canAddATrade = new CompletableFuture<Integer>();
        new JavaTestKit(system) {{
            final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor1");

            final Trade trade = Trade.aTrade();
            supervisor.tell(new NewTradeMessage(trade), getTestActor());
            //final Trade trade1 = Trade.aTrade();
            //supervisor.tell(new NewTradeMessage(trade1), getTestActor());

            supervisor.tell(new TradesRequest(UUID.randomUUID().toString(), RequestType.GET_TRADES), getTestActor());

            final TradesResponseMessage response = expectMsgClass(TradesResponseMessage.class);

            //assertEquals(true, response.getTrades().contains(trade));
            //assertEquals(true, response.getTrades().contains(trade1));
            //assertEquals(2, response.getTrades().size());
            canAddATrade.complete(response.getTrades().size());
        }};

        try {
            return canAddATrade.get();
        } catch (InterruptedException e) {
            return -1;
        } catch (ExecutionException e) {
            return -1;
        }
    }


    public Integer canAddACcpTrade() {
        CompletableFuture<Integer> canAddACcpTrade = new CompletableFuture<Integer>();
        new JavaTestKit(system) {{
            final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor2");

            final CcpTrade ccpTrade = CcpTrade.aCcpTrade();
            supervisor.tell(new NewCcpTradeMessage(ccpTrade), getTestActor());
            supervisor.tell(new TradesRequest(UUID.randomUUID().toString(), RequestType.GET_CCP_TRADES), getTestActor());

            final TradesResponseMessage response = expectMsgClass(TradesResponseMessage.class);

            assertEquals(ccpTrade, response.getCcpTrades().get(0));
            assertEquals(1, response.getCcpTrades().size());
            //assertEquals(ccpTrade, ccpTrades.getCcpTrades().get(0));
            //assertEquals(1, ccpTrades.getCcpTrades().size());
            canAddACcpTrade.complete(response.getCcpTrades().size());
        }};

        try {
            return canAddACcpTrade.get();
        } catch (InterruptedException e) {
            return -1;
        } catch (ExecutionException e) {
            return -1;
        }
    }

    @Test
    public void volumeTestActors() {
        new JavaTestKit(system) {
            {
                final int numberOfTrades = 500000;
                final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor3");
                loadTrades(supervisor, getTestActor(), numberOfTrades);

                Long startTimestamp = System.currentTimeMillis();


                supervisor.tell(new TradesRequest(UUID.randomUUID().toString(), RequestType.GET_UNMATCHED_TRADES), getTestActor());
                TradesResponseMessage response = expectMsgClass(new FiniteDuration(20, TimeUnit.SECONDS), TradesResponseMessage.class);

                Long endTimestamp = System.currentTimeMillis();
                Long diff = endTimestamp - startTimestamp;
               // log.debug("Trades matching duration (ms): " + diff);
            }
        };
    }

    private void loadTrades(ActorRef supervisor, ActorRef testActor, Integer numberOfTrades) {
        Long startTimestamp = System.currentTimeMillis();

        Integer tradeExchangeReference = 0;
        Integer ccpTradeExchangeReference = 0;

        for(int i=1; i<=numberOfTrades; i++) {
            tradeExchangeReference += 2;
            Trade trade = new Trade(tradeExchangeReference.toString());
            NewTradeMessage newTradeMessage = new NewTradeMessage(trade);
            supervisor.tell(newTradeMessage, testActor);

            ccpTradeExchangeReference += 3;
            CcpTrade ccpTrade = new CcpTrade(ccpTradeExchangeReference.toString());
            NewCcpTradeMessage newCcpTradeMessage = new NewCcpTradeMessage(ccpTrade);
            supervisor.tell(newCcpTradeMessage, testActor);
        }

        Long endTimestamp = System.currentTimeMillis();
        Long diff = endTimestamp - startTimestamp;
        //log.debug("Trades loading duration (ms): " + diff);
    }
}
