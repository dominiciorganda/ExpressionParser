/*
    EBNF RULES
    1) Expr -> Term, ER
    2) ER -> [ "+", Term, ER ]
    3)      | [  '-", Term, ER ]
    4) Term -> Factor, TR
    5) TR -> [ "*", Factor, TR ]
    6)      | [ "/", Factor, TR ]
    7) Factor -> number
 */


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class ExpressionParser {

    static List<Character> alphabet = new ArrayList<>();
    static List<Character> numericAlphabet = new ArrayList<>();
    static List<Character> operatorsAlphabet = new ArrayList<>();
    static String currentString;
    static List<Token> tokens = new ArrayList<>();
    static int position = 0;
    static boolean lexicalError = false;

    private static void generateAlphabet() {
        for (char c = '0'; c != '9'; c++)
            numericAlphabet.add(c);
        numericAlphabet.add('9');
        operatorsAlphabet.add('+');
        operatorsAlphabet.add('-');
        operatorsAlphabet.add('*');
        operatorsAlphabet.add('/');
        alphabet.addAll(numericAlphabet);
        alphabet.addAll(operatorsAlphabet);
    }

    private static String transition(String currentState, char symbol) {
        if (!alphabet.contains(symbol))
            return "error";
        if (currentState.equals("state0")) {
            if (numericAlphabet.contains(symbol) && symbol != '0')
                return "state1";
            if (symbol == '0')
                return "state2";
            if (operatorsAlphabet.contains(symbol))
                return "state3";
            return "error";
        }
        if (currentState.equals("state1")) {
            if (numericAlphabet.contains(symbol))
                return "state1";
            return "error";
        }
        return "error";
    }

    enum Type {
        Number, Operator
    }


    static class Token {
        String value;
        Type type;


        public Token(String value, Type type) {
            this.value = value;
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Type getType() {
            return type;
        }

        public void setType(Type type) {
            this.type = type;
        }
    }

    static class SyntaxError extends Exception {
        private String token;
        private int position;

        public SyntaxError(String token, int position) {
            this.token = token;
            this.position = position;
        }

        public void showMessage() {
            System.out.println("Syntax error not allowed token: " + token + " at position " + (position + 1));
        }
    }

    public static double Expr() throws SyntaxError {
        // EBNF Rule 1
        //        System.out.println("EXPR");
        return Term() + ER();
    }

    public static double Term() throws SyntaxError {
        // EBNF Rule 4
//        System.out.println("TERM");
        return Factor() * TR();
    }

    public static double Factor() throws SyntaxError {
        // EBNF Rule 7
//        System.out.println("FACTOR");
        Token current = tokens.get(position);
        if (current.getType() == Type.Number) {
            position++;
            return Integer.parseInt(current.getValue());
        } else {            // syntax error
            throw new SyntaxError(current.getValue(), position);
        }
    }

    public static double ER() throws SyntaxError {
//        System.out.println("ER");
        Token current = tokens.get(position);
        if (current.getValue().equals("+") || current.getValue().equals("-")) {
            position++;
            if (current.getValue().equals("+"))
                return Term() + ER();       // EBNF Rule 2
            return -Term() + ER();          // EBNF Rule 3
        } else if (current.getValue().equals("eof")) {      // EBNF epsilon
            return 0;
        } else if (current.getType() == Type.Number) {      // syntax error
            throw new SyntaxError(current.getValue(), position);
        }
        return 0;       // EBNF (not an error? -> epsilon)
    }

    public static double TR() throws SyntaxError {
//        System.out.println("TR");
        Token current = tokens.get(position);
        if (current.getValue().equals("*") || current.getValue().equals("/")) {
            position++;
            if (current.getValue().equals("*"))
                return Factor() * TR();         // EBNF Rule 5
            return (1 / Factor()) * TR();       // EBNF Rule 6
        } else if (current.getValue().equals("eof")) {      // EBNF epsilon
            return 1;
        } else if (current.getType() == Type.Number) {      // syntax error
            throw new SyntaxError(current.getValue(), position);
        }
        return 1;       // EBNF (not an error? -> epsilon)
    }

    public static void main(String[] args) {
        System.out.println("Taste eine arithmetischen Ausdruck");
        Scanner scanner = new Scanner(System.in);
        List<String> states = Arrays.asList("state0", "state1", "state2", "state3");
        List<String> finalStates = Arrays.asList("state1", "state2", "state3");
        generateAlphabet();
        String line = scanner.nextLine();
        String currentState = states.get(0);
        currentString = "";
        char[] input = line.toCharArray();
        for (int i = 0; i < line.length(); i++) {
            char symbol = input[i];

            if (transition(currentState, symbol).equals("error")) {
                checkTokenOrError(finalStates, currentState, i);
                currentState = states.get(0);
                currentString = "";
            }

            currentState = transition(currentState, symbol);
            currentString += String.valueOf(symbol);

        }
        checkTokenOrError(finalStates, currentState, line.length());
        tokens.add(new Token("eof", null));


//        for (Token token : tokens)
//            System.out.println(token.getValue() + " " + token.getType());
//        System.out.println();
//        System.out.println();

        if (!lexicalError) {
            try {
                double value = Expr();
                System.out.println("Value of expression = " + value);
            } catch (SyntaxError syntaxError) {
                syntaxError.showMessage();
            }
        }
        scanner.nextLine();
        scanner.close();
    }

    private static void checkTokenOrError(List<String> finalStates, String currentState, int position) {
        if (finalStates.contains(currentState)) {
            if (currentState.equals("state3"))
                tokens.add(new Token(currentString, Type.Operator));
            else
                tokens.add(new Token(currentString, Type.Number));
        } else {
            //lexical error
            if (!currentString.equals(" ") && !currentString.isEmpty() && !currentString.equals("\t")) {
                lexicalError = true;
                System.out.println("Lexical Error at position " + position + " illegal character: " + currentString);
            }
        }
    }
}
