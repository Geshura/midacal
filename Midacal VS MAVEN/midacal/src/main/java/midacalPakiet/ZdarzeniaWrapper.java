package midacalPakiet;

import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "zdarzenia")
public class ZdarzeniaWrapper {

    private List<Zdarzenie> zdarzenia;

    @XmlElement(name = "zdarzenie")
    public List<Zdarzenie> getZdarzenia() {
        return zdarzenia;
    }

    public void setZdarzenia(List<Zdarzenie> zdarzenia) {
        this.zdarzenia = zdarzenia;
    }
}
