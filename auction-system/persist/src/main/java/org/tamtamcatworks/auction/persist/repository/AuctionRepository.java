package org.tamtamcatworks.auction.persist.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.user.User;
import java.util.List;

public interface AuctionRepository extends JpaRepository<Auction, String> {
    List<Auction> findByStatus(AuctionStatus status);
    List<Auction> findBySeller(User seller);

    @Query("""
        select distinct a
        from Auction a
        join a.item i
        join a.seller s
        where (:status is null or a.status = :status)
          and (
              :keyword is null or :keyword = ''
              or lower(a.title) like lower(concat('%', :keyword, '%'))
              or lower(i.name) like lower(concat('%', :keyword, '%'))
              or lower(i.description) like lower(concat('%', :keyword, '%'))
              or lower(s.fullName) like lower(concat('%', :keyword, '%'))
          )
        order by a.creationDate desc
        """)
    List<Auction> search(@Param("keyword") String keyword,
                         @Param("status") AuctionStatus status);
}