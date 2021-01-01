package io.github.ricoapon.readableregex;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static io.github.ricoapon.readableregex.Constants.*;
import static io.github.ricoapon.readableregex.ReadableRegex.regex;
import static io.github.ricoapon.readableregex.matchers.PatternMatchMatcher.doesntMatchAnythingFrom;
import static io.github.ricoapon.readableregex.matchers.PatternMatchMatcher.doesntMatchExactly;
import static io.github.ricoapon.readableregex.matchers.PatternMatchMatcher.matchesExactly;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests related to methods that are inside {@link StandaloneBlockBuilder}.
 */
@SuppressFBWarnings(value = "SIC_INNER_SHOULD_BE_STATIC", justification = "@Nested classes should be non-static, but SpotBugs wants them static." +
        "See https://github.com/spotbugs/spotbugs/issues/560 for the bug (open since 2018).")
class StandaloneBlockTests {
    @Nested
    class RegexFromString {
        @Test
        void nullAsArgumentThrowsNpe() {
            assertThrows(NullPointerException.class, () -> regex().regexFromString(null));
        }

        @Test
        void quantifierIsPossibleAfter() {
            ReadableRegexPattern pattern = regex().digit().oneOrMore().regexFromString("\\s+").oneOrMore().digit().build();

            assertThat(pattern, matchesExactly("111   2"));
        }
    }

    @Nested
    class Add {
        @Test
        void nullAsArgumentThrowsNpe() {
            assertThrows(NullPointerException.class, () -> regex().add(null));
        }

        @Test
        void otherBuildersAreAddedAsStandaloneBlock() {
            ReadableRegexPattern pattern = regex().digit().add(regex().whitespace()).oneOrMore().digit().build();

            assertThat(pattern, matchesExactly("1   2"));
            assertThat(pattern, doesntMatchExactly("1 1 2"));
        }
    }

    @Nested
    class Digits {
        @Test
        void digitOnlyMatchesDigits() {
            ReadableRegexPattern pattern = regex().digit().build();

            // Matches exactly every character inside DIGITS.
            for (String digit : DIGITS.split("")) {
                assertThat(pattern, matchesExactly(digit));
            }
            assertThat(pattern, doesntMatchAnythingFrom(WORD_CHARACTERS));
            assertThat(pattern, doesntMatchAnythingFrom(NON_LETTERS));
            assertThat(pattern, doesntMatchAnythingFrom(WHITESPACES));
        }

        @Test
        void digitsAreStandaloneBlocks() {
            ReadableRegexPattern pattern = regex().literal("a").digit().oneOrMore().build();

            assertThat(pattern, matchesExactly("a" + DIGITS));
            assertThat(pattern, doesntMatchExactly("a1a1"));
        }
    }

    @Nested
    class Literals {
        @Test
        void nullAsArgumentThrowsNpe() {
            assertThrows(NullPointerException.class, () -> regex().literal(null));
        }

        @Test
        void literalCharactersAreEscaped() {
            ReadableRegexPattern pattern = regex()
                    .literal("a.()[]\\/|?.+*")
                    .build();

            assertThat(pattern, matchesExactly("a.()[]\\/|?.+*"));
        }

        @Test
        void literalCanBeCombinedWithMetaCharacters() {
            ReadableRegexPattern pattern = regex()
                    .literal(".").digit().whitespace().literal("*")
                    .build();

            assertThat(pattern, matchesExactly(".1 *"));
            assertThat(pattern, matchesExactly(".2\t*"));
            assertThat(pattern, doesntMatchExactly("a1 *"));
        }

        @Test
        void literalsAreStandaloneBlocks() {
            ReadableRegexPattern pattern = regex().digit().literal("a").oneOrMore().build();

            assertThat(pattern, matchesExactly("1aaaa"));
            assertThat(pattern, doesntMatchExactly("1a1a"));
        }
    }

    @Nested
    class Whitespaces {
        @Test
        void whitespacesOnlyMatchWhitespaces() {
            ReadableRegexPattern pattern = regex().whitespace().build();

            // Matches exactly every character inside WHITESPACES.
            for (String digit : WHITESPACES.split("")) {
                assertThat(pattern, matchesExactly(digit));
            }
            assertThat(pattern, matchesExactly(WHITESPACES.substring(0, 1)));
            assertThat(pattern, doesntMatchAnythingFrom(WORD_CHARACTERS));
            assertThat(pattern, doesntMatchAnythingFrom(NON_LETTERS));
            assertThat(pattern, doesntMatchAnythingFrom(DIGITS));
        }

        @Test
        void whitespacesAreStandaloneBlocks() {
            ReadableRegexPattern pattern = regex().literal("a").whitespace().oneOrMore().build();

            assertThat(pattern, matchesExactly("a" + WHITESPACES));
            assertThat(pattern, doesntMatchExactly("a a "));
        }
    }

    @Nested
    class OneOf {
        @Test
        void oneOfCanBeUsedWithZeroOrOneArguments() {
            ReadableRegexPattern pattern = regex().oneOf().oneOf(regex().literal("b")).build();

            assertThat(pattern, matchesExactly("b"));
        }

        @Test
        void oneOfCanBeUsedWithMultipleArguments() {
            ReadableRegexPattern pattern = regex().oneOf(regex().literal("b"), regex().literal("c")).build();

            assertThat(pattern, matchesExactly("b"));
            assertThat(pattern, matchesExactly("c"));
            assertThat(pattern, doesntMatchAnythingFrom(""));
        }

        @Test
        void oneOfAreStandaloneBlocks() {
            ReadableRegexPattern pattern = regex().literal("a").oneOf(regex().literal("b")).optional().build();

            assertThat(pattern, matchesExactly("a"));
            assertThat(pattern, matchesExactly("ab"));
            assertThat(pattern, doesntMatchAnythingFrom(""));
        }
    }
}