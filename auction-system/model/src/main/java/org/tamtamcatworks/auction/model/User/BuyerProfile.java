package org.tamtamcatworks.auction.model.User;

import org.tamtamcatworks.auction.model.Entity;
import java.util.List;

public class BuyerProfile extends Entity {
    private List<String> biddingHistory;
    private List<String> watchlist;

    protected BuyerProfile(List<String> biddingHistory, List<String> watchlist) {
        this.biddingHistory = biddingHistory;
        this.watchlist = watchlist;
    }

    public List<String> getBiddingHistory() { return biddingHistory; }

    public List<String> getWatchlist() { return watchlist; }

    public void setBiddingHistory(List<String> newBiddingHistory) {
        this.biddingHistory = newBiddingHistory;
    }

    public void setWatchlist(List<String> newWatchlist) {
        this.watchlist = newWatchlist;
    }

    public String getDisplayInfo() {
        return "BuyerId: " + this.getEntityId() + ", bidding history: " + biddingHistory + ", watchlist: " + watchlist;
    }
}
