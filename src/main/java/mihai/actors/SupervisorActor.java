package mihai.actors;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.routing.ActorRefRoutee;
import akka.routing.BroadcastRoutingLogic;
import akka.routing.Routee;
import akka.routing.Router;
import mihai.dto.Trade;
import mihai.messages.GetCcpTradesMessage;
import mihai.messages.GetTradesMessage;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;
import mihai.messages.TradesMessage;
import mihai.utils.RequestInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by mcojocariu on 1/31/2017.
 */
public class SupervisorActor extends UntypedActor {
    private Router router;
    private int nbOfChildren = 0;
    private Map<String, RequestInfo> requestsMap = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof NewTradeMessage) {
            performNewTrade((NewTradeMessage) message);
        } else if (message instanceof NewCcpTradeMessage) {
            performNewCcpTrade((NewCcpTradeMessage) message);
        } else if (message instanceof GetTradesMessage) {
            performGetTrades((GetTradesMessage) message);
        } else if (message instanceof GetCcpTradesMessage) {
            performGetCcpTrades((GetCcpTradesMessage) message);
        } else if (message instanceof TradesMessage) {
            performCollateTrades((TradesMessage) message);
        } else {
            unhandled(message);
        }
    }

    public void performCollateTrades(TradesMessage tradesMessage) {
        String requestId = tradesMessage.getRequestId();
        RequestInfo requestInfo = requestsMap.get(requestId);
        if (requestInfo == null) {
            throw new RuntimeException("Message received from child actor for an inexisting request");
        }

        int nbOfAnswers = requestInfo.getNbOfAnswers();
        nbOfAnswers--;

        requestInfo.setNbOfAnswers(nbOfAnswers);
        requestInfo.getTradesList().addAll(tradesMessage.getTrades());

        if (nbOfAnswers == 0) {
            requestInfo.getSender().tell(new TradesMessage(requestId, requestInfo.getTradesList()), getSelf());
            requestsMap.remove(requestId);
        } else {
            requestsMap.put(requestId, requestInfo);
        }
    }

    private void performGetCcpTrades(GetCcpTradesMessage getCcpTradesMessage) {

    }

    private void performGetTrades(GetTradesMessage getTradesMessage) {
        String requestId = getTradesMessage.getRequestId();
        requestsMap.put(requestId, new RequestInfo(getSender(), nbOfChildren));

        router = getBroadcastRouter();
        router.route(getTradesMessage, getSelf());
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
