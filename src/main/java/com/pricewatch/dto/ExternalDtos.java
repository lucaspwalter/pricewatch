package com.pricewatch.dto;

public final class ExternalDtos {
    private ExternalDtos() {
    }

    public record EvolutionSendTextRequest(String number, String text) {
    }
}
