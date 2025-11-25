package MojPakiet;

import java.util.ArrayList;

public class t1_TrojkatPascalaTabDynamiczne {
    
    public static void trojkatPascalaIteracyjnie(int n){
        
        // Dynamiczna tablica dwuwymiarowa: lista list
        ArrayList<ArrayList<Long>> tab = new ArrayList<>();
        
        for (int j = 0; j < n; j++) {
            
            // Dodaj nowy dynamiczny wiersz
            tab.add(new ArrayList<Long>());
            
            // Pierwszy element zawsze 1
            tab.get(j).add(1L);
            
            // Oblicz œrodkowe elementy
            for (int i = 1; i < j; i++) {
                long wartosc = tab.get(j - 1).get(i - 1) + tab.get(j - 1).get(i);
                tab.get(j).add(wartosc);
            }
            
            // Ostatni element te¿ 1 (gdy j > 0)
            if (j > 0) {
                tab.get(j).add(1L);
            }
            
            // Wypisz wiersz
            for (Long liczba : tab.get(j)) {
                System.out.print(liczba + " ");
            }
            System.out.println();
        }
    }

    
    public static void trojkatPascalaRekurencyjnie(int n){
        for (int wiersz = 0; wiersz < n; wiersz++){
            for (int pozycja = 0; pozycja <= wiersz; pozycja++){
                System.out.print(RekurencjaPomocniczna(wiersz, pozycja) + " ");
            }
            System.out.println();
        }
    }
    
    public static long RekurencjaPomocniczna(int wiersz, int pozycja) {
        if (pozycja == 0 || pozycja == wiersz) {
            return 1;
        }
        return RekurencjaPomocniczna(wiersz - 1, pozycja - 1)
             + RekurencjaPomocniczna(wiersz - 1, pozycja);
    }
    
    public static void main(String[] args) {
        int liczbaN = 10;

        System.out.println("Trojkat Pascala (Iteracyjnie) to: ");
        trojkatPascalaIteracyjnie(liczbaN);

        System.out.println("Trojkat Pascala (Rekurencyjnie) to: ");
        trojkatPascalaRekurencyjnie(liczbaN);
    }
}
