package io.roach.spring.statemachine.domain;

public enum PaymentState {
    CREATED("Initial state"),
    AUTHORIZED("Charge approved by processor"),
    AUTH_ERROR("Charge declined by processor"),
    ABORTED("Payment aborted before auth"),
    CANCELLED("Payment cancelled before capture"),
    CAPTURED("Payment verified and settled by processor"),
    CAPTURE_ERROR("Authorized charge declined by processor"),
    REVERSED("Captured payment refunded"),
    REVERSE_ERROR("Captured reversal failed");

    String note;

    PaymentState(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }
}
