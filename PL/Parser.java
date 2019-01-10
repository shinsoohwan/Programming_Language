import java.io.IOException;
import java.util.*;

public class Parser {
    Token token;          // current token from the input stream
    Lexer lexer;
  
    public Parser(Lexer ts) { 
        lexer = ts;                        
        token = lexer.next();          
    }
  
    private String match (TokenType t) {
        String value = token.value();
        if (token.type().equals(t))
            token = lexer.next();
        else
            error(t);
        return value;
    }
  
    private void error(TokenType tok) {
        System.err.println("Syntax error1: expecting: " + tok + "; saw: " + token + "");
        System.exit(1);
    }
  
    private void error(String tok) {
         System.err.println("Syntax error2: expecting: " + tok + "; saw: " + token+ " ");
        System.exit(1);
    }
  
    public Program program() {
        // Program --> void main ( ) '{' Declarations Statements '}'
        TokenType[ ] header = {TokenType.Int, TokenType.Main,
                          TokenType.LeftParen, TokenType.RightParen};
        for (int i=0; i<header.length; i++)   // bypass "int main ( )"
            match(header[i]);
        match(TokenType.LeftBrace);
	Declarations diss = declarations();
	Block bb = progstatements();
    match(TokenType.RightBrace);
    
	return new Program(diss, bb);  
    }
  
    private Declarations declarations() {
        // Declarations --> { Declaration }
	Declarations ds = new Declarations(); 
	while (isType()){
		declaration(ds);
	}
        return ds;  
    }
  
    private void declaration (Declarations ds) {
        // Declaration  --> Type Identifier { , Identifier } ;
	Variable v;
	Declaration d;
	Type t = type();
	v = new Variable(match(TokenType.Identifier));
	d = new Declaration(v, t);	
	ds.add(d);

		while (isComma()) {	
			token = lexer.next();	
			v = new Variable(match(TokenType.Identifier));
			d = new Declaration(v, t);
			ds.add(d);
		}
	match(TokenType.Semicolon);
	}
  
    private Type type () {
        // Type  -->  int | bool | float | char | string
	Type t = null;
	if (token.type().equals(TokenType.Int)) {
            t = Type.INT;		
	} else if (token.type().equals(TokenType.Bool)) {
			t = Type.BOOL;
	} else if (token.type().equals(TokenType.Float)) {
			t = Type.FLOAT;
	} else if (token.type().equals(TokenType.Char)) {
			t = Type.CHAR;
	}
	else error ("Error in Type construction");
	
	token = lexer.next();
	return t;          
    }
  
    private Statement statement() {
        // Statement --> ; | Block | Assignment | IfStatement | WhileStatement  | print | scan
	Statement s = null;
	if (token.type().equals(TokenType.Semicolon))
		s = new Skip();
	else if (token.type().equals(TokenType.LeftBrace)) //block
		s = statements();
	else if (token.type().equals(TokenType.If))    //if
		s = ifStatement();
	else if (token.type().equals(TokenType.While)) //while
		s = whileStatement();
	else if (token.type().equals(TokenType.Identifier)) 
		s = assignment();
	
	else if (token.type().equals(TokenType.Scan))
		s = scanstatement();
	else if (token.type().equals(TokenType.Print))
		s = printstatement();
	
	
	else error("Error in Statement construction");
        return s;
    }
  
    private Block statements( ) {
        // Block --> '{' Statements '}'
	Statement s;
	Block b = new Block();
	
	match(TokenType.LeftBrace);
	while (isStatement()) {
		s = statement();
		b.members.add(s);
	}
	
    match(TokenType.RightBrace);// end of the block 
    return b;
    }
    
	private Block progstatements( ) {
	Block b = new Block();
	Statement s;
	while (isStatement()) {
		s = statement();
		b.members.add(s);
	} 
        return b;
    }
 
	
	

    private Assignment assignment( ) {
        // Assignment --> Identifier = Expression ;
	Expression source; 
	Variable target; 

	target = new Variable(match(TokenType.Identifier));
	match(TokenType.Assign);
	source = expression();
	match(TokenType.Semicolon);

	return new Assignment(target, source); 

    }

    private Conditional ifStatement() {
        // IfStatement --> if ( Expression ) Statement [ else Statement ]
	Conditional con;
	Statement s;
	Expression test;
	
	match(TokenType.If);
	match(TokenType.LeftParen);
	test = expression();
	match(TokenType.RightParen);
	s = statement();
	if (token.type().equals(TokenType.Else)) {
		match(TokenType.Else);
		Statement elsestate = statement();
		con = new Conditional(test, s, elsestate);
	}
	else {
		con = new Conditional(test, s);
	}
	return con;
    }
  
    private Loop whileStatement() {
        // WhileStatement --> while ( Expression ) Statement
  	Statement body;
	Expression test;

	match(TokenType.While);
	match(TokenType.LeftParen);
	test = expression();
	match(TokenType.RightParen);
	body = statement();
	return new Loop(test, body);
	
    }
	private Print printstatement(){
	      // PrintStatement --> print ( Statement )       print(i) ->  System.out.println(i)
	   Expression e;
	   match(TokenType.Print);

	   match(TokenType.LeftParen);

	   e = expression();

	   match(TokenType.RightParen);

	   match(TokenType.Semicolon);
	   return new Print(e);
	}
	private Scan scanstatement() { // scan( type, variable);
		Variable v;
		Type t;
		match(TokenType.Scan);
		match(TokenType.LeftParen);
		
		t=type();
		match(TokenType.Comma);
		v = new Variable(match(TokenType.Identifier));
		
		match(TokenType.RightParen);
		match(TokenType.Semicolon);
		return new Scan(v,t);
	}
	 
    private Expression expression() {
        // Expression --> Conjunction { || Conjunction }
	Expression c = conjunction();
	while (token.type().equals(TokenType.Or)) {
		Operator op = new Operator(match(token.type()));
		Expression e = expression();
		c = new Binary(op, c, e);
	}
	return c;  
    }
  
    private Expression conjunction() {
        // Conjunction --> Equality { && Equality }
	Expression eq = equality();
	while (token.type().equals(TokenType.And)) {
		Operator op = new Operator(match(token.type()));
		Expression c = conjunction();
		eq = new Binary(op, eq, c);
	}
        return eq;  
    }
  
    private Expression equality() {
        // Equality --> Relation [ EquOp Relation ]
	Expression rel = relation();
	while (isEqualityOp()) {
		Operator op = new Operator(match(token.type()));
		Expression rel2 = relation();
		rel = new Binary(op, rel, rel2);
	}
        return rel;  
    }

    private Expression relation() {
        // Relation --> Addition [RelOp Addition] 
	Expression a = addition();
	while (isRelationalOp()) {
		Operator op = new Operator(match(token.type()));
		Expression a2 = addition();
		a = new Binary(op, a, a2);
	}
        return a;  
    }
  
    private Expression addition() {
        // Addition --> Term { AddOp Term }
        Expression e = term();
        while (isAddOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = term();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression term() {
        // Term --> Factor { MultiplyOp Factor }
        Expression e = factor();
        while (isMultiplyOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term2 = factor();
            e = new Binary(op, e, term2);
        }
        return e;
    }
  
    private Expression factor() {
        // Factor --> [ UnaryOp ] Primary 
        if (isUnaryOp()) {
            Operator op = new Operator(match(token.type()));
            Expression term = primary();
             return new Unary(op, term);
        }
        else return primary();
    }
  
    private Expression primary () {
        // Primary --> Identifier | Literal | ( Expression )
        //             | Type ( Expression )
        Expression e = null;
        if (token.type().equals(TokenType.Identifier)) {
            e = new Variable(match(TokenType.Identifier));
        } else if (isLiteral()) {
            e = literal();
        } else if (token.type().equals(TokenType.LeftParen)) {
            token = lexer.next();
            e = expression();       
            match(TokenType.RightParen);
        } else if (isType( )) {
            Operator op = new Operator(match(token.type()));
            match(TokenType.LeftParen);
            Expression term = expression();
            match(TokenType.RightParen);
            e = new Unary(op, term);
        } else error("Identifier | Literal | ( | Type");
        return e;
    }

    private Value literal( ) {
	Value value = null;
	String stval = token.value();
	if (token.type().equals(TokenType.IntLiteral)) {
		value = new IntValue (Integer.parseInt(stval));
		token = lexer.next();
	}
	else if (token.type().equals(TokenType.FloatLiteral))  {
		value = new FloatValue(Float.parseFloat(stval));
		token = lexer.next();
	}
	else if (token.type().equals(TokenType.CharLiteral))  {
		value = new CharValue(stval.charAt(0));
		token = lexer.next();
	}
    else if (token.type().equals(TokenType.True))  {
        value = new BoolValue(true);
        token = lexer.next();
    }
    else if (token.type().equals(TokenType.False))  {
        value = new BoolValue(false);
        token = lexer.next();
    }
     else error ("Error in literal value contruction");
	return value;
    }
  
    private boolean isBooleanOp() {
	return token.type().equals(TokenType.And) || 
	    token.type().equals(TokenType.Or);
    } 

    private boolean isAddOp( ) {
        return token.type().equals(TokenType.Plus) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isMultiplyOp( ) {
        return token.type().equals(TokenType.Multiply) ||
               token.type().equals(TokenType.Divide);
    }
    
    private boolean isUnaryOp( ) {
        return token.type().equals(TokenType.Not) ||
               token.type().equals(TokenType.Minus);
    }
    
    private boolean isEqualityOp( ) {
        return token.type().equals(TokenType.Equals) ||
            token.type().equals(TokenType.NotEqual);
    }
    
    private boolean isRelationalOp( ) {
        return token.type().equals(TokenType.Less) ||
               token.type().equals(TokenType.LessEqual) || 
               token.type().equals(TokenType.Greater) ||
               token.type().equals(TokenType.GreaterEqual);
    }
    
    private boolean isType( ) {
        return token.type().equals(TokenType.Int)
            || token.type().equals(TokenType.Bool) 
            || token.type().equals(TokenType.Float)
            || token.type().equals(TokenType.Char);
    }
    
    private boolean isLiteral( ) {
        return token.type().equals(TokenType.IntLiteral) ||
            isBooleanLiteral() ||
            token.type().equals(TokenType.FloatLiteral) ||
            token.type().equals(TokenType.CharLiteral);
    }
    
    private boolean isBooleanLiteral( ) {
        return token.type().equals(TokenType.True) ||
            token.type().equals(TokenType.False);
    }

    private boolean isComma( ) {
	return token.type().equals(TokenType.Comma);
    }
   
    private boolean isSemicolon( ) {
	return token.type().equals(TokenType.Semicolon);
    }

    private boolean isLeftBrace() {
	return token.type().equals(TokenType.LeftBrace);
    } 
 
    private boolean isRightBrace() {
	return token.type().equals(TokenType.RightBrace);
    } 

    private boolean isStatement() {
	return 	isSemicolon() ||
		isLeftBrace() ||
		token.type().equals(TokenType.If) ||
		token.type().equals(TokenType.While) ||
		token.type().equals(TokenType.Print) ||
		token.type().equals(TokenType.Scan) ||
		token.type().equals(TokenType.Identifier); 
    }
 
    public static void main(String args[]) {
        Parser parser  = new Parser(new Lexer(args[0]));
        Program prog = parser.program();
        prog.display();
    } //main

} // Parser

