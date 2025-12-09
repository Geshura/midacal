package midacalPakiet;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "kontakt")
@XmlAccessorType(XmlAccessType.FIELD)
public class Kontakt implements Comparable<Kontakt>{
    private static int nextId = 1;
    private int id;
	private String imie;
	private String nazwisko;
    @XmlJavaTypeAdapter(value = AdapterPhone.class)
	private PhoneNumber telefon;
    @XmlJavaTypeAdapter(value = AdapterEmail.class)
	private InternetAddress email;
	@XmlElement(name = "zdarzenie")
	private List<Zdarzenie> zdarzenia;

	//konstruktor bezargumentowy
	public Kontakt(){
		this.zdarzenia = new ArrayList<>();
	}
	
	//konstruktor glowny do tworzenia obiektow
	public Kontakt(String imie, String nazwisko, PhoneNumber telefon, InternetAddress email) throws AddressException{
		this.id = nextId++;
		this.imie=imie;
		this.nazwisko=nazwisko;
		this.telefon=telefon;
		if (email != null) {
			email.validate();
		}
		this.email=email;
		this.zdarzenia = new ArrayList<>();
	}
	
	//resetuj licznik ID
	public static void resetIdCounter(){
		nextId = 1;
	}
	public void setImie(String imie){
		this.imie=imie;
	}
	public void setNazwisko(String nazwisko){
		this.nazwisko=nazwisko;
	}
	public void setTelefon(PhoneNumber telefon){
		this.telefon=telefon;
	}
	public void setEmail(InternetAddress email) throws AddressException{
		if (email != null) {
			email.validate();
		}
		this.email=email;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}
	
	//metody do odczytu get
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

	public List<Zdarzenie> getZdarzenia(){
		return zdarzenia;
	}

	public void setZdarzenia(List<Zdarzenie> zdarzenia){
		this.zdarzenia = zdarzenia;
	}

	public void addZdarzenie(Zdarzenie zdarzenie){
		if (!this.zdarzenia.contains(zdarzenie)) {
			this.zdarzenia.add(zdarzenie);
		}
	}

	public void removeZdarzenie(Zdarzenie zdarzenie){
		this.zdarzenia.remove(zdarzenie);
	}

	//metoda CompareTo (interfejs Comparable) do porownywania kontaktu po domyslnym atrybucie nazwisko
	public int compareTo(Kontakt other) {
	    return this.nazwisko.compareTo(other.getNazwisko());
	}
	
	//metoda do wyswietlania toString()
	@Override
	public String toString(){
		
		String samtelefon = (telefon != null) ? String.valueOf(telefon.getNationalNumber()) : "brak";
        String samemail = (email != null) ? email.getAddress() : "brak";
        
	    return "Kontakt {" +
	            "imie='" + imie + "'" + 
	            ", nazwisko=" + nazwisko + 
	            ", telefon='" + samtelefon + "'" + 
	            ", email='" + samemail + "'" + 
	            '}';
	}
}
