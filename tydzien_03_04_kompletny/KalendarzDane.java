package midacalPakiet;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "kalendarz")
public class KalendarzDane {

    private List<Zdarzenie> zdarzenia;
    private List<Kontakt> kontakty;

    @XmlElement(name = "zdarzenie")
    public List<Zdarzenie> getZdarzenia() {
        return zdarzenia;
    }

    public void setZdarzenia(List<Zdarzenie> zdarzenia) {
        this.zdarzenia = zdarzenia;
    }

    @XmlElement(name = "kontakt")
    public List<Kontakt> getKontakty() {
        return kontakty;
    }

    public void setKontakty(List<Kontakt> kontakty) {
        this.kontakty = kontakty;
    }
}
