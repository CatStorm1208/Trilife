package de.catstorm.trilife.logic;

public class AlternatingValue<Type> {
    private final Type value1;
    private final Type value2;
    private boolean shift = false;

    public AlternatingValue(Type value1, Type value2) {
        this.value1 = value1;
        this.value2 = value2;
    }

    public Type get() {
        return shift? value2 : value1;
    }

    public Type next() {
        skip();

        return shift? value1 : value2;
    }

    public void skip() {
        shift = !shift;
    }

    public void reset() {
        shift = false;
    }
}