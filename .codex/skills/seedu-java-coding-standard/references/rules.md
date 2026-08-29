# Java coding-standard checklist

Primary source: [SE-EDU Java coding standard (basic + intermediate)](https://se-education.org/guides/conventions/java/intermediate.html)

Fallback source for topics not covered below: [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)

The SE-EDU rules override the fallback guide when they differ. In particular, use 4-space block indentation, 8 additional spaces for wrapped lines, a 110-character soft line limit, and a 120-character hard line limit.

## Naming

- Write package names in lowercase. Use `athena` as this project's root package, followed by logical subpackages.
- Name classes and enums with English nouns in PascalCase.
- Name variables in camelCase and constants in SCREAMING_SNAKE_CASE.
- Name methods with English verbs or verb phrases in camelCase.
- Test methods may use `featureUnderTest_testScenario_expectedBehavior()`. The scenario, expected behavior, or both may be omitted when the shorter name still describes the test coverage.
- Treat abbreviations and acronyms as ordinary words inside names, such as `exportHtmlSource` and `openDvdPlayer`.
- Use English names. Give wide-scope variables descriptive names; reserve short names for small-scope scratch values. Use `i` for an ordinary loop index and `j`, `k`, and later letters only for nested loops.
- Make boolean variable and method names read like booleans. Prefer prefixes such as `is`, `has`, `was`, `can`, and `should`. A boolean setter takes the corresponding boolean name, such as `setFound(boolean isFound)`.
- Use plural names for collections and arrays.
- Give related constants a shared prefix.

## Layout

- Indent blocks with 4 spaces, never tabs.
- Aim for at most 110 characters per line and never exceed 120 characters. Imports, package declarations, and literal content that cannot sensibly be wrapped are the only practical exceptions.
- Indent wrapped lines 8 spaces beyond their parent line. Break after commas and before operators, including `.`, `&` in type bounds, and `|` in multi-catch clauses. Keep a method or constructor name attached to its opening parenthesis and prefer higher-level breaks.
- Use K&R braces. Put the opening brace on the declaration or control-statement line and the closing brace on its own line. Write `} else {`, `} catch (...) {`, and `} finally {` on one line.
- Always use braces around `if`, `else`, `for`, `while`, and `do` bodies. Put a conditional and its body on separate lines.
- Indent `case` and `default` labels one level inside a `switch`, and their statements one further level. Mark intentional old-style switch fallthrough with `// Fallthrough`; no comment is needed when control exits with `break`, `return`, `continue`, or `throw`.
- Put spaces around binary and ternary operators, after commas and semicolons, and between control-flow keywords and `(`. Do not insert spaces around method-call parentheses or the dot operator.
- Separate logical units within a block with one blank line. Avoid multiple blank lines that do not communicate structure.

## Source structure and statements

- Put every class in a package and keep one top-level class per source file.
- List every imported type explicitly; never use wildcard imports.
- Keep imports consistent with the existing project order: non-static application and third-party imports in one ASCII-sorted block, Java library imports in a second ASCII-sorted block, and static imports in a final ASCII-sorted block. Separate present blocks with one blank line.
- Attach array brackets to the type, such as `String[] arguments`, not to the variable.
- Declare one variable per declaration. Initialize variables where declared when a real initial value is available, declare them close to first use, and keep them in the smallest practical scope.
- Do not expose public class variables unless they are constants or fields of a behavior-free data class.

## Comments and Javadoc

- Write comments in English using American spelling and no local slang. Indent comments with the surrounding code.
- Write ordinary single-line comments as `//Text`, with no space after `//`. Use sentence case and omit terminal punctuation for a short phrase, for example `//Do nothing`. Keep the explicit SE-EDU switch marker as `// Fallthrough`.
- Write descriptive Javadoc for every public class, constructor, and method. Javadoc may be omitted for straightforward getters and setters, exact overrides whose inherited documentation applies unchanged, and test classes or methods.
- Start a method or constructor summary with a third-person verb such as `Returns`, `Adds`, `Sends`, or `Constructs`. Keep the first sentence short because Javadoc uses it as the summary.
- Put `/**` and `*/` on their own lines for normal Javadoc blocks. Align each `*`, put one space after it, and place no blank line between the block and the declaration. A simple class-member description may use one line; omit terminal punctuation when it does, for example `/** Number of connections */`.
- Separate the description from block tags with one blank Javadoc line. End every `@param`, `@return`, and `@throws` description with punctuation.
- Include `@param` for every parameter or omit all of them when every name is self-explanatory and the main description already explains them. Omit `@return` for `void` methods or when it adds no information.
- Use `{@inheritDoc}` when an override needs to retain parent documentation while documenting a meaningful difference.
