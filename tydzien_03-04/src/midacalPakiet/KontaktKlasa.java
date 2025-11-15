package midacalPakiet;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import jakarta.mail.internet.InternetAddress

public class KontaktKlasa{
	private String imie;
	private String nazwisko;
	private PhoneNumber telefon;
	private InternetAddress email;
}

//konstruktor bezargumentowy
public KontaktKlasa(){
}

//konstruktor g³ówny do tworzenia obiektów
public KontaktKlasa{
	this.imie=imie;
	this.nazwisko=nazwisko;
	this.telefon=telefon;
	this.email=email;
}

//metoda zapisu set
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

//metoda zapisu get
public void getImie(String imie){
	this.imie=imie;
}
public void getNazwisko(String nazwisko){
	this.nazwisko=nazwisko;
}
public void getTelefon(PhoneNumber telefon){
	this.telefon=telefon;
}
public void getEmail(InternetAddress email){
	this.email=email;
}

