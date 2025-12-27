package com.example.concertreservationservice.reservationpayment;

import com.example.concertreservationservice.queue.QueueTokenService;
import com.example.concertreservationservice.reservationpayment.application.port.in.*;
import com.example.concertreservationservice.reservationpayment.application.port.out.*;
import com.example.concertreservationservice.reservationpayment.application.service.ReservationPaymentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationPaymentServiceTest {

    @Mock SeatPort seatPort;
    @Mock PointPort pointPort;
    @Mock PaymentPort paymentPort;
    @Mock QueueTokenService queueTokenService;

    @Test
    void 좌석홀드_성공() {
        var service = new ReservationPaymentService(seatPort, pointPort, paymentPort, queueTokenService);

        when(seatPort.tryHold(eq(1L), eq(10), eq("u1"), any(), any())).thenReturn(true);

        var result = service.hold(new HoldSeatUseCase.HoldSeatCommand("t1", "u1", 1L, 10));

        assertThat(result.holdUntil()).isNotNull();
        verify(queueTokenService).validateActive("t1", "u1");
    }

    @Test
    void 결제_성공시_토큰만료() {
        var service = new ReservationPaymentService(seatPort, pointPort, paymentPort, queueTokenService);

        when(seatPort.getSeatPrice(1L, 10)).thenReturn(8000L);
        when(pointPort.deductIfEnough("u1", 8000)).thenReturn(true);
        when(seatPort.confirmSold(eq(1L), eq(10), eq("u1"), any())).thenReturn(true);

        var result = service.pay(new PayUseCase.PayCommand("t1", "u1", 1L, 10, 8000));

        assertThat(result.status()).isEqualTo("SUCCESS");
        verify(queueTokenService).expire("t1");
        verify(paymentPort).saveSuccess("u1", 1L, 10, 8000);
    }
}
