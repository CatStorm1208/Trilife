package de.catstorm.trilife.logic;

public class AlternatingInt {
    private final int value1;
    private final int value2;
    private boolean shift = false;

    public AlternatingInt(int value1, int value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public int get() {
        return shift ? value2 : value1;
    }

    public int next() {
        int result = get();
        skip();

        return result;
    }

    public void skip() {
        shift = !shift;
    }

    public void reset() {
        shift = false;
    }
}