package com.joyplan.service;

import java.util.List;

public record AiDraft(String summary, List<Stop> stops) {

    public record Stop(Long venueId, String reason) {
    }
}
