package com.MW.chatServer.functions;


import java.util.Scanner;// program uses class Scanner


public class first {
	private static Scanner scan;

	public static void main(String[] args){
		
		 int a, b, sum;
	        scan = new Scanner(System.in);//to Obtain User Input from the Keyboard
			
	        System.out.print("Enter Two Numbers : ");
	        a = scan.nextInt();
	        b = scan.nextInt();
			
	        sum = a + b;
			
	        System.out.print("Sum of the Entered Two Number is " +sum);// add numbers then store total in sum
		
	
		
		
		
	}

}
