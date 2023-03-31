package io.roach.spring.statemachine.domain;

public enum PaymentEvent {
    ABORT("Abort payment"),
    AUTHORIZE("Contact processor for charge authorization"),
    AUTH_APPROVED("Processor approved charge"),
    AUTH_DECLINED("Processor rejected charge"),
    CANCEL("Approved charge cancellation"),
    CAPTURE("Authorized amount settlement"),
    CAPTURE_SUCCESS("Capture approved"),
    CAPTURE_FAILED("Capture failed"),
    REVERSE("Captured amount reversal"),
    REVERSE_SUCCESS("Refund successful"),
    REVERSE_FAILED("Refund failure");

    String note;

    PaymentEvent(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }
}
