import org.apache.axiom.checker.union.Union;

/** Tests that {@code null} ({@code @UnionBottom}) is assignable to any {@code @Union} type. */
class NullAssignment {

    @Union(types = {String.class, Integer.class}) Object field = null;

    void acceptStringOrInteger(@Union(types = {String.class, Integer.class}) Object o) {}

    void test() {
        acceptStringOrInteger(null);
    }
}
