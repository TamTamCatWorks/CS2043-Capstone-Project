package org.tamtamcatworks.auction;

import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

public class AuctionManagerTest {

    @Test
    void shouldReturnSameInstance() {
        AuctionManager first = AuctionManager.getInstance();
        AuctionManager second = AuctionManager.getInstance();
        assertSame(first, second); // assert same check the same instance
    }

    /**
     * Testing singleton in a multi-threaded environment
     * @throws InterruptedException
     */
    @Test
    void shouldReturnSameInstanceAcrossThreads() throws InterruptedException {
        AuctionManager[] instances = new AuctionManager[2];

        Thread t1 = new Thread(() -> instances[0] = AuctionManager.getInstance());
        Thread t2 = new Thread(() -> instances[1] = AuctionManager.getInstance());

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assertSame(instances[0], instances[1]);
    }
}   

