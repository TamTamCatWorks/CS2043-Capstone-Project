package org.tamtamcatworks.auction.service.auction;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tamtamcatworks.auction.model.Auction;
import org.tamtamcatworks.auction.model.AuctionStatus;

@ExtendWith(MockitoExtension.class)
class AuctionSchedulerTest {

  @Mock
  private AuctionService auctionService;

  private AuctionScheduler scheduler;

  @BeforeEach
  void setUp() {
    scheduler = new AuctionScheduler(auctionService);
  }

  @Test
  void testCloseExpiredAuctionsSuccess() {
    // Expired auction (endTime in past)
    Auction expiredAuction = mock(Auction.class);
    when(expiredAuction.getId()).thenReturn("auc1");
    when(expiredAuction.getEndTime()).thenReturn(LocalDateTime.now().minusMinutes(5));

    // Non-expired auction (endTime in future)
    Auction activeAuction = mock(Auction.class);
    when(activeAuction.getEndTime()).thenReturn(LocalDateTime.now().plusHours(1));

    when(auctionService.findByStatus(AuctionStatus.ACTIVE))
        .thenReturn(List.of(expiredAuction, activeAuction));

    scheduler.closeExpiredAuctions();

    // Expired auction should be closed, non-expired should not
    verify(auctionService, times(1)).close("auc1");
    verify(auctionService, never()).close(eq("auc2"));
  }

  @Test
  void testCloseExpiredAuctionsHandlesException() {
    Auction expiredAuction = mock(Auction.class);
    when(expiredAuction.getId()).thenReturn("auc1");
    when(expiredAuction.getEndTime()).thenReturn(LocalDateTime.now().minusMinutes(5));

    when(auctionService.findByStatus(AuctionStatus.ACTIVE)).thenReturn(List.of(expiredAuction));
    // Throw exception when trying to close
    doThrow(new IllegalStateException("Failed")).when(auctionService).close("auc1");

    // Should not throw exception from scheduler, handles gracefully
    scheduler.closeExpiredAuctions();

    verify(auctionService, times(1)).close("auc1");
  }
}
