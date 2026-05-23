package com.marketsense.watchlist;

import com.marketsense.watchlist.Watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    boolean existsByUserIdAndSymbol(Long userId, String symbol);

    Watchlist findByUserIdAndSymbol(Long userId, String symbol);

    Page<Watchlist> findByUserId(Long userId, Pageable pageable);

    @Query("""
       SELECT w FROM Watchlist w 
       WHERE w.user.id = :userId 
       AND (LOWER(w.name) LIKE LOWER(CONCAT('%', :keyword, '%')) 
            OR LOWER(w.symbol) LIKE LOWER(CONCAT('%', :keyword, '%')))
       """)
    Page<Watchlist> searchUserWatchlist(
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
