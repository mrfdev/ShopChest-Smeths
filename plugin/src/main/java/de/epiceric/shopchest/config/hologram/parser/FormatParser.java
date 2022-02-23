package de.epiceric.shopchest.config.hologram.parser;

import de.epiceric.shopchest.config.hologram.calculation.Calculation;
import de.epiceric.shopchest.config.hologram.condition.Condition;
import de.epiceric.shopchest.config.hologram.condition.ProviderCondition;
import de.epiceric.shopchest.config.hologram.condition.ReverseCondition;
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
                        nextChain,
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
                final Function<Map<P, Object>, Double> previousProvider = checkNumeric(previousChain, providerFunction, providerTypes);
                if (previousProvider == null) {
                    throw new RuntimeException("Try to apply calculation operator on something that does not represent a number (first member)");
                }
                // Get second member
                final Function<Map<P, Object>, Double> nextProvider = checkNumeric(nextChain, providerFunction, providerTypes);
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

        // Equality check

        return tokensChain == null ? null : tokensChain.getValue();
    }

    /**
     * Get the condition of this chain
     *
     * @param chain            The {@link Chain} that need to be analyzed
     * @param providerFunction The mapping function to associate a value to his provider key
     * @param providerTypes    The types of the provider keys
     * @param <P>              The provider type
     * @return A {@link Condition} contained in this {@link Chain}. {@code null} if it's not a condition.
     */
    private <P> Condition<Map<P, Object>> checkCondition(
            Chain<Token<?>> chain,
            Function<String, P> providerFunction,
            Map<P, Class<?>> providerTypes
    ) {
        final Token<?> token = chain.getValue();

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
     * @param chain            The {@link Chain} that need to be analyzed
     * @param providerFunction The mapping function to associate a value to his provider key
     * @param providerTypes    The types of the provider keys
     * @param <P>              The provider type
     * @return A {@link Function} that generate a {@link Double} contained in this {@link Chain}. {@code null} if it's not a {@link Function} that generate a {@link Double}.
     */
    private <P> Function<Map<P, Object>, Double> checkNumeric(
            Chain<Token<?>> chain,
            Function<String, P> providerFunction,
            Map<P, Class<?>> providerTypes
    ) {
        final Token<?> token = chain.getValue();
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
     * @param chain            The {@link Chain} that need to be analyzed
     * @param providerFunction The mapping function to associate a value to his provider key
     * @param providerTypes    The types of the provider keys
     * @param <P>              The provider type
     * @return A {@link Function} that generate a {@link String} contained in this {@link Chain}. {@code null} if it's not a {@link Function} that generate a {@link String}.
     */
    private <P> Function<Map<P, Object>, String> checkString(
            Chain<Token<?>> chain,
            Function<String, P> providerFunction,
            Map<P, Class<?>> providerTypes
    ) {
        final Token<?> token = chain.getValue();
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
