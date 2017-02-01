package mihai.messages;

import mihai.dto.Trade;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mcojocariu on 1/31/2017.
 */
public class TradesMessage implements Serializable {
    private String requestId;
    private List<Trade> trades;

    public TradesMessage(String requestId, List<Trade> trades){
        this.requestId = requestId;
        this.trades = trades;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public String getRequestId() {
        return requestId;
    }
}
