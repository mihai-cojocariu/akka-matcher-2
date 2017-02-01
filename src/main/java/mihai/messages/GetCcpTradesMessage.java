package mihai.messages;

import java.io.Serializable;

/**
 * Created by mcojocariu on 2/1/2017.
 */
public class GetCcpTradesMessage implements Serializable {
    String requestId;

    public GetCcpTradesMessage(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
