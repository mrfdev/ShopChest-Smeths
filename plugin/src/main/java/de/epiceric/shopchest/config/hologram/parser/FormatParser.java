package de.epiceric.shopchest.config.hologram.parser;

import de.epiceric.shopchest.config.hologram.condition.Condition;
import de.epiceric.shopchest.config.hologram.condition.ProviderCondition;
import de.epiceric.shopchest.config.hologram.condition.ReverseCondition;
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

    @SuppressWarnings("unchecked")
    private Token<List<Token<?>>> getTypedNoteToken(Token<?> token) {
        if (token.getType() == Token.NODE) {
            return (Token<List<Token<?>>>) token;
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
                final Token<?> nextToken = nextChain.getValue();

                // Create reversed
                final Condition<?> reversed;
                final boolean isValue = nextToken.getType() == Token.VALUE;
                if (isValue || nextToken.getType() == Token.CONDITION) {
                    if (isValue) {
                        final String value = (String) nextToken.getValue();
                        final P provided = providerFunction.apply(value);
                        // It uses a valid provided value
                        if (provided != null) {
                            final Class<?> providedClass = providerTypes.get(provided);
                            // The provided value is a boolean
                            if (providedClass == Boolean.class) {
                                reversed = new ReverseCondition<>(new ProviderCondition<>(
                                        new MapProvider.BooleanMapProvider<>(provided)
                                ));
                            } else {
                                throw new RuntimeException("'" + value + "' can not be used as a boolean");
                            }
                        } else {
                            throw new RuntimeException("'" + value + "' does not exist");
                        }
                    }
                    // It's a condition
                    else {
                        reversed = new ReverseCondition<>((Condition<?>) nextToken.getValue());
                    }
                } else {
                    throw new RuntimeException("Try to reverse something that does not represent a condition");
                }

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

        return tokensChain == null ? null : tokensChain.getValue();
    }

}
