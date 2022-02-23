package de.epiceric.shopchest.config.hologram.parser;

import de.epiceric.shopchest.config.hologram.calculation.Calculation;
import de.epiceric.shopchest.config.hologram.condition.*;
import de.epiceric.shopchest.config.hologram.provider.ConditionProvider;
import de.epiceric.shopchest.config.hologram.provider.ConstantProvider;
import de.epiceric.shopchest.config.hologram.provider.MapProvider;

import java.util.*;
import java.util.function.Function;

public class FormatParser {

    public List<Token<?>> getTokens(String string) {
        final List<Token<?>> tokens = new LinkedList<>();

        final char[] chars = string.toCharArray();

        StringBuilder currentString = null;
        StringBuilder currentToken = new StringBuilder();

        for (int index = 0; index < chars.length; index++) {
            final char currentChar = chars[index];

            // String detection
            if (currentChar == '\"') {
                if (currentString != null) {
                    tokens.add(new Token<>(Token.STRING, currentString.toString()));
                    currentString = null;
                } else {
                    currentString = new StringBuilder();
                }
                continue;
            }

            // Add the char to the current string
            if (currentString != null) {
                currentString.append(currentChar);
                continue;
            }

            // Reverse detection
            if (currentChar == '!' && currentToken.length() == 0 && index + 1 < chars.length && chars[index + 1] != '=') {
                tokens.add(new Token<>(Token.REVERSE, null));
                continue;
            }

            // Unit detection
            if (currentChar == '(') {
                final Token<?> token = getToken(currentToken);
                if (token != null) {
                    tokens.add(token);
                    currentToken = new StringBuilder();
                }
                tokens.add(new Token<>(Token.BEGIN_UNIT, null));
            } else if (currentChar == ')') {
                final Token<?> token = getToken(currentToken);
                if (token != null) {
                    tokens.add(token);
                    currentToken = new StringBuilder();
                }
                tokens.add(new Token<>(Token.END_UNIT, null));
            } else if (currentChar == ' ') {
                final Token<?> token = getToken(currentToken);
                if (token != null) {
                    tokens.add(token);
                    currentToken = new StringBuilder();
                }
            } else if (index + 1 >= chars.length) {
                // Add the last char only if it's the last (otherwise it will be skipped)
                currentToken.append(currentChar);
                final Token<?> token = getToken(currentToken);
                if (token != null) {
                    tokens.add(token);
                    currentToken = new StringBuilder();
                }
            } else {
                // Add the char to currentToken to handle specific token
                currentToken.append(currentChar);
            }

        }
        return new ArrayList<>(tokens);
    }

    public Token<?> getToken(StringBuilder currentToken) {
        // If it's empty, don't need to add a token
        if (currentToken.length() == 0) {
            return null;
        }

        final String stringToken = currentToken.toString();

        // Double detection
        try {
            final double doubleValue = Double.parseDouble(stringToken);
            return new Token<>(Token.DOUBLE, doubleValue);
        } catch (Exception ignored) {
        }

        // Operator detection
        final Token<?> token;
        switch (stringToken) {
            // Calculation
            case "+":
                token = new Token<>(Token.CALCULATION_OPERATOR, Token.CalculationOperator.ADDITION);
                break;
            case "-":
                token = new Token<>(Token.CALCULATION_OPERATOR, Token.CalculationOperator.SUBTRACTION);
                break;
            case "*":
                token = new Token<>(Token.CALCULATION_OPERATOR, Token.CalculationOperator.MULTIPLICATION);
                break;
            case "/":
                token = new Token<>(Token.CALCULATION_OPERATOR, Token.CalculationOperator.DIVISION);
                break;
            case "%":
                token = new Token<>(Token.CALCULATION_OPERATOR, Token.CalculationOperator.MODULO);
                break;
            // Logic
            case "&&":
                token = new Token<>(Token.LOGIC_OPERATOR, Token.LogicOperator.AND);
                break;
            case "||":
                token = new Token<>(Token.LOGIC_OPERATOR, Token.LogicOperator.OR);
                break;
            // Condition
            case "==":
                token = new Token<>(Token.CONDITION_OPERATOR, Token.ConditionOperator.EQUAL);
                break;
            case "!=":
                token = new Token<>(Token.CONDITION_OPERATOR, Token.ConditionOperator.NOT_EQUAL);
                break;
            case ">":
                token = new Token<>(Token.CONDITION_OPERATOR, Token.ConditionOperator.GREATER);
                break;
            case "<":
                token = new Token<>(Token.CONDITION_OPERATOR, Token.ConditionOperator.LESS);
                break;
            case ">=":
                token = new Token<>(Token.CONDITION_OPERATOR, Token.ConditionOperator.GREATER_OR_EQUAL);
                break;
            case "<=":
                token = new Token<>(Token.CONDITION_OPERATOR, Token.ConditionOperator.LESS_OR_EQUAL);
                break;
            // Boolean
            default:
                token = new Token<>(Token.VALUE, stringToken);
        }

        return token;
    }

    public List<Token<?>> createNode(Iterable<Token<?>> tokens) {
        final Iterator<Token<?>> tokenIterator = tokens.iterator();
        final Counter counter = new Counter();
        final List<Token<?>> resolvedTokens = resolveNode(tokenIterator, counter).getValue();
        if (counter.get() > 0) {
            // TODO create a custom exception
            throw new RuntimeException("Start unit '(' without closing it");
        } else if (counter.get() < 0) {
            // TODO create a custom exception
            throw new RuntimeException("End unit ')' without starting it");
        }
        return resolvedTokens;
    }

    public Token<List<Token<?>>> resolveNode(Iterator<Token<?>> tokens, Counter counter) {
        final List<Token<?>> nodeTokens = new LinkedList<>();
        while (tokens.hasNext()) {
            final Token<?> token = tokens.next();
            if (token.getType() == Token.END_UNIT) {
                counter.decrement();
                break;
            } else if (token.getType() == Token.BEGIN_UNIT) {
                nodeTokens.add(resolveNode(tokens, counter.increment()));
                continue;
            }
            nodeTokens.add(token);
        }
        if (nodeTokens.isEmpty()) {
            // TODO Create a custom exception
            throw new RuntimeException("Empty unit '( )'");
        }
        // Extract if there is useless parenthesis
        if (nodeTokens.size() == 1) {
            final Token<?> token = nodeTokens.get(0);
            final Token<List<Token<?>>> typedToken = getTypedNoteToken(token);
            if (typedToken != null) {
                return typedToken;
            }
        }
        return new Token<>(Token.NODE, nodeTokens);
    }

    /**
     * Cast basically everything.
     * It's used to cast tokens that contain generic types (e.g. Node or Condition).
     *
     * @param o   A generic {@link Object} to cast
     * @param <T> The return type
     * @return The same {@link Object}, without the 'unchecked' warning
     */
    @SuppressWarnings("unchecked")
    private <T> T cast(Object o) {
        return (T) o;
    }

    private Token<List<Token<?>>> getTypedNoteToken(Token<?> token) {
        if (token.getType() == Token.NODE) {
            return cast(token);
        }
        return null;
    }

    public <P> Token<?> createFunctions(Iterable<Token<?>> tokens, Function<String, P> providerFunction, Map<P, Class<?>> providerTypes) {
        Chain<Token<?>> tokensChain = Chain.getChain(tokens);

        // Node
        Chain<Token<?>> nodeChain = tokensChain;
        while (nodeChain != null) {
            final Token<?> token = nodeChain.getValue();
            final Token<List<Token<?>>> typedToken = getTypedNoteToken(token);
            if (typedToken != null) {
                nodeChain.setValue(createFunctions(typedToken.getValue(), providerFunction, providerTypes));
            }
            nodeChain = nodeChain.getAfter();
        }

        // Reverse
        Chain<Token<?>> reverseChain = tokensChain;
        while (reverseChain != null) {
            // Reverse check
            if (reverseChain.getValue().getType() == Token.REVERSE) {
                final Chain<Token<?>> nextChain = reverseChain.getAfter();
                // Next check
                if (nextChain == null) {
                    // TODO Create custom exceptions
                    throw new RuntimeException("Try to reverse a condition that does not exist");
                }

                // Get next condition
                final Condition<Map<P, Object>> originalCondition = checkCondition(
                        nextChain.getValue(),
                        providerFunction,
                        providerTypes
                );
                if (originalCondition == null) {
                    throw new RuntimeException("Try to reverse something that does not represent a condition");
                }

                final ReverseCondition<Map<P, Object>> reversed = new ReverseCondition<>(originalCondition);

                // Set the chain
                final Chain<Token<?>> afterConditionChain = nextChain.getAfter();
                reverseChain.setValue(new Token<>(Token.CONDITION, reversed));
                if (afterConditionChain != null) {
                    afterConditionChain.setBefore(reverseChain);
                    reverseChain.setAfter(afterConditionChain);
                } else {
                    reverseChain.setAfter(null);
                }
            }

            reverseChain = reverseChain.getAfter();
        }

        // Calculation
        Chain<Token<?>> calculationChain = tokensChain;
        while (calculationChain != null) {
            final Token<?> token = calculationChain.getValue();
            // Operator check
            if (token.getType() == Token.CALCULATION_OPERATOR) {
                final Chain<Token<?>> previousChain = calculationChain.getBefore();
                final Chain<Token<?>> nextChain = calculationChain.getAfter();
                // First member does not exist
                if (previousChain == null) {
                    throw new RuntimeException("Try to apply a calculation operator without first member");
                }
                // Second member does not exist
                if (nextChain == null) {
                    throw new RuntimeException("Try to apply a calculation operator without second member");
                }
                // Get First member
                final Function<Map<P, Object>, Double> previousProvider = checkNumeric(previousChain.getValue(), providerFunction, providerTypes);
                if (previousProvider == null) {
                    throw new RuntimeException("Try to apply calculation operator on something that does not represent a number (first member)");
                }
                // Get second member
                final Function<Map<P, Object>, Double> nextProvider = checkNumeric(nextChain.getValue(), providerFunction, providerTypes);
                if (nextProvider == null) {
                    throw new RuntimeException("Try to apply calculation operator on something that does not represent a number (second member)");
                }

                // Create the calculation
                final Calculation<Map<P, Object>> calculation;
                final Token.CalculationOperator operator = (Token.CalculationOperator) token.getValue();
                switch (operator) {
                    case ADDITION:
                        calculation = new Calculation.Addition<>(previousProvider, nextProvider);
                        break;
                    case SUBTRACTION:
                        calculation = new Calculation.Subtraction<>(previousProvider, nextProvider);
                        break;
                    case MULTIPLICATION:
                        calculation = new Calculation.Multiplication<>(previousProvider, nextProvider);
                        break;
                    case DIVISION:
                        calculation = new Calculation.Division<>(previousProvider, nextProvider);
                        break;
                    case MODULO:
                        calculation = new Calculation.Modulo<>(previousProvider, nextProvider);
                        break;
                    default:
                        throw new RuntimeException("Can not figure out what is the calculation operator");
                }

                // Set the chain
                final Chain<Token<?>> afterCalculationChain = nextChain.getAfter();
                previousChain.setValue(new Token<>(Token.CALCULATION, calculation));
                if (afterCalculationChain != null) {
                    afterCalculationChain.setBefore(previousChain);
                    previousChain.setAfter(afterCalculationChain);
                } else {
                    previousChain.setAfter(null);
                }
            }
            calculationChain = calculationChain.getAfter();
        }

        // Condition operator
        Chain<Token<?>> equalityChain = tokensChain;
        while (equalityChain != null) {
            final Token<?> token = equalityChain.getValue();

            // Operator check
            if (token.getType() == Token.CONDITION_OPERATOR) {
                final Chain<Token<?>> previousChain = equalityChain.getBefore();
                final Chain<Token<?>> nextChain = equalityChain.getAfter();
                // First member does not exist
                if (previousChain == null) {
                    throw new RuntimeException("Try to apply a condition operator without first member");
                }
                // Second member does not exist
                if (nextChain == null) {
                    throw new RuntimeException("Try to apply a condition operator without second member");
                }

                // Create the condition
                final Condition<Map<P, Object>> condition;

                final Token.ConditionOperator operator = (Token.ConditionOperator) token.getValue();
                if (operator == Token.ConditionOperator.EQUAL || operator == Token.ConditionOperator.NOT_EQUAL) {
                    // Equality check (Boolean / Double / String)

                    boolean twoValues = false;
                    Token<?> firstMemberToken = previousChain.getValue();
                    Token<?> secondMemberToken = nextChain.getValue();
                    // Pre-check to facilitate the determination of the value types
                    if (firstMemberToken.getType() == Token.VALUE) {
                        if (secondMemberToken.getType() == Token.VALUE) {
                            // Comparing two values
                            twoValues = true;
                        } else {
                            // Set the constant determined value as first member (permute them)
                            final Token<?> firstMember = firstMemberToken;
                            firstMemberToken = secondMemberToken;
                            secondMemberToken = firstMember;
                        }
                    }
                    final Function<Map<P, Object>, ?> firstProvider;
                    final Class<?> firstProviderType;
                    if (twoValues) {
                        final String value = (String) firstMemberToken.getValue();
                        final P provided = providerFunction.apply(value);
                        // It uses a valid provided value
                        if (provided != null) {
                            // Create the first provider
                            final Class<?> providerClass = providerTypes.get(provided);
                            if (providerClass == Boolean.class) {
                                firstProviderType = Boolean.class;
                                firstProvider = new MapProvider.BooleanMapProvider<>(provided);
                            } else if (providerClass == Double.class) {
                                firstProviderType = Double.class;
                                firstProvider = new MapProvider.DoubleMapProvider<>(provided);
                            } else if (providerClass == String.class) {
                                firstProviderType = String.class;
                                firstProvider = new MapProvider.StringMapProvider<>(provided);
                            } else {
                                throw new RuntimeException("'" + value + "' is not a boolean, a double or a string");
                            }
                        } else {
                            throw new RuntimeException("'" + value + "' does not exist");
                        }
                    } else {
                        // Create the first provider
                        final Token.TokenType<?> type = firstMemberToken.getType();
                        if (type == Token.CONDITION) {
                            firstProviderType = Boolean.class;
                            firstProvider = new ConditionProvider<>(cast(firstMemberToken.getValue()));
                        } else if (type == Token.DOUBLE) {
                            firstProviderType = Double.class;
                            firstProvider = new ConstantProvider<>((Double) firstMemberToken.getValue());
                        } else if (type == Token.CALCULATION) {
                            firstProviderType = Double.class;
                            firstProvider = cast(firstMemberToken.getValue());
                        } else if (type == Token.STRING) {
                            firstProviderType = String.class;
                            firstProvider = new ConstantProvider<>((String) firstMemberToken.getValue());
                        } else {
                            throw new RuntimeException("Try to apply a condition operator on something that is not a boolean, a double or a string");
                        }
                    }

                    // Create the second provider
                    final Function<Map<P, Object>, ?> secondProvider;
                    // Boolean equality
                    if (firstProviderType == Boolean.class) {
                        final Condition<Map<P, Object>> secondCondition = checkCondition(secondMemberToken, providerFunction, providerTypes);
                        if (secondCondition == null) {
                            throw new RuntimeException("Try to apply a boolean equality on something that is not a condition");
                        }
                        secondProvider = new ConditionProvider<>(secondCondition);
                    }
                    // Double equality
                    else if (firstProviderType == Double.class) {
                        secondProvider = checkNumeric(secondMemberToken, providerFunction, providerTypes);
                        if (secondProvider == null) {
                            throw new RuntimeException("Try to apply a number equality on something that does not represent a number");
                        }
                    }
                    // String equality
                    else {
                        secondProvider = checkString(secondMemberToken, providerFunction, providerTypes);
                        if (secondProvider == null) {
                            throw new RuntimeException("Try to apply a string equality on something that is not a string");
                        }
                    }

                    // Create the equality condition
                    // Providers need to be cast, which is safe because we ensure before that they have the same return type
                    switch (operator) {
                        case EQUAL:
                            condition = new AbstractEqualityCondition.EqualityCondition<>(cast(firstProvider), cast(secondProvider));
                            break;
                        case NOT_EQUAL:
                            condition = new AbstractEqualityCondition.InequalityCondition<>(cast(firstProvider), cast(secondProvider));
                            break;
                        default:
                            throw new RuntimeException("Can not figure out what is the condition operator");
                    }
                } else {
                    // Relative check (Double)

                    // Get First member
                    final Function<Map<P, Object>, Double> previousProvider = checkNumeric(previousChain.getValue(), providerFunction, providerTypes);
                    if (previousProvider == null) {
                        throw new RuntimeException("Try to apply relative condition operator on something that does not represent a number (first member)");
                    }
                    // Get second member
                    final Function<Map<P, Object>, Double> nextProvider = checkNumeric(nextChain.getValue(), providerFunction, providerTypes);
                    if (nextProvider == null) {
                        throw new RuntimeException("Try to apply relative condition operator on something that does not represent a number (second member)");
                    }

                    // Create the relative condition
                    switch (operator) {
                        case GREATER:
                            condition = new ComparisonCondition.GreaterCondition<>(previousProvider, nextProvider);
                            break;
                        case GREATER_OR_EQUAL:
                            condition = new ComparisonCondition.GreaterOrEqualCondition<>(previousProvider, nextProvider);
                            break;
                        case LESS:
                            condition = new ComparisonCondition.LessCondition<>(previousProvider, nextProvider);
                            break;
                        case LESS_OR_EQUAL:
                            condition = new ComparisonCondition.LessOrEqualCondition<>(previousProvider, nextProvider);
                            break;
                        default:
                            throw new RuntimeException("Can not figure out what is the condition operator");
                    }
                }

                // Set the chain
                final Chain<Token<?>> afterCalculationChain = nextChain.getAfter();
                previousChain.setValue(new Token<>(Token.CONDITION, condition));
                if (afterCalculationChain != null) {
                    afterCalculationChain.setBefore(previousChain);
                    previousChain.setAfter(afterCalculationChain);
                } else {
                    previousChain.setAfter(null);
                }
            }

            equalityChain = equalityChain.getAfter();
        }

        return tokensChain == null ? null : tokensChain.getValue();
    }

    /**
     * Get the condition of this chain
     *
     * @param token            The {@link Token} that need to be analyzed
     * @param providerFunction The mapping function to associate a value to his provider key
     * @param providerTypes    The types of the provider keys
     * @param <P>              The provider type
     * @return A {@link Condition} contained in this {@link Chain}. {@code null} if it's not a condition.
     */
    private <P> Condition<Map<P, Object>> checkCondition(
            Token<?> token,
            Function<String, P> providerFunction,
            Map<P, Class<?>> providerTypes
    ) {
        // Check if it's boolean value
        if (token.getType() == Token.VALUE) {
            final String value = (String) token.getValue();
            final P provided = providerFunction.apply(value);
            // It uses a valid provided value
            if (provided != null) {
                final Class<?> providedClass = providerTypes.get(provided);
                // The provided value is a boolean
                if (providedClass == Boolean.class) {
                    return new ProviderCondition<>(
                            new MapProvider.BooleanMapProvider<>(provided)
                    );
                } else {
                    throw new RuntimeException("'" + value + "' can not be used as a boolean");
                }
            } else {
                throw new RuntimeException("'" + value + "' does not exist");
            }
        } else if (token.getType() == Token.CONDITION) {
            // Check if condition and extract it
            return cast(token.getValue());
        }
        return null;
    }

    /**
     * Get the number or calculation of this chain
     *
     * @param token            The {@link Token} that need to be analyzed
     * @param providerFunction The mapping function to associate a value to his provider key
     * @param providerTypes    The types of the provider keys
     * @param <P>              The provider type
     * @return A {@link Function} that generate a {@link Double} contained in this {@link Chain}. {@code null} if it's not a {@link Function} that generate a {@link Double}.
     */
    private <P> Function<Map<P, Object>, Double> checkNumeric(
            Token<?> token,
            Function<String, P> providerFunction,
            Map<P, Class<?>> providerTypes
    ) {
        if (token.getType() == Token.VALUE) {
            final String value = (String) token.getValue();
            final P provided = providerFunction.apply(value);
            // It uses a valid provided value
            if (provided != null) {
                final Class<?> providedClass = providerTypes.get(provided);
                // The provided value is a number
                if (providedClass == Double.class) {
                    // Return the provided key
                    return new MapProvider.DoubleMapProvider<>(provided);
                } else {
                    throw new RuntimeException("'" + value + "' can not be used as a number");
                }
            } else {
                throw new RuntimeException("'" + value + "' does not exist");
            }
        } else if (token.getType() == Token.CALCULATION) {
            return cast(token.getValue());
        } else if (token.getType() == Token.DOUBLE) {
            return new ConstantProvider<>((Double) token.getValue());
        }

        return null;
    }

    /**
     * Get the {@link String} of this chain
     *
     * @param token            The {@link Token} that need to be analyzed
     * @param providerFunction The mapping function to associate a value to his provider key
     * @param providerTypes    The types of the provider keys
     * @param <P>              The provider type
     * @return A {@link Function} that generate a {@link String} contained in this {@link Chain}. {@code null} if it's not a {@link Function} that generate a {@link String}.
     */
    private <P> Function<Map<P, Object>, String> checkString(
            Token<?> token,
            Function<String, P> providerFunction,
            Map<P, Class<?>> providerTypes
    ) {
        if (token.getType() == Token.VALUE) {
            final String value = (String) token.getValue();
            final P provided = providerFunction.apply(value);
            // It uses a valid provided value
            if (provided != null) {
                final Class<?> providedClass = providerTypes.get(provided);
                // The provided value is a number
                if (providedClass == String.class) {
                    // Return the provided key
                    return new MapProvider.StringMapProvider<>(provided);
                } else {
                    throw new RuntimeException("'" + value + "' can not be used as a string");
                }
            } else {
                throw new RuntimeException("'" + value + "' does not exist");
            }
        } else if (token.getType() == Token.STRING) {
            return new ConstantProvider<>((String) token.getValue());
        }

        return null;
    }

}
