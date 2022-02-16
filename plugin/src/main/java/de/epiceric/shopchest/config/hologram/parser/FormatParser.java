package de.epiceric.shopchest.config.hologram.parser;

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

            final boolean isSpace = currentChar == ' ';
            final boolean isOpenParenthesis = currentChar == '(';
            final boolean isCloseParenthesis = currentChar == ')';
            final boolean isLastChar = index + 1 >= chars.length;

            // Handle specific token
            if (
                    isSpace ||
                            isOpenParenthesis ||
                            isCloseParenthesis ||
                            isLastChar
            ) {
                if (isLastChar) {
                    // Add the last char only if it's the last (otherwise it will be skipped)
                    currentToken.append(currentChar);
                }

                // If it's empty, don't need to add a token
                if (currentToken.length() == 0) {
                    continue;
                }

                final String stringToken = currentToken.toString();

                // Double detection
                try {
                    final double doubleValue = Double.parseDouble(stringToken);
                    tokens.add(new Token<>(Token.DOUBLE, doubleValue));

                    // Duplicate due to the double detection with exception

                    // Reset the specific token
                    currentToken = new StringBuilder();

                    // Unit detection
                    if (isOpenParenthesis) {
                        tokens.add(new Token<>(Token.BEGIN_UNIT, null));
                    } else if (isCloseParenthesis) {
                        tokens.add(new Token<>(Token.END_UNIT, null));
                    }
                    continue;
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
                        token = new Token<>(Token.BOOLEAN, stringToken);
                }

                tokens.add(token);

                // Reset the specific token
                currentToken = new StringBuilder();

                // Unit detection
                if (isOpenParenthesis) {
                    tokens.add(new Token<>(Token.BEGIN_UNIT, null));
                } else if (isCloseParenthesis) {
                    tokens.add(new Token<>(Token.END_UNIT, null));
                }
                continue;
            }

            // Add the char to currentToken to handle specific token
            currentToken.append(currentChar);
        }
        return tokens;
    }

}
