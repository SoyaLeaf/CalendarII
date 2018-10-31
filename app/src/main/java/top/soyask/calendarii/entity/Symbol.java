package top.soyask.calendarii.entity;

public enum Symbol {
    RECT("rect", 1),
    CIRCLE("circle", 2),
    TRIANGLE("triangle", 3),
    STAR("star", 4),
    HEART("heart", 5),
    ;

    public final String KEY;
    public final int VALUE;

    Symbol(String key, int value) {
        this.KEY = key;
        this.VALUE = value;
    }

}
