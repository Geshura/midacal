package MojPakiet;

public class t1_DeklarowanieZmiennych {

	public static void main(String[] args) {
		// dodawanie liczb
		double a=4; // deklaracja zmiennej
		double b=5;
		System.out.println(a+b);

		// dzielenie z instrukcja warunkowa
		if (b!=0)
		System.out.println(a/b);
		else
		System.out.println("Nie dzielimy przez zero!");
	}

}

/*
2. Dzielenie bylo wykonywane na liczbach calkowitych (int). Gdy obie liczby w operacji dzielenia sa tego typu, wynik jest rowniez sprowadzany do liczby calkowitej, a jego czesc ulamkowa zostaje odrzucona. Wlasnie dlatego matematyczny rezultat 0.8 zostal zredukowany do 0.

Zmiana typu na double rozwiazala ten problem, poniewaz double to typ zmiennoprzecinkowy, ktory precyzyjnie przechowuje wartosci z ulamkami. Dzieki temu operacja dala poprawny wynik 0.8.
*/