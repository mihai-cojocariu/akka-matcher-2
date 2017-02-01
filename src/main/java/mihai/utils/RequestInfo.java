package mihai.utils;

import akka.actor.ActorRef;
import mihai.dto.Trade;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mcojocariu on 2/1/2017.
 */
public class RequestInfo {
    private ActorRef sender;
    private int nbOfAnswers;
    List<Trade> tradesList = new ArrayList<>();

    public RequestInfo(ActorRef sender, int nbOfAnswers) {
        this.sender = sender;
        this.nbOfAnswers = nbOfAnswers;
    }

    public ActorRef getSender() {
        return sender;
    }

    public int getNbOfAnswers() {
        return nbOfAnswers;
    }

    public List<Trade> getTradesList() {
        return tradesList;
    }

    public void setNbOfAnswers(int nbOfAnswers) {
        this.nbOfAnswers = nbOfAnswers;
    }
}
