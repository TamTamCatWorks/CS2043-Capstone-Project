package org.tamtamcatworks.auction;
/**
 * Singleton class that manages all auction
 * 
 * <p> Just a singleton for now, we are using double checked singleton
 * 
 * @author Nguyen Hoang Vu
 * @version 1.0
 */
public class AuctionManager {

    /**
     * Volatile so everything threads see the same object
     */
    private static volatile AuctionManager instance;

    /**
     * Private constructor so that only AuctionManager can call it
     */
    private AuctionManager() {

    }

    /**
     * Returns the singleton instance of {@link AuctionManager}
     * 
     * <p> Uses double-checked locking
     * @return the singleton {@link AuctionManager} instance
     */
    public static AuctionManager getInstance() {
        
    }
}
