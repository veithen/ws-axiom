import org.apache.axiom.checker.union.Union;

/**
 * Tests for the {@code @UnknownUnion} escape rule, which allows an unconstrained value to be used
 * where {@code @Union(types = {...})} is expected if its erased type is a subtype of one of the
 * union members ({@code UnionTypeHierarchy}).
 */
class UnknownUnionEscape {

    void acceptStringOrCharSequence(@Union(types = {String.class, CharSequence.class}) Object o) {}

    void acceptIntegerOnly(@Union(types = {Integer.class}) Object o) {}

    // String is itself one of the union members.
    void exactMember(String s) {
        acceptStringOrCharSequence(s);
    }

    // StringBuilder is a subtype of CharSequence, a union member.
    void subtypeOfMember(StringBuilder sb) {
        acceptStringOrCharSequence(sb);
    }

    // StringBuilder is not a subtype of Integer, the only union member.
    void notAMember(StringBuilder sb) {
        // :: error: (argument)
        acceptIntegerOnly(sb);
    }
}
