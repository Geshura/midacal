package midacalPakiet;

import java.time.LocalDate;

public class Zdarzenie{
	private String nazwa;
	private LocalDate data;
	private String miejsce;
	private String opis;

	//konstruktor bezargumentowy
	public Zdarzenie(){
	}
	
	//konstruktor g³ówny do tworzenia obiektów
	public Zdarzenie(String nazwa, LocalDate data, String miejsce, String opis){
		this.nazwa=nazwa;
		this.data=data;
		this.miejsce=miejsce;
		this.opis=opis;
	}
	
	//metoda do zapisu set
	public void setNazwa(String nazwa){
		this.nazwa=nazwa;
	}
	public void setData(LocalDate data){
		this.data=data;
	}
	public void setMiejsce(String miejsce){
		this.miejsce=miejsce;
	}
	public void setOpis(String opis){
		this.opis=opis;
	}
	
	//metoda do do odczytu get
	public String getNazwa(){
		return nazwa;
	}
	public LocalDate getData(){
		return data;
	}
	public String getMiejsce(){
		return miejsce;
	}
	public String getOpis(){
		return opis;
	}
	
	//metoda do wyswietlania toString()
	@Override
	public String toString(){
	    return "Zdarzenie {" +
	            "nazwa='" + nazwa + '\'' +
	            ", data=" + data +
	            ", miejsce='" + miejsce + '\'' +
	            ", opis='" + opis + '\'' +
	            '}';
	}
}