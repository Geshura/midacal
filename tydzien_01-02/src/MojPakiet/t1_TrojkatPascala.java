package MojPakiet;

public class t1_TrojkatPascala {
	
	public static void trojkatPascalaIteracyjnie(int n) {
	    long[] tab = new long[n];
	    tab[0] = 1;

	    for (int i = 0; i < n; i++) {
	        for (int j = i; j > 0; j--) {
	            tab[j] = tab[j] + tab[j - 1];
	        }

	        for (int j = 0; j <= i; j++) {
	            System.out.print(tab[j] + " ");
	        }
	        System.out.println();
	    }
	}
	public static void trojkatPascalaRekurencyjnie(int n){
		for (int wiersz=0;wiersz<n;wiersz++){
			for (int pozycja=0;pozycja<=wiersz;pozycja++){
			System.out.print(RekurencjaPomocniczna(wiersz,pozycja)+" ");
			}
			System.out.println();
		}
	}
	
	public static long RekurencjaPomocniczna(int wiersz, int pozycja) {
		if (pozycja==0||pozycja==wiersz) {
			return 1;
		}
		return RekurencjaPomocniczna(wiersz-1,pozycja-1) + RekurencjaPomocniczna(wiersz-1,pozycja);
	}
	

	
	public static void main(String[] args) {
		int liczbaN = 10;

		System.out.println("Trojkat Pascala (Iteracyjnie) to: ");
		trojkatPascalaIteracyjnie(liczbaN);
		System.out.println("Trojkat Pascala (Rekurencyjnie) to: ");
		trojkatPascalaRekurencyjnie(liczbaN);
	}
}
