package midacalPakiet;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(name = "zdarzenie")
@XmlAccessorType(XmlAccessType.FIELD)
public class Zdarzenie implements Comparable<Zdarzenie>{
	private String nazwa;
    @XmlJavaTypeAdapter(value = AdapterData.class)
	private LocalDate data;
	private String miejsce;
	private String opis;

	//konstruktor bezargumentowy
	public Zdarzenie(){
	}
	
	//konstruktor glowny do tworzenia obiektow
	public Zdarzenie(String nazwa, LocalDate data, String miejsce, String opis){
		this.nazwa=nazwa;
		this.data=data;
		this.miejsce=miejsce;
		this.opis=opis;
	}
	
	//metody do zapisu set
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
	
	//metody do odczytu get
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
	            ", miejsce='" + miejsce + "'" + 
	            ", opis='" + opis + "'" + 
	            '}';
	}
}
