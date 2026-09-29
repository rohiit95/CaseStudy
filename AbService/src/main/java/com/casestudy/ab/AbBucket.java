package com.casestudy.ab;

public enum AbBucket {
    DEFAULT,
    TEST_1,
    TEST_2,
    TEST_3,
    TEST_4;

    public static final int COUNT = values().length;

    public static AbBucket fromIndex(int index) {
        return values()[Math.floorMod(index, COUNT)];
    }

    public boolean isDefault() {
        return this == DEFAULT;
    }
}
