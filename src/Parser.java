import java.util.List;
import java.util.ArrayList;

public class Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Object> parse() {
        List<Object> declarations = new ArrayList<>();
        while (!isAtEnd()) {
            Object decl = declaration();
            if (decl != null) {
                declarations.add(decl);
            }
        }
        return declarations;
    }

    // Top of the grammar: Declaration -> varDecl | statement ; 
    private Object declaration() {
        try {
            if (match(TokenType.VAR)) {
                return varDeclaration();
            }
            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Object statement() {
        try {
            if (match(TokenType.PRINT)) {
                Object value = expression();
                consume(TokenType.SEMICOLON, "Expect ';' after value.");
                return new PrintStatement(value);
            }

            if (match(TokenType.LEFT_BRACE)) {
                return new Block(block());
            }

            return assignmentOrExpressionStatement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    //block -> "{" declaration* "}" ;
    private List<Object> block() {
        List<Object> declarations = new ArrayList<>();

        while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
            Object decl = declaration();
            if (decl != null) {
                declarations.add(decl);
            }
        }

        consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
        return declarations;
    }

    // Parses: var IDENTIFIER ( "=" expression )? ";"
    private Object varDeclaration() {
        Token name = consume(TokenType.IDENTIFIER, "Expect variable name.");

        Object initializer = null;
        if (match(TokenType.EQUAL)) {
            initializer = expression();
        }

        consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
        return new VarDecl(name.getLexeme(), initializer);
    }

    // Parses either VarAssign or ExpressionStatement
    private Object assignmentOrExpressionStatement() {
        Object expr = expression();

        // Check if there is an '=' afterwards
        if (match(TokenType.EQUAL)) {
            Token equals = previous();
            Object value = expression(); // Parse the right-hand side value

            // Check if the left-hand side was just an IDENTIFIER (Variable node)
            if (expr instanceof Variable) {
                String name = ((Variable) expr).name.getLexeme();
                consume(TokenType.SEMICOLON, "Expect ';' after variable assignment.");
                return new VarAssign(name, value);
            }

            // If it was a general expression with an '=' after it, report a syntax error
            error(equals, "Invalid assignment target.");
        }

        // If it is an expression without '=', treat it as a normal ExpressionStatement
        consume(TokenType.SEMICOLON, "Expect ';' after expression.");
        return new ExpressionStatement(expr);
    }

    // expression -> equality ;
    private Object expression() {
        return equality();
    }

    // equality -> comparison ( ( "!=" | "==" ) comparison )* ;
    private Object equality() {
        Object expr = comparison();

        while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
            Token operator = previous();
            Object right = comparison();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    // comparison -> term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
    private Object comparison() {
        Object expr = term();

        while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
            Token operator = previous();
            Object right = term();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    // term -> factor ( ( "-" | "+" ) factor )* ;
    private Object term() {
        Object expr = factor();

        while (match(TokenType.MINUS, TokenType.PLUS)) {
            Token operator = previous();
            Object right = factor();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    // factor -> unary ( ( "/" | "*" ) unary )* ;
    private Object factor() {
        Object expr = unary();

        while (match(TokenType.SLASH, TokenType.STAR)) {
            Token operator = previous();
            Object right = unary();
            expr = new Binary(expr, operator, right);
        }

        return expr;
    }

    // unary -> ( "!" | "-" ) unary | primary ;
    private Object unary() {
        if (match(TokenType.BANG, TokenType.MINUS)) {
            Token operator = previous();
            Object right = unary();
            return new Unary(operator, right);
        }

        return primary();
    }

    // primary -> NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
    private Object primary() {
        if (match(TokenType.FALSE))
            return new Literal("false");
        if (match(TokenType.TRUE))
            return new Literal("true");
        if (match(TokenType.NIL))
            return new Literal("nil");

        if (match(TokenType.NUMBER, TokenType.STRING)) {
            return new Literal(previous().getLiteralValue());
        }

        // Handle variables/identifiers
        if (match(TokenType.IDENTIFIER)) {
            return new Variable(previous());
        }

        if (match(TokenType.LEFT_PAREN)) {
            Object expr = expression();
            consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
            return new Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // Helper Utility Methods

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type))
            return advance();
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek().getType() == type;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return previous();
    }

    private boolean isAtEnd() {
        return peek().getType() == TokenType.EOF;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    // Synchronization method to recover from errors
    // Method to be added to as more is added to the language
    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().getType() == TokenType.SEMICOLON)
                return;

            switch (peek().getType()) {
                case PRINT:
                case VAR:
                case LEFT_BRACE:
                    return;
                default:
                    break;
            }

            advance();
        }
    }

    private ParseError error(Token token, String message) {
        if (token.getType() == TokenType.EOF) {
            System.err.println("[line " + token.getLine() + "] Error at end: " + message);
        } else {
            System.err.println("[line " + token.getLine() + "] Error at '" + token.getLexeme() + "': " + message);
        }
        return new ParseError();
    }
}