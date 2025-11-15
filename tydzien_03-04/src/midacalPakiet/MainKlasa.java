package midacalPakiet;

import java.time.LocalDate;

public class MainKlasa {

	public static void main(String[] args) {
		System.out.println("Start programu..");
		List<ZdarzenieKlasa> ListaZdarzenia = new Arraylist<>();
		List<KontaktKlasa> ListaKontakty = new Arraylist<>();
		
		//tworzenie obiektu Zdarzenie
		ZdarzenieKlasa zdarzenie = new ZdarzenieKlasa("Praca", LocalDate.of(2025, 11, 26),"Uczelnia","Rozliczenie projektu - tydzieñ 03/04.");
		ZdarzenieKlasa zdarzenie = new ZdarzenieKlasa("Praca", LocalDate.of(2025, 11, 26),"Uczelnia, sala 57","Technologie komponentowe (p) -  prof. dr hab. Adam Niewiadomski, sala 57");
		ZdarzenieKlasa zdarzenie = new ZdarzenieKlasa("Praca", LocalDate.of(2025, 11, 26),"Uczelnia, sala 57","Technologie komponentowe (p) -  prof. dr hab. Adam Niewiadomski, sala 57");
		ZdarzenieKlasa zdarzenie = new ZdarzenieKlasa("Praca", LocalDate.of(2025, 11, 26),"Uczelnia, sala 57","Technologie komponentowe (p) -  prof. dr hab. Adam Niewiadomski, sala 57");
		
		//tworzenie obiektu Kontakt
		KontaktKlasa kontakt = null;
		KontaktKlasa kontakt = new KontaktKlasa("Wojtek Gola", )
		

		System.out.println("Utworzone obiekty:");
		System.out.println(zdarzenie);
		System.out.println(kontakt);
	}

}

//metoda compareTo
//metoda compare
//metoda toString() - wyœwietlanie wartoœci pól obiektu.
/* sortowanie (https://docs.oracle.com/javase/tutorial/collections/algorithms/index.html)
import java.util.*;

public class Sort {
    public static void main(String[] args) {
        List<String> list = Arrays.asList(args);
        Collections.sort(list);
        System.out.println(list);
    }
}
*/

