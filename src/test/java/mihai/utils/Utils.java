package mihai.utils;

import akka.actor.ActorRef;
import mihai.dto.CcpTrade;
import mihai.dto.Trade;
import mihai.messages.NewCcpTradeMessage;
import mihai.messages.NewTradeMessage;

/**
 * Created by mcojocariu on 2/2/2017.
 */
public class Utils {
    public static void loadTrades(ActorRef supervisor, ActorRef testActor, Integer numberOfTrades) {
        Integer tradeExchangeReference = 0;
        Integer ccpTradeExchangeReference = 0;

        for(int i=1; i<=numberOfTrades; i++) {
            tradeExchangeReference += 2;
            Trade trade = Trade.aTrade(tradeExchangeReference.toString());
            NewTradeMessage newTradeMessage = new NewTradeMessage(trade);
            supervisor.tell(newTradeMessage, testActor);

            ccpTradeExchangeReference += 3;
            CcpTrade ccpTrade = CcpTrade.aCcpTrade(ccpTradeExchangeReference.toString());
            NewCcpTradeMessage newCcpTradeMessage = new NewCcpTradeMessage(ccpTrade);
            supervisor.tell(newCcpTradeMessage, testActor);
        }
    }
}
