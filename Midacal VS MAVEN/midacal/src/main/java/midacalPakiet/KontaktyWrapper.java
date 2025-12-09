package midacalPakiet;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "kontakty")
public class KontaktyWrapper {

    private List<Kontakt> kontakty;

    @XmlElement(name = "kontakt")
    public List<Kontakt> getKontakty() {
        return kontakty;
    }

    public void setKontakty(List<Kontakt> kontakty) {
        this.kontakty = kontakty;
    }
}
