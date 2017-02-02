package mihai.messages;

import mihai.dto.CcpTrade;
import mihai.dto.Trade;
import mihai.utils.RequestType;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mcojocariu on 2/1/2017.
 */
public class TradesResponseMessage implements Serializable{
    private String requestId;
    private RequestType requestType;
    private List<Trade> trades;
    private List<CcpTrade> ccpTrades;

    public TradesResponseMessage(String requestId, RequestType requestType, List<Trade> trades, List<CcpTrade> ccpTrades){
        this.requestId = requestId;
        this.requestType = requestType;
        this.trades = trades;
        this.ccpTrades = ccpTrades;
    }

    public String getRequestId() {
        return requestId;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public List<CcpTrade> getCcpTrades() {
        return ccpTrades;
    }
}
