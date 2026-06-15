import org.apache.axiom.checker.union.Union;

/**
 * Tests for promoting the type of a conditional expression to {@code @Union(types = {...})} when
 * one branch is {@code @UnknownUnion} but its erased type is a subtype of a union member
 * ({@code UnionTreeAnnotator}).
 */
class ConditionalPromotion {

    // sb has erased type StringBuilder, a subtype of CharSequence, so the conditional
    // expression is promoted to @Union({String, CharSequence}), regardless of which branch it
    // appears in.
    @Union(types = {String.class, CharSequence.class}) Object unknownSecond(
            boolean flag,
            @Union(types = {String.class, CharSequence.class}) Object data, StringBuilder sb) {
        return flag ? sb : data;
    }

    @Union(types = {String.class, CharSequence.class}) Object unknownFirst(
            boolean flag,
            @Union(types = {String.class, CharSequence.class}) Object data, StringBuilder sb) {
        return flag ? data : sb;
    }

    // other has erased type Object, which is not a subtype of String or CharSequence, so no
    // promotion happens and the conditional expression's type remains @UnknownUnion, which is not
    // a subtype of the declared @Union return type.
    @Union(types = {String.class, CharSequence.class}) Object noPromotion(
            boolean flag, @Union(types = {String.class, CharSequence.class}) Object data, Object other) {
        // :: error: (return)
        return flag ? other : data;
    }
}
