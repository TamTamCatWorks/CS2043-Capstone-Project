package org.tamtamcatworks.auction.persist;

import java.time.LocalDateTime;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.BidTransaction;
import org.tamtamcatworks.auction.model.item.*;
import org.tamtamcatworks.auction.model.user.BuyerProfile;
import org.tamtamcatworks.auction.model.user.SellerProfile;
import org.tamtamcatworks.auction.model.user.User;

import org.tamtamcatworks.auction.persist.repository.AuctionRepository;
import org.tamtamcatworks.auction.persist.repository.BidTransactionRepository;
import org.tamtamcatworks.auction.persist.repository.ItemRepository;
import org.tamtamcatworks.auction.persist.repository.UserRepository;


@Configuration
@EntityScan("org.tamtamcatworks.auction")
public class LoadDatabase {

    private static final Logger log = LoggerFactory.getLogger(LoadDatabase.class);

    private static String BIDDER_ID;
    private static String SELLER_ID;
    private static String ITEM_ID;
    private static String AUCTION_ID;

    @Bean
    @Order(0)
    CommandLineRunner initUser(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            User bidder = new User("testbidder", "test1@example.com", passwordEncoder.encode("hashed123"), "Test Bidder", 3000.0);
            bidder.setBuyerProfile(new BuyerProfile());

            User seller = new User("testseller", "test2@example.com", passwordEncoder.encode("hashed123"), "Test Seller", 1000.0);
            seller.setBuyerProfile(new BuyerProfile());
            seller.setSellerProfile(new SellerProfile());

            User savedBidder = userRepository.save(bidder);
            BIDDER_ID = savedBidder.getId();
            log.info("Preloading " + savedBidder);

            User savedSeller = userRepository.save(seller);
            SELLER_ID = savedSeller.getId();
            log.info("Preloading " + savedSeller);
        };
    }

    @Bean
    @Order(1)
    CommandLineRunner initItems(UserRepository userRepository,
                                ItemRepository itemRepository) {
        return args -> {
            User seller = userRepository.findById(
                Objects.requireNonNull(SELLER_ID)
            ).orElseThrow();

            Art art = new Art(
                "testArt",
                "testArtTest",
                1000.0,
                ItemCondition.FAIR,
                "https://example.com/test-art.jpg",
                seller,
                "testArtist",
                1999,
                "testMedium",
                "100cm x 80cm",
                false);

            Item savedArt = itemRepository.save(art);
            ITEM_ID = savedArt.getId();
            log.info("Preloading " + savedArt);
        };
    }

    @Bean
    @Order(2)
    CommandLineRunner initAuction(UserRepository userRepository,
                                ItemRepository itemRepository,
                                AuctionRepository auctionRepository) {
        return args -> {
            User seller = userRepository.findById(
                Objects.requireNonNull(SELLER_ID)
            ).orElseThrow();
            Item item = itemRepository.findById(
                Objects.requireNonNull(ITEM_ID)
            ).orElseThrow();

            Auction auction = new Auction("testAuction", seller, item,
                1000, LocalDateTime.now(), LocalDateTime.now().plusDays(7));
            Auction savedAuction = auctionRepository.save(auction);
            AUCTION_ID = savedAuction.getId();
            log.info("Preloading " + savedAuction);
        };
    }

    @Bean
    @Order(3)
    CommandLineRunner initBidTransaction(UserRepository userRepository,
                                        AuctionRepository auctionRepository,
                                        BidTransactionRepository bidTransactionRepository,
                                        PlatformTransactionManager transactionManager) {
        return args -> {
            TransactionTemplate transactionTemplate =
                new TransactionTemplate(
                    Objects.requireNonNull(transactionManager)
                );
            transactionTemplate.execute(status -> {
                User bidder = userRepository.findById(
                    Objects.requireNonNull(BIDDER_ID)
                ).orElseThrow();
                Auction auction = auctionRepository.findById(
                    Objects.requireNonNull(AUCTION_ID)
                ).orElseThrow();
                auction.open();

                bidder.holdFunds(2000);
                BidTransaction bidTransaction = new BidTransaction(auction, bidder, 2000, BidTransaction.BidType.MANUAL);
                auction.recordBid(bidTransaction);

                Auction savedAuction = auctionRepository.save(auction);
                userRepository.save(bidder);
                log.info("Preloading auction {}", savedAuction);

                return null;
            });
        };
    }
}