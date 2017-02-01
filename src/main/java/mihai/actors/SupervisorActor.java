package mihai.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;
import mihai.messages.TradesRequest;
import mihai.messages.TradesResponseMessage;
import mihai.utils.RequestInfo;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mcojocariu on 1/31/2017.
 */
public class SupervisorActor extends UntypedActor {
    LoggingAdapter log = Logging.getLogger(getContext().system(), this);
    private Router router;
    private int nbOfChildren = 0;
    private Map<String, RequestInfo> requestsMap = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof NewTradeMessage) {
            performNewTrade((NewTradeMessage) message);
        } else if (message instanceof NewCcpTradeMessage) {
            performNewCcpTrade((NewCcpTradeMessage) message);
        } else if (message instanceof TradesRequest) {
            performGetTrades((TradesRequest) message);
        } else if (message instanceof TradesResponseMessage) {
            performCollateResponses((TradesResponseMessage) message);
        } else {
            unhandled(message);
        }
    }

    private void performCollateResponses(TradesResponseMessage tradesResponseMessage) {
        String requestId = tradesResponseMessage.getRequestId();
        RequestInfo requestInfo = requestsMap.get(requestId);
        if (requestInfo == null) {
            throw new RuntimeException("Message received from child actor for an inexisting request");
        }

        int nbOfAnswers = requestInfo.getNbOfAnswers();
        nbOfAnswers--;

        updateRequestInfo(requestInfo, nbOfAnswers, tradesResponseMessage);

        if (nbOfAnswers == 0) {
            log.debug("We have {} unmatched trades and {} unmatched CCP trades", requestInfo.getTradesList().size(), requestInfo.getCcpTradesList().size() );
            log.debug("Memory {}", Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory());

            TradesResponseMessage response = new TradesResponseMessage(requestId, requestInfo.getTradesList(), requestInfo.getCcpTradesList());
            requestInfo.getSender().tell(response, getSelf());
            requestsMap.remove(requestId);
        } else {
            requestsMap.put(requestId, requestInfo);
        }
    }

    private void updateRequestInfo(RequestInfo requestInfo, int nbOfAnswers, TradesResponseMessage tradesResponseMessage) {
        requestInfo.setNbOfAnswers(nbOfAnswers);
        if (CollectionUtils.isNotEmpty(tradesResponseMessage.getTrades())) {
            requestInfo.getTradesList().addAll(tradesResponseMessage.getTrades());
        }
        if (CollectionUtils.isNotEmpty(tradesResponseMessage.getCcpTrades())) {
            requestInfo.getCcpTradesList().addAll(tradesResponseMessage.getCcpTrades());
        }
    }

    private void performGetTrades(TradesRequest tradesRequest) {
        String requestId = tradesRequest.getRequestId();
        requestsMap.put(requestId, new RequestInfo(getSender(), nbOfChildren));
        router = getBroadcastRouter();
        router.route(tradesRequest, getSelf());
    }

    private Router getBroadcastRouter() {
        List<Routee> routees = new ArrayList<>();
        Iterator<ActorRef> childActorsIterator = getContext().getChildren().iterator();
        while (childActorsIterator.hasNext()) {
            ActorRef r = childActorsIterator.next();
//            getContext().watch(r);
            routees.add(new ActorRefRoutee(r));
        }
        return new Router(new BroadcastRoutingLogic(), routees);
    }

    private void performNewCcpTrade(NewCcpTradeMessage newCcpTradeMessage) {
        ActorRef actor = getChildActor(newCcpTradeMessage.getCcpTrade().getExchangeReference());
        actor.tell(newCcpTradeMessage, getSelf());
    }

    private void performNewTrade(NewTradeMessage newTradeMessage) {
        ActorRef actor = getChildActor(newTradeMessage.getTrade().getExchangeReference());
        actor.tell(newTradeMessage, getSelf());
    }

    private ActorRef getChildActor(String exchangeReference) {
        String childActorName = getChildActorName(exchangeReference);
        ActorRef actor = getContext().getChild(childActorName);
        if (actor == null) {
            actor = getContext().actorOf(Props.create(TradeWorkerActor.class), childActorName);
            nbOfChildren++;
        }
        return actor;
    }

    private String getChildActorName(String exchangeReference) {
        return "TradeWorkerActor_" + String.valueOf(exchangeReference.charAt(0)).toUpperCase();
    }
}
