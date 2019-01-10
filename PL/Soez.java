import java.util.*;
import java.io.*;

public class Soez {
	public static void main(String args[]) {
		int i;
		int even;
		double x;
		i=0;
		even=0;
		x=0;
		Scanner scan=new Scanner(System.in);
		x=scan.nextDouble();
		i=scan.nextInt();
		even=scan.nextInt();
		while(i<5){
			i=i+1;
			if(i==((i/2)*2))
			{
				even=even+1;
			}
			
		}
		i=even+10;
		System.out.print(x);
	}
}