package MojPakiet;

public class t1_TrojkatPascala {
	
	public static void trojkatPascalaIteracyjnie(int n){
		long tab[][] = new long[n][n];
		for (int j=0; j<n; j++)
		{
		    tab[j][0]=1;
		    tab[j][j]=1;
		    
		    for (int i=0; i<j; i++){
		        tab[j][i+1]=tab[j-1][i]+tab[j-1][i+1];
		        System.out.print(tab[j][i] + " ");
		    }
		    System.out.print(tab[j][j] + " ");
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
sda
fsda
fsd
afsda
fsda
fsda
fsad
fasd
fsad
fsad
fsa

