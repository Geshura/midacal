package MojPakiet;

public class t1_SilniaDwumian {
	
	public static int silniaIteracyjna(int n){
	    if(n < 0){
	        return -1;
	    }
	    int silnia = 1;
	    for(int i=1; i<=n; i++){
	        silnia = silnia * i;    
	    }
	    return silnia;
	}
	
	public static int silniaRekurencyjna(int n){
		if(n < 0){
	    	return -1;
	    }
	    if(n == 0){
	    	return 1;
	    }
	    return n * silniaRekurencyjna(n-1);
	}
	
	public static int dwumianNewtonaIteracyjnie(int n, int k){
		if(k<0||k>n){
			return -1;
		}
		if(k==0||k==n){
			return 1;
		}
	    int nSilnia = silniaIteracyjna(n);
	    int kSilnia = silniaIteracyjna(k);
	    int nkSilnia = silniaIteracyjna(n-k);
	    return nSilnia/(kSilnia*nkSilnia);
	}
	
	public static int dwumianNewtonaRekurencyjnie(int n, int k){
		if(k<0||k>n){
			return -1;
		}
		if(k==0||k==n){
			return 1;
		}
	    return dwumianNewtonaRekurencyjnie(n-1,k-1)+dwumianNewtonaRekurencyjnie(n-1,k);
	}
	
	public static int dwumianNewtonaPrzysp(int n, int k) {
	    if (k < 0 || k > n) {
	        return -1;
	    }
	    if (k == 0 || k == n) {
	        return 1;
	    }

	    if (k > n - k) {
	        k = n - k;
	    }

	    int wynik = 1;

	    for (int i = 1; i <= k; i++) {
	        wynik = wynik * (n - i + 1) / i;
	    }

	    return wynik;
	}
	
	public static void main(String[] args) {
        int liczbaN = 8;
        int liczbaK = 6;
        
        // SILNIA ITERACYJNA
        int wynikIteracyjny = silniaIteracyjna(liczbaN);
        if (wynikIteracyjny == -1){
            System.out.println("Silnia (Iteracyjnie) z liczby "+liczbaN+" nie mo¿e zostaæ utworzona (ujemne n).");
        }else if (liczbaN > 12){
            System.out.println("Silnia (Iteracyjnie) z liczby "+liczbaN+" nie mo¿e zostaæ utworzona (przepe³nienie int).");
        }else{
            System.out.println("Silnia (Iteracyjnie) z liczby "+liczbaN+" to: "+wynikIteracyjny);
        }

        // SILNIA REKURENCYJNA
        int wynikRekurencyjny = silniaRekurencyjna(liczbaN);
        if (wynikRekurencyjny == -1){
            System.out.println("Silnia (Rekurencyjnie) z liczby "+liczbaN+" nie mo¿e zostaæ utworzona (ujemne n).");
        }else if (liczbaN > 12){
            System.out.println("Silnia (Rekurencyjnie) z liczby "+liczbaN+" nie mo¿e zostaæ utworzona (przepe³nienie int).");
        }else{
            System.out.println("Silnia (Rekurencyjnie) z liczby "+liczbaN+" to: "+wynikRekurencyjny);
        }

        // DWUMIAN NEWTONA ITERACYJNIE
        int wynikDwumianuIteracyjny = dwumianNewtonaIteracyjnie(liczbaN, liczbaK);
        if (wynikDwumianuIteracyjny == -1){ 
            System.out.println("Dwumian Newtona (Iteracyjnie) z "+liczbaN+" nie mo¿e zostaæ utworzony.");
        }else{
            System.out.println("Dwumian Newtona (Iteracyjnie) z "+liczbaN+" to: "+wynikDwumianuIteracyjny);
        }
        
        // DWUMIAN NEWTONA REKURENCYJNIE
        int wynikDwumianuRekurencyjny = dwumianNewtonaRekurencyjnie(liczbaN, liczbaK);
        if (wynikDwumianuRekurencyjny == -1){ 
            System.out.println("Dwumian Newtona (Rekurencyjnie) z "+liczbaN+" nie mo¿e zostaæ utworzony.");
        }else{
            System.out.println("Dwumian Newtona (Rekurencyjnie) z "+liczbaN+" to: "+wynikDwumianuRekurencyjny);
        }
            
         // DWUMIAN NEWTONA PRZYSPIESZONY
         int wynikDwumianuPrzysp = dwumianNewtonaPrzysp (liczbaN, liczbaK);
         if (wynikDwumianuPrzysp == -1){ 
            System.out.println("Dwumian Newtona Przysp z "+liczbaN+" nie mo¿e zostaæ utworzony.");
         }else{
            System.out.println("Dwumian Newtona Przysp z "+liczbaN+" to: "+wynikDwumianuPrzysp);
         }
	}
}