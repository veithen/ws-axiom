import org.apache.axiom.checker.union.Union;

/**
 * Tests for the least upper bound of two {@code @Union} qualifiers at a conditional expression
 * ({@code UnionQualifierHierarchy#leastUpperBoundWithElements}).
 */
class ConditionalLub {

    static class A {}

    static class B {}

    void acceptAOrB(@Union(types = {A.class, B.class}) Object o) {}

    void acceptAOnly(@Union(types = {A.class}) Object o) {}

    // The lub of @Union({A}) and @Union({B}) is @Union({A, B}).
    void lub(
            boolean flag,
            @Union(types = {A.class}) Object a,
            @Union(types = {B.class}) Object b) {
        acceptAOrB(flag ? a : b);
    }

    // @Union({A, B}) is not a subtype of @Union({A}).
    void lubNotNarrower(
            boolean flag,
            @Union(types = {A.class}) Object a,
            @Union(types = {B.class}) Object b) {
        // :: error: (argument)
        acceptAOnly(flag ? a : b);
    }
}
