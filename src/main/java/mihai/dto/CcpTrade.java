package mihai.dto;

import org.apache.commons.lang3.RandomStringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

/**
 * Created by mcojocariu on 1/31/2017.
 */
public class CcpTrade implements Serializable {
    private String exchangeReference;
    private List<String> someData = new ArrayList<>();

    public CcpTrade(String exchangeReference) {
        this.exchangeReference = exchangeReference;
        for (int i=0;i<20;i++){
            someData.add(RandomStringUtils.randomAlphabetic(10));
        }
    }

    public String getExchangeReference() {
        return exchangeReference;
    }

    public static CcpTrade aCcpTrade() {
        return new CcpTrade(randomAlphabetic(10));
    }
}
