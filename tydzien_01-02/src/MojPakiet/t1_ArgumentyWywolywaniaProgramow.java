package MojPakiet;

public class t1_ArgumentyWywolywaniaProgramow {

	public static void main(String[] args) {
		System.out.println("1. Drukowanie pierwszego argumentu wywo³ania");
		System.out.println(args[0]);

<<<<<<< HEAD

		System.out.println("2. Drukowanie DRUGIEGO i TRZECIEGO argumentu wywo³ania");
		System.out.println(args[1]+" | "+args[2]);


=======
		System.out.println("2. Drukowanie DRUGIEGO i TRZECIEGO argumentu wywo³ania");
		System.out.println(args[1]+" | "+args[2]);

>>>>>>> 6eb7266fe9017e1da64a6b2a6d3f59629dbc7dd1
		System.out.println("3. Zabezpieczenie przed niew³aœciw¹ liczb¹ argumentów");
		if (args.length <3)
		System.out.print("Za ma³o argumentów");
		else
		System.out.println(" * " + args[0]+" ** "+args[1]+" *** "+args[2]);

<<<<<<< HEAD

=======
>>>>>>> 6eb7266fe9017e1da64a6b2a6d3f59629dbc7dd1
		System.out.println("4. Drukowanie dowolnej liczby argumentów");
		int i;
		for (i=0; i<args.length; i++)
		System.out.println(args[i]);
<<<<<<< HEAD
		
=======
>>>>>>> 6eb7266fe9017e1da64a6b2a6d3f59629dbc7dd1
	}
}
