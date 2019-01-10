import java.io.*;
import java.util.Scanner;

public class CodeGenerate {
	public static void main (String args[]) throws Exception
	{
		Parser parser  = new Parser(new Lexer(args[0]));

        System.out.println("lexer & parser start");
        Program prog = parser.program();
        System.out.println("lexer & parser end");
        

        System.out.println("typemap start");
		TypeMap map = StaticTypeCheck.typing(prog.decpart);
		StaticTypeCheck.V(prog);
		
		 Program out = TypeTransformer.T(prog, map);
	     System.out.println("Output AST");
	     out.display();
	     Semantics semantics = new Semantics( );
	     State state = semantics.M(out);
	     System.out.println("\nFinal State");		
        System.out.println("");
        state.display();
                
        System.out.println("making output file...");
        BufferedWriter bw=new BufferedWriter(new FileWriter("./Soez.java"));
    	PrintWriter pw=new PrintWriter(bw,true);
    	pw.println("import java.util.*;");
    	pw.println("import java.io.*;");
    	pw.println("");
    	pw.println("public class Soez {");
    	pw.println("\tpublic static void main(String args[]) {");
     	pw.flush();

        prog.output();
        pw.flush();
        pw.close();
        
        BufferedWriter bw2=new BufferedWriter(new FileWriter("./Soez.java",true));
        PrintWriter pw2=new PrintWriter(bw2,true);
        pw2.println("}");
        pw2.print("}");
        pw2.flush();
        pw2.close();
        System.out.println("OK");
     }
}
