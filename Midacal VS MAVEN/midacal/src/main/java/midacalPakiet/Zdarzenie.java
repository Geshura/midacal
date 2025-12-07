package midacalPakiet;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "zdarzenie")
@XmlAccessorType(XmlAccessType.FIELD)
public class Zdarzenie implements Comparable<Zdarzenie>{
    private int id;
	private String nazwa;
    @XmlJavaTypeAdapter(value = AdapterData.class)
	private LocalDate data;
		@XmlElement(name = "miejsce")
		@XmlJavaTypeAdapter(AdapterLokalizacja.class)
		private URI lokalizacja;
	private String opis;
	private List<Kontakt> kontakty;

	//konstruktor bezargumentowy
	public Zdarzenie(){
		this.kontakty = new ArrayList<>();
	}
	
	// konstruktor glowny do tworzenia obiektow
	public Zdarzenie(String nazwa, LocalDate data, String lokalizacja, String opis){
		this.nazwa=nazwa;
		this.data=data;
		if (lokalizacja != null) {
			try {
				this.lokalizacja = new URI(lokalizacja);
			} catch (URISyntaxException e) {
				// spróbuj zastąpić spacje i ustawić jako URI
				try {
					this.lokalizacja = new URI(lokalizacja.replaceAll(" ", "%20"));
				} catch (URISyntaxException ex) {
					this.lokalizacja = null;
				}
			}
		} else {
			this.lokalizacja = null;
		}
		this.opis=opis;
		this.kontakty = new ArrayList<>();
	}
	
	//metody do zapisu set
	public void setNazwa(String nazwa){
		this.nazwa=nazwa;
	}
	public void setData(LocalDate data){
		this.data=data;
	}
		public void setLokalizacja(String lokalizacja){
			if (lokalizacja != null) {
				try {
					this.lokalizacja = new URI(lokalizacja);
				} catch (URISyntaxException e) {
					try {
						this.lokalizacja = new URI(lokalizacja.replaceAll(" ", "%20"));
					} catch (URISyntaxException ex) {
						this.lokalizacja = null;
					}
				}
			} else {
				this.lokalizacja = null;
			}
		}
	public void setOpis(String opis){
		this.opis=opis;
	}
	
	//metody do odczytu get
	public String getNazwa(){
		return nazwa;
	}
	public LocalDate getData(){
		return data;
	}
		public URI getLokalizacja(){
			return lokalizacja;
		}
	public String getOpis(){
		return opis;
	}

	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}

	public List<Kontakt> getKontakty(){
		return kontakty;
	}

	public void setKontakty(List<Kontakt> kontakty){
		this.kontakty = kontakty;
	}

	public void addKontakt(Kontakt kontakt){
		if (!this.kontakty.contains(kontakt)) {
			this.kontakty.add(kontakt);
		}
	}

	public void removeKontakt(Kontakt kontakt){
		this.kontakty.remove(kontakt);
	}

	//metoda CompareTo (interfejs Comparable) do porownywania zdarzen po domyslnym atrybucie data
	public int compareTo(Zdarzenie other) {
	    return this.data.compareTo(other.getData());
	}

	//metoda do wyswietlania toString()
	@Override
	public String toString(){
	    return "Zdarzenie {" +
	            "nazwa='" + nazwa + "'" + 
	            ", data=" + data + 
				   ", lokalizacja='" + (lokalizacja != null ? lokalizacja.toString() : "brak") + "'" + 
	            ", opis='" + opis + "'" + 
	            '}';
	}
}
