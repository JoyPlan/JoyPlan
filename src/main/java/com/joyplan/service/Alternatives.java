package com.joyplan.service;

import com.joyplan.model.PlanItem;

import java.util.List;

public record Alternatives(PlanItem target, List<ScoredVenue> options, boolean distanceExtended) {
}
