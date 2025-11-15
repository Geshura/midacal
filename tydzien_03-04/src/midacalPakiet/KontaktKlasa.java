package midacalPakiet;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import javax.mail.internet.InternetAddress;

public class KontaktKlasa{
	private String imie;
	private String nazwisko;
	private PhoneNumber telefon;
	private InternetAddress email;

	//konstruktor bezargumentowy
	public KontaktKlasa(){
	}
	
	//konstruktor g³ówny do tworzenia obiektów
	public KontaktKlasa(String imie, String nazwisko, PhoneNumber telefon, InternetAddress email){
		this.imie=imie;
		this.nazwisko=nazwisko;
		this.telefon=telefon;
		this.email=email;
	}
	
	//metoda do zapisu set
	public void setImie(String imie){
		this.imie=imie;
	}
	public void setNazwisko(String nazwisko){
		this.nazwisko=nazwisko;
	}
	public void setTelefon(PhoneNumber telefon){
		this.telefon=telefon;
	}
	public void setEmail(InternetAddress email){
		this.email=email;
	}
	
	//metoda do odczytu get
	public String getImie(){
		return imie;
	}
	public String getNazwisko(){
		return nazwisko;
	}
	public PhoneNumber getTelefon(){
		return telefon;
	}
	public InternetAddress getEmail(){
		return email;
	}
	
	//metoda do wyswietlania toString()
	@Override
	public String toString(){
	    return "Kontakt {" +
	            "imie='" + imie + '\'' +
	            ", nazwisko=" + nazwisko +
	            ", telefon='" + telefon + '\'' +
	            ", email='" + email + '\'' +
	            '}';
	}
}

