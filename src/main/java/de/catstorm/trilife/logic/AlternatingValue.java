package de.catstorm.trilife.logic;

public class AlternatingValue<T> {
    private final T value1;
    private final T value2;
    private boolean shift = false;

    public AlternatingValue(T value1, T value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public T get() {
        return shift ? value2 : value1;
    }

    public T next() {
        skip();

        return shift ? value1 : value2;
    }

    public void skip() {
        shift = !shift;
    }

    public void reset() {
        shift = false;
    }
}