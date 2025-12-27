package com.example.concertreservationservice.reservationpayment.adapter.in.web;


import com.example.concertreservationservice.reservationpayment.application.port.in.HoldSeatUseCase;
import com.example.concertreservationservice.reservationpayment.application.port.in.PayUseCase;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final HoldSeatUseCase holdSeatUseCase;
    private final PayUseCase payUseCase;

    public ReservationController(HoldSeatUseCase holdSeatUseCase, PayUseCase payUseCase) {
        this.holdSeatUseCase = holdSeatUseCase;
        this.payUseCase = payUseCase;
    }

    @PostMapping("/seats/hold")
    public HoldSeatUseCase.HoldSeatResult hold(@RequestBody HoldRequest req) {
        return holdSeatUseCase.hold(new HoldSeatUseCase.HoldSeatCommand(req.userId, req.concertDateId, req.seatNo));
    }

    @PostMapping("/payments")
    public PayUseCase.PayResult pay(@RequestBody PayRequest req) {
        return payUseCase.pay(new PayUseCase.PayCommand(req.userId, req.concertDateId, req.seatNo, req.amount));
    }

    public static class HoldRequest {
        @NotBlank public String userId;
        @NotNull public Long concertDateId;
        @Min(1) public int seatNo;
    }

    public static class PayRequest {
        @NotBlank public String userId;
        @NotNull public Long concertDateId;
        @Min(1) public int seatNo;
        @Min(1) public long amount;
    }
}
