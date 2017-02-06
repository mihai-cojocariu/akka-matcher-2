package mihai;

/**
 * Created by mcojocariu on 1/31/2017.
 */
import akka.actor.ActorSystem;
import akka.actor.Props;
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
import org.concordion.api.FailFast;
import org.concordion.api.extension.Extension;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.LoggingTooltipExtension;
import org.concordion.ext.timing.TimerExtension;
import mihai.utils.Utils;
import org.concordion.integration.junit4.ConcordionRunner;
import org.concordion.logback.LogbackAdaptor;
import org.concordion.slf4j.ext.ReportLogger;
import org.concordion.slf4j.ext.ReportLoggerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.duration.Duration;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@RunWith(ConcordionRunner.class)
@Extensions(value = TimerExtension.class)
@FailFast
public class ConcordionTradesTest {
    static ActorSystem system;
    private final ReportLogger logger = ReportLoggerFactory.getReportLogger(this.getClass().getName());
    private final Logger tooltipLogger = LoggerFactory.getLogger("TOOLTIP_" + this.getClass().getName());

    public ConcordionTradesTest() {

    }

    static {
        LogbackAdaptor.logInternalStatus();
    }

    public ReportLogger getLogger() {
        return logger;
    }

    public void addConcordionTooltip(final String message) {
        // Logging at debug level means the message won't make it to the console, but will make it to the logs (based on included logback configuration files)
        tooltipLogger.debug(message);
    }
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
        CompletableFuture<Integer> canAddATrade = new CompletableFuture<>();
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
            logger.info("Add a Trade {} on thread {}", this.getClass().getSimpleName(), Thread.currentThread().getName());

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
        CompletableFuture<Integer> canAddACcpTrade = new CompletableFuture<>();
        new JavaTestKit(system) {{
            final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor2");

            final CcpTrade ccpTrade = CcpTrade.aCcpTrade();
            supervisor.tell(new NewCcpTradeMessage(ccpTrade), getTestActor());
            supervisor.tell(new TradesRequest(UUID.randomUUID().toString(), RequestType.GET_CCP_TRADES), getTestActor());

            final TradesResponseMessage response = expectMsgClass(TradesResponseMessage.class);

            //assertEquals(ccpTrade, response.getCcpTrades().get(0));
            //assertEquals(1, response.getCcpTrades().size());
            //assertEquals(ccpTrade, ccpTrades.getCcpTrades().get(0));
            //assertEquals(1, ccpTrades.getCcpTrades().size());
            canAddACcpTrade.complete(response.getCcpTrades().size());
            logger.info("Add a Ccp Trade {} on thread {}", this.getClass().getSimpleName(), Thread.currentThread().getName());
        }};

        try {
            return canAddACcpTrade.get();
        } catch (InterruptedException e) {
            return -1;
        } catch (ExecutionException e) {
            return -1;
        }
    }
}
