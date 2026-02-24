package io.hhplus.concert.api.ranking;

import io.hhplus.concert.application.ranking.dto.ConcertRankingItem;
import io.hhplus.concert.application.ranking.service.ConcertRankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/concerts")
public class ConcertRankingController {

    private final ConcertRankingService rankingService;

    @GetMapping("/ranking")
    public List<ConcertRankingItem> ranking(@RequestParam(defaultValue = "10") int limit) {
        return rankingService.top(limit);
    }
}