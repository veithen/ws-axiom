import org.apache.axiom.checker.union.Union;

/** Tests for {@code instanceof}-based narrowing ({@code UnionTransfer}). */
class InstanceofNarrowing {

    void acceptStringOrInteger(@Union(types = {String.class, Integer.class}) Object o) {}

    void acceptIntegerOnly(@Union(types = {Integer.class}) Object o) {}

    // x instanceof String || x instanceof Integer narrows x to @Union({String, Integer}).
    void or(Object x) {
        if (x instanceof String || x instanceof Integer) {
            acceptStringOrInteger(x);
        }
    }

    // The equivalent if/else if chain narrows x the same way in each branch.
    void elseIf(Object x) {
        if (x instanceof String) {
            acceptStringOrInteger(x);
        } else if (x instanceof Integer) {
            acceptStringOrInteger(x);
        } else {
            // :: error: (argument)
            acceptStringOrInteger(x);
        }
    }

    // x instanceof String narrows x to @Union({String}), which is not a subtype of
    // @Union({Integer}).
    void mismatch(Object x) {
        if (x instanceof String) {
            // :: error: (argument)
            acceptIntegerOnly(x);
        }
    }
}
