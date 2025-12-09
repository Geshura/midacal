package midacalPakiet;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "kalendarz")
public class KalendarzDane {

    // Używamy JacksonXmlElementWrapper do utworzenia elementu opakowującego listę (np. <zdarzenia>...</zdarzenia>)
    @JacksonXmlElementWrapper(localName = "zdarzenia")
    // Używamy JsonProperty, aby nazwać poszczególne elementy wewnątrz listy (np. <zdarzenie>)
    @JsonProperty("zdarzenie")
    private List<Zdarzenie> listaZdarzen;

    @JacksonXmlElementWrapper(localName = "kontakty")
    @JsonProperty("kontakt")
    private List<Kontakt> listaKontaktow;

    public List<Zdarzenie> getListaZdarzen() {
        return listaZdarzen;
    }

    public void setListaZdarzen(List<Zdarzenie> listaZdarzen) {
        this.listaZdarzen = listaZdarzen;
    }

    public List<Kontakt> getListaKontaktow() {
        return listaKontaktow;
    }

    public void setListaKontaktow(List<Kontakt> listaKontaktow) {
        this.listaKontaktow = listaKontaktow;
    }
}