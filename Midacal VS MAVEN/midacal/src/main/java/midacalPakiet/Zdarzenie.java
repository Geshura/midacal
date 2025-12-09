package midacalPakiet;

import java.time.LocalDate;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty; 
import com.fasterxml.jackson.annotation.JsonFormat; // Nowy Import

public class Zdarzenie implements Comparable<Zdarzenie>{
    private static int nextId = 1;
    private int id;
	private String nazwa;
    
    // KLUCZOWA POPRAWKA 1: Użycie @JsonFormat, aby jawnie określić format zapisu.
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    // KLUCZOWA POPRAWKA 2: Dodajemy @JsonProperty nad polem, aby wymusić serializację TYLKO tego pola
    @JsonProperty("data")
	private LocalDate data;
		
	@JsonProperty("miejsce")
	private URI lokalizacja;
	
	private String opis;
	
	@JsonProperty("kontakt")
	private List<Kontakt> kontakty;

	//konstruktor bezargumentowy
	public Zdarzenie(){
		this.kontakty = new ArrayList<>();
	}
	
	// konstruktor glowny do tworzenia obiektow
	public Zdarzenie(String nazwa, LocalDate data, String lokalizacjaStr, String opis){
		this.id = nextId++;
		this.nazwa=nazwa;
		this.data=data;
		
		setLokalizacja(lokalizacjaStr);
        
		this.opis=opis;
		this.kontakty = new ArrayList<>();
	}
	
	//resetuj licznik ID
	public static void resetIdCounter(){
		nextId = 1;
	}
    
	public int getId(){
		return id;
	}

	public void setId(int id){
		this.id = id;
	}
    
	public void setNazwa(String nazwa){
		this.nazwa=nazwa;
	}
    
	public void setData(LocalDate data){
		this.data=data;
	}
    
	public void setLokalizacja(String lokalizacjaStr){
        if (lokalizacjaStr != null && !lokalizacjaStr.trim().isEmpty()) {
			try {
				this.lokalizacja = new URI(lokalizacjaStr);
			} catch (URISyntaxException e) {
				try {
					this.lokalizacja = new URI(lokalizacjaStr.replaceAll(" ", "%20"));
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
            "id=" + id +
	            ", nazwa='" + nazwa + "'" + 
	            ", data=" + data + 
				   ", lokalizacja='" + (lokalizacja != null ? lokalizacja.toString() : "brak") + "'" + 
	            ", opis='" + opis + "'" + 
	            '}';
	}
}