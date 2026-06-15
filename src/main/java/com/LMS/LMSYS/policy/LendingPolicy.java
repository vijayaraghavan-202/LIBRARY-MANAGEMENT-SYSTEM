package com.LMS.LMSYS.policy;

public final class LendingPolicy {

    public static final int LOAN_PERIOD_DAYS = 14;
    public static final int DUE_SOON_REMINDER_DAYS = 2;
    public static final int MAX_ACTIVE_BORROWS_PER_MEMBER = 5;

    private LendingPolicy() {
    }
}
