package com.example.concertreservationservice.query;

import com.example.concertreservationservice.queue.QueueTokenService;
import com.example.concertreservationservice.query.repository.ConcertDateEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ConcertQueryController {

    private final ConcertQueryService service;
    private final QueueTokenService queueTokenService;

    public ConcertQueryController(ConcertQueryService service, QueueTokenService queueTokenService) {
        this.service = service;
        this.queueTokenService = queueTokenService;
    }

    @GetMapping("/concerts/{concertId}/dates")
    public List<ConcertDateEntity> dates(@RequestHeader("X-QUEUE-TOKEN") String token,
                                         @RequestParam String userId,
                                         @PathVariable Long concertId) {
        queueTokenService.validateActive(token, userId);
        return service.getDates(concertId);
    }

    @GetMapping("/concert-dates/{concertDateId}/seats")
    public List<ConcertQueryService.SeatView> seats(@RequestHeader("X-QUEUE-TOKEN") String token,
                                                    @RequestParam String userId,
                                                    @PathVariable Long concertDateId) {
        queueTokenService.validateActive(token, userId);
        return service.getSeats(concertDateId);
    }
}
