package edu.campus.library.domain.member;

import edu.campus.library.domain.Identifiable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/** A library reader (student or staff) with a fine balance. */
public class Member implements Identifiable, Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final String email;
    private final MemberType type;
    private boolean active = true;
    private BigDecimal fineBalance = BigDecimal.ZERO.setScale(2);

    public Member(String id, String name, String email, MemberType type) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
        this.type = Objects.requireNonNull(type);
    }

    @Override
    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String email() {
        return email;
    }

    public MemberType type() {
        return type;
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }

    public BigDecimal fineBalance() {
        return fineBalance;
    }

    public void addFine(BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Fine cannot be negative");
        }
        fineBalance = fineBalance.add(amount).setScale(2, RoundingMode.HALF_UP);
    }

    /** Pays part or all of the balance. Returns the amount actually applied. */
    public BigDecimal payFine(BigDecimal amount) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Payment must be positive");
        }
        BigDecimal applied = amount.min(fineBalance).setScale(2, RoundingMode.HALF_UP);
        fineBalance = fineBalance.subtract(applied).setScale(2, RoundingMode.HALF_UP);
        return applied;
    }

    @Override
    public String toString() {
        return id + " " + name + " (" + type + ", fines " + fineBalance + ")";
    }
}
