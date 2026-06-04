package org.tamtamcatworks.auction.persist.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;
import org.tamtamcatworks.auction.model.item.Item;
import org.tamtamcatworks.auction.model.user.User;

public interface AuctionRepository extends JpaRepository<Auction, String> {
  List<Auction> findByStatusAndEndTimeBefore(AuctionStatus status, LocalDateTime time);

  List<Auction> findByStatus(AuctionStatus status);

  List<Auction> findBySeller(User seller);

  @Query(
      """
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
  List<Auction> search(@Param("keyword") String keyword, @Param("status") AuctionStatus status);

  @Query(
      """
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
          and (:itemType is null or TYPE(i) = :itemType)
        order by a.creationDate desc
        """)
  Page<Auction> searchPaged(
      @Param("keyword") String keyword,
      @Param("status") AuctionStatus status,
      @Param("itemType") Class<? extends Item> itemType,
      Pageable pageable);
}
