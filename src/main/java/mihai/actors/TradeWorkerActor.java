package mihai.actors;

import akka.actor.UntypedActor;
import mihai.dto.CcpTrade;
import mihai.dto.Trade;
import mihai.messages.CancelCcpTradeMessage;
import mihai.messages.CancelTradeMessage;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;
import mihai.messages.TradesRequest;
import mihai.messages.TradesResponseMessage;
import mihai.utils.RequestType;

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
            performNewTrade((NewTradeMessage) message);
        } else if (message instanceof NewCcpTradeMessage) {
            performNewCcpTrade((NewCcpTradeMessage) message);
        } else if (message instanceof CancelTradeMessage) {
            performCancelTrade((CancelTradeMessage) message);
        } else if (message instanceof CancelCcpTradeMessage) {
            performCancelCcpTrade((CancelCcpTradeMessage) message);
        } else if (message instanceof TradesRequest) {
            performTradesRequest((TradesRequest) message);
        } else {
            unhandled(message);
        }
    }

    private void performCancelCcpTrade(CancelCcpTradeMessage cancelCcpTradeMessage) {
        CcpTrade ccpTrade = cancelCcpTradeMessage.getCcpTrade();
        ccpTradeMap.remove(ccpTrade.getExchangeReference());
    }

    private void performCancelTrade(CancelTradeMessage cancelTradeMessage) {
        Trade trade = cancelTradeMessage.getTrade();
        tradeMap.remove(trade.getExchangeReference());
    }

    private void performTradesRequest(TradesRequest tradesRequest) {
        String requestId = tradesRequest.getRequestId();

        TradesResponseMessage response = null;
        switch (tradesRequest.getRequestType()) {
            case GET_CS_TRADES:
                List<Trade> trades = new ArrayList<>(tradeMap.values());
                response = new TradesResponseMessage(requestId, tradesRequest.getRequestType(), trades, Collections.emptyList());
                break;
            case GET_CCP_TRADES:
                List<CcpTrade> ccpTrades = new ArrayList<>(ccpTradeMap.values());
                response = new TradesResponseMessage(requestId, tradesRequest.getRequestType(), Collections.emptyList(), ccpTrades);
                break;
            case GET_UNMATCHED_TRADES:
                response = getUnmatchedTradesResponse(requestId, tradesRequest.getRequestType());
                break;
        }

        if (response != null) {
            getSender().tell(response, getSelf());
        }
    }

    private TradesResponseMessage getUnmatchedTradesResponse(String requestId, RequestType requestType) {
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

        return new TradesResponseMessage(requestId, requestType, unmatchedTrades, unmatchedCcpTrades);
    }

    private void performNewTrade(NewTradeMessage newTradeMessage) {
        Trade trade = newTradeMessage.getTrade();
        tradeMap.put(trade.getExchangeReference(), trade);
    }

    private void performNewCcpTrade(NewCcpTradeMessage newCcpTradeMessage) {
        CcpTrade ccpTrade = newCcpTradeMessage.getCcpTrade();
        ccpTradeMap.put(ccpTrade.getExchangeReference(), ccpTrade);
    }
}
