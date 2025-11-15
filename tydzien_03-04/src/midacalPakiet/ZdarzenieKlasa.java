package midacalPakiet;
import java.time.LocalDate;

public class ZdarzenieKlasa{
	private String nazwa;
	private LocalDate data;
	private String miejsce;
	private String opis;
}

//konstruktor bezargumentowy
public ZdarzenieKlasa(){
}

//konstruktor g³ówny do tworzenia obiektów
public ZdarzenieKlasa{
	this.nazwa=nazwa;
	this.data=data;
	this.miejsce=miejsce;
	this.opis=opis;
}

//metoda zapisu set
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

//metoda zapisu get
public void getNazwa(String nazwa){
	this.nazwa=nazwa;
}
public void getData(LocalDate data){
	this.data=data;
}
public void getMiejsce(String miejsce){
	this.miejsce=miejsce;
}
public void getOpis(String opis){
	this.opis=opis;
}