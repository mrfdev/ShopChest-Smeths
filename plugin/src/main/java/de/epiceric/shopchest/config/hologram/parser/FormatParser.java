package de.epiceric.shopchest.config.hologram.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

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
            throw new RuntimeException("Start unit '(' without closing it");
        } else if (counter.get() < 0) {
            throw new RuntimeException("End unit ')' without starting it");
        }
        return resolvedTokens;
    }

    @SuppressWarnings("unchecked")
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
        if (nodeTokens.size() == 1) {
            final Token<?> token = nodeTokens.get(0);
            if (token.getType() == Token.NODE) {
                return (Token<List<Token<?>>>) token;
            }
        }
        return new Token<>(Token.NODE, nodeTokens);
    }

}
