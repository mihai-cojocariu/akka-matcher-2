package mihai.actors;

import akka.actor.UntypedActor;
import mihai.dto.CcpTrade;
import mihai.dto.Trade;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;
import mihai.messages.TradesRequest;
import mihai.messages.TradesResponseMessage;

import java.util.ArrayList;
import java.util.Collections;
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
        } else if (message instanceof TradesRequest) {
            performTradesRequest((TradesRequest) message);
        } else {
            unhandled(message);
        }
    }

    public void performTradesRequest(TradesRequest tradesRequest) {
        String requestId = tradesRequest.getRequestId();

        TradesResponseMessage response = null;
        switch (tradesRequest.getRequestType()) {
            case GET_TRADES:
                List<Trade> trades = new ArrayList<>(tradeMap.values());
                response = new TradesResponseMessage(requestId, trades, Collections.emptyList());
                break;
            case GET_CCP_TRADES:
                List<CcpTrade> ccpTrades = new ArrayList<>(ccpTradeMap.values());
                response = new TradesResponseMessage(requestId, Collections.emptyList(), ccpTrades);
                break;
            case GET_UNMATCHED_TRADES:
                response = getUnmatchedTradesResponse(requestId);
                break;
        }

        if (response != null) {
            getSender().tell(response, getSelf());
        }
    }

    private TradesResponseMessage getUnmatchedTradesResponse(String requestId) {
        List<Trade> unmatchedTrades = new ArrayList<>();
        List<CcpTrade> unmatchedCcpTrades = new ArrayList<>();

        List<Trade> trades = new ArrayList<>(tradeMap.values());
        for (Trade trade : trades) {
            if (!ccpTradeMap.containsKey(trade.getExchangeReference())) {
                unmatchedTrades.add(trade);
            }
        }

        List<CcpTrade> ccpTrades = new ArrayList<>(ccpTradeMap.values());
        for (CcpTrade ccpTrade : ccpTrades) {
            if (!tradeMap.containsKey(ccpTrade.getExchangeReference())) {
                unmatchedCcpTrades.add(ccpTrade);
            }
        }

        return new TradesResponseMessage(requestId, unmatchedTrades, unmatchedCcpTrades);
    }

}
