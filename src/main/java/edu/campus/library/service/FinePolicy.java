package edu.campus.library.service;

import edu.campus.library.domain.fine.FineStrategy;
import edu.campus.library.domain.member.MemberType;

import java.util.EnumMap;
import java.util.Map;

/** Chooses the fine Strategy for a reader type. Configured in App. */
public class FinePolicy {
    private final Map<MemberType, FineStrategy> strategies;

    public FinePolicy(Map<MemberType, FineStrategy> strategies) {
        this.strategies = new EnumMap<>(strategies);
        for (MemberType t : MemberType.values()) {
            if (!this.strategies.containsKey(t)) {
                throw new IllegalArgumentException("No fine strategy for " + t);
            }
        }
    }

    public FineStrategy strategyFor(MemberType type) {
        return strategies.get(type);
    }
}
