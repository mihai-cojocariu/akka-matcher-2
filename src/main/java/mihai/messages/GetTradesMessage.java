package mihai.messages;

import java.io.Serializable;

/**
 * Created by mcojocariu on 1/31/2017.
 */
public class GetTradesMessage implements Serializable {
    String requestId;

    public GetTradesMessage(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}
