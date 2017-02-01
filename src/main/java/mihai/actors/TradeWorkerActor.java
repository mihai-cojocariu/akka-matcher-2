package mihai.actors;

import akka.actor.UntypedActor;
import mihai.dto.CcpTrade;
import mihai.dto.Trade;
import mihai.messages.GetTradesMessage;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;
import mihai.messages.TradesMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mcojocariu on 1/31/2017.
 */
public class TradeWorkerActor extends UntypedActor {
    Map<String, Trade> tradeMap = new HashMap<>();
    Map<String, CcpTrade> ccpTradeMap = new HashMap<>();

    @Override
    public void onReceive(Object message) throws Throwable {
        if (message instanceof NewTradeMessage) {
            NewTradeMessage newTradeMessage = (NewTradeMessage) message;
            Trade trade = newTradeMessage.getTrade();
            tradeMap.put(trade.getExchangeReference(), trade);
        } else if (message instanceof NewCcpTradeMessage) {
            NewCcpTradeMessage newCcpTradeMessage = (NewCcpTradeMessage) message;
            CcpTrade ccpTrade = newCcpTradeMessage.getCcpTrade();
            ccpTradeMap.put(ccpTrade.getExchangeReference(), ccpTrade);
        } else if (message instanceof GetTradesMessage) {
            String requestId = ((GetTradesMessage) message).getRequestId();
            List<Trade> trades = new ArrayList<>(tradeMap.values());
            getSender().tell(new TradesMessage(requestId, trades), getSelf());
        } else {
            unhandled(message);
        }
    }


}
