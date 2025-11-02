package MojPakiet;

public class t1_ArgumentyWywolywaniaProgramow {

	public static void main(String[] args) {
		System.out.println("1. Drukowanie pierwszego argumentu wywołania");
		System.out.println(args[0]);

		System.out.println("2. Drukowanie DRUGIEGO i TRZECIEGO argumentu wywołania");
		System.out.println(args[1]+" | "+args[2]);

		System.out.println("3. Zabezpieczenie przed niewłaściwą liczbą argumentów");
		if (args.length <3)
		System.out.print("Za mało argumentów");
		else
		System.out.println(" * " + args[0]+" ** "+args[1]+" *** "+args[2]);

		System.out.println("4. Drukowanie dowolnej liczby argumentów");
		int i;
		for (i=0; i<args.length; i++)
		System.out.println(args[i]);
	}
}
