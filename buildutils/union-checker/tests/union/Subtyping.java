import org.apache.axiom.checker.union.Union;

/** Tests for {@code @Union} subset-based subtyping ({@code UnionQualifierHierarchy}). */
class Subtyping {

    void acceptNarrow(@Union(types = {String.class}) Object o) {}

    void acceptWide(@Union(types = {String.class, Integer.class}) Object o) {}

    // @Union({String}) is a subtype of @Union({String, Integer}).
    void narrowToWide(@Union(types = {String.class}) Object narrow) {
        acceptWide(narrow);
    }

    // @Union({String, Integer}) is not a subtype of @Union({String}).
    void wideToNarrow(@Union(types = {String.class, Integer.class}) Object wide) {
        // :: error: (argument)
        acceptNarrow(wide);
    }

    // Equal sets are subtypes of each other.
    void sameSet(@Union(types = {Integer.class, String.class}) Object wide) {
        acceptWide(wide);
    }
}
