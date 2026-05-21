# Consolidate Matrix Tests

## Purpose

This skill describes how to consolidate multiple single-method matrix test classes (each
implementing `Executable`) into a single multi-method class annotated with JUnit 5 `@Test`
methods. Use this when several closely-related test classes in the same package share the
same injected dependencies and logically belong together (e.g., they all test the same DOM
node type or API surface).

## When to consolidate

Consolidation is a good fit when:

- Multiple `Executable` test classes live in the same package.
- All of them inject the exact same set of fields (e.g., only `DocumentBuilderFactory`).
- Their names follow a pattern like `TestFooBar`, `TestFooBaz`, `TestFooQux` — all prefixed
  with the same noun, indicating they test the same feature area.
- None of them has parameters driven by a `FanOutNode` (i.e., they appear as plain
  `new MatrixTest(TestFoo.class)` leaf nodes with no surrounding fan-out).

Do **not** consolidate when:

- The tests have different injected dependencies (consolidation would add unnecessary fields).
- One or more of the test classes is used in a `MatrixTestFilters` exclusion in any consumer.
  After consolidation, filters can only exclude the whole class (all methods), not individual
  methods. Check all `MatrixTestFilters.builder()` usages in the repo before proceeding.
- The tests are wrapped in a `FanOutNode` — they rely on dimension-specific injected values
  and the fan-out supplies separate display names / labels per test case.

## How MatrixTest handles multi-method classes

`MatrixTest` detects at runtime whether the supplied class implements `Executable`:

- **`Executable` class → single `DynamicTest`** named after the class.
- **Plain class with `@Test` methods → `DynamicContainer`** named after the class, containing
  one `DynamicTest` per method. Methods are sorted alphabetically; a fresh Guice-injected
  instance is created for each method invocation.

## Step-by-step consolidation process

### 1. Identify the target group

Find all `Executable` test classes in a package that share the same injected fields and have
no cross-cutting filter entries. Example: a `documentfragment` package containing
`TestCloneNodeDeep`, `TestCloneNodeShallow`, `TestLookupNamespaceURI`, `TestLookupPrefix`.

### 2. Verify no filters reference the old classes

Search across the entire codebase for `MatrixTestFilters` usages that reference any of the
target classes. If any consumer excludes one of the old classes individually, you must either:
- Keep that class as a standalone `Executable` test (don't consolidate it), or
- Migrate the filter to the new class name (accepting that the whole class is excluded).

```bash
grep -rn "TestCloneNodeDeep\|TestLookupPrefix" --include="*.java" .
```

### 3. Create the consolidated test class

Create a new class in the same package. Follow this naming convention:
- Use the shared noun/area as the class name prefix, e.g., `DocumentFragmentTests`.
- The class name should end with `Tests` (plural) to distinguish it from single-method
  `Executable` test classes which typically end with no suffix or a singular noun.

Structure of the new class:

```java
/** Tests for {@link SomeType}. */
public class SomeTypeTests {
    // Declare only the fields that were @Inject-ed in the old classes
    @Inject
    private SomeDependency dep;

    /** One-line description of what this method tests. */
    @Test
    public void descriptiveMethodName() throws Throwable {
        // body from the old execute() method
    }

    // ... one @Test method per old class
}
```

Method naming conventions:
- Drop the `Test` prefix from the old class name and lowercase the first letter.
  - `TestCloneNodeDeep` → `cloneNodeDeep()`
  - `TestLookupNamespaceURI` → `lookupNamespaceURI()`
- Methods should be `public void` and may declare `throws Throwable`.
- Keep the original Javadoc comment from the old class on the corresponding method.

### 4. Update the test suite registration

In the suite factory class (e.g., `DOMTestSuite`), replace the N individual `MatrixTest`
entries with a single entry:

```java
// Before
new MatrixTest(org.example.documentfragment.TestCloneNodeDeep.class),
new MatrixTest(org.example.documentfragment.TestCloneNodeShallow.class),
new MatrixTest(org.example.documentfragment.TestLookupNamespaceURI.class),
new MatrixTest(org.example.documentfragment.TestLookupPrefix.class),

// After
new MatrixTest(org.example.documentfragment.DocumentFragmentTests.class),
```

### 5. Delete the old files

Remove all the individual `Executable` test files that were consolidated.

### 6. Build and test

Run the affected module(s) to confirm no regressions:

```bash
./mvnw -pl <affected-modules> -am test
```

For changes to `testing/dom-testsuite` or `testing/matrix-testsuite`, run:

```bash
./mvnw -pl testing/matrix-testsuite,testing/dom-testsuite -am test
```

## Formatting

The project uses `spotless-maven-plugin` with `palantirJavaFormat`. After editing, run:

```bash
./mvnw -pl <module> spotless:apply
```

or let the build fail on the first attempt and re-run after applying the formatter.
