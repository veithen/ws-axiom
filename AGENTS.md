# AI Agent Instructions

## Code formatting

This project uses the [spotless-maven-plugin](https://github.com/diffplug/spotless) with `palantirJavaFormat` to enforce consistent Java formatting. **After modifying any Java source files, you MUST run spotless to reformat the code:**

```
./mvnw -pl <module> spotless:apply
```

Replace `<module>` with the Maven module path(s) you modified (e.g. `axiom-api`, `implementations/axiom-impl`). To apply across all modules at once, omit `-pl`:

```
./mvnw spotless:apply
```

To verify formatting without modifying files (mirrors what CI checks):

```
./mvnw -pl <module> spotless:check
```

**CI will fail if spotless has not been applied.** Do not consider any change complete until `spotless:check` passes for the modified modules.
