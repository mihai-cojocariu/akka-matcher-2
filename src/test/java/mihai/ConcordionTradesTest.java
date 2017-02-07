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
import mihai.utils.TradeDirection;
import mihai.utils.Utils;
import org.concordion.api.extension.Extensions;
import org.concordion.ext.excel.ExcelExtension;
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
import scala.concurrent.duration.FiniteDuration;

import java.text.ParseException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@RunWith(ConcordionRunner.class)
//@Extensions(value = TimerExtension.class)
@Extensions(ExcelExtension.class)
//@FailFast
public class ConcordionTradesTest {
    static ActorSystem system;
    private static int supervisorIndex = 0;
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
            //final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor1");
            final TestActorRef<SupervisorActor> supervisor = getSupervisorActor();
            final Trade trade = Trade.aTrade();
            supervisor.tell(new NewTradeMessage(trade), getTestActor());

            supervisor.tell(new TradesRequest(UUID.randomUUID().toString(), RequestType.GET_TRADES), getTestActor());

            final TradesResponseMessage response = expectMsgClass(TradesResponseMessage.class);

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
            //final TestActorRef<SupervisorActor> supervisor = TestActorRef.create(system, Props.create(SupervisorActor.class), "supervisor2");
            final TestActorRef<SupervisorActor> supervisor = getSupervisorActor();

            final CcpTrade ccpTrade = CcpTrade.aCcpTrade();
            supervisor.tell(new NewCcpTradeMessage(ccpTrade), getTestActor());
            supervisor.tell(new TradesRequest(UUID.randomUUID().toString(), RequestType.GET_CCP_TRADES), getTestActor());

            final TradesResponseMessage response = expectMsgClass(TradesResponseMessage.class);

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



    public String canPerformAMatch(String externalReference, String externalReferenceCCP, String tradeReference,
                                  String tradeDate, String tradeDateCCP, String is2in, String is2inCCP,
                                  String direction, String directionCCP,
                                  String quantity, String quantityCCP, String currency, String currencyCCP,
                                  String amount, String amountCCP) throws ParseException {

        CompletableFuture<Trade> canPerformAMAtch = new CompletableFuture<>();
        new JavaTestKit(system) {{
            //log.debug("Starting canPerformAMatch()...");

            final TestActorRef<SupervisorActor> supervisor = getSupervisorActor();


            final Trade trade = new Trade.TradeBuilder(externalReference)
                    .withReference(tradeReference)
                    .withTradeDate(Utils.getZonedDateTime(tradeDate))
                    .withIsin(is2in)
                    .withDirection(TradeDirection.valueOf(direction))
                    .withQuantity(Integer.parseInt(quantity))
                    .withCurrency(currency)
                    .withAmount(Float.parseFloat(amount))
                    .build();

            final CcpTrade ccpTrade = new CcpTrade.CcpTradeBuilder(externalReferenceCCP)
                        .withTradeDate(Utils.getZonedDateTime(tradeDateCCP))
                        .withIsin(is2inCCP)
                        .withDirection(TradeDirection.valueOf(directionCCP))
                        .withQuantity(Integer.parseInt(quantityCCP))
                        .withCurrency(currencyCCP)
                        .withAmount(Float.parseFloat(amountCCP))
                        .build();


            supervisor.tell(new NewTradeMessage(trade), getTestActor());
            supervisor.tell(new NewCcpTradeMessage(ccpTrade), getTestActor());
            supervisor.tell(new TradesRequest(UUID.randomUUID().toString(), RequestType.GET_UNMATCHED_TRADES), getTestActor());

            final TradesResponseMessage response = expectMsgClass(TradesResponseMessage.class);

            new Within(new FiniteDuration(10, TimeUnit.SECONDS)) {
                protected void run() {
                   // assertEquals(0, response.getTrades().get(0).getExchangeReference());
                    canPerformAMAtch.complete(response.getTrades().get(0));
                    logger.info("Perform a Match - Trade {} on thread {}", this.getClass().getSimpleName(), Thread.currentThread().getName());
                }
            };
        }};
        try {
            return canPerformAMAtch.get().getExchangeReference();
        } catch (InterruptedException e) {
            return "ERROR";
        } catch (ExecutionException e) {
            return "ERROR";
        }
    }

    private TestActorRef getSupervisorActor() {
        String name = "Supervisor_" + ++supervisorIndex;
        return TestActorRef.create(system, Props.create(SupervisorActor.class), name);
    }



}


