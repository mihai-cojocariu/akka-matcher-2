package mihai.messages;

import mihai.dto.CcpTrade;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mcojocariu on 2/1/2017.
 */
public class CcpTradesMessage implements Serializable {
    private String requestId;
    private List<CcpTrade> ccpTrades;

    public CcpTradesMessage(String requestId, List<CcpTrade> ccpTrades) {
        this.requestId = requestId;
        this.ccpTrades = ccpTrades;
    }

    public String getRequestId() {
        return requestId;
    }

    public List<CcpTrade> getCcpTrades() {
        return ccpTrades;
    }
}
