package midacalPakiet;

import java.net.URI;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class AdapterLokalizacja extends XmlAdapter<String, URI> {

    @Override
    public URI unmarshal(String v) throws Exception {
        if (v == null || v.trim().isEmpty()) return null;
        try {
            return new URI(v);
        } catch (Exception e) {
            // jeśli to nie jest poprawny URI, spróbuj stworzyć prosty one z escape
            return new URI(v.replaceAll(" ", "%20"));
        }
    }

    @Override
    public String marshal(URI v) throws Exception {
        if (v == null) return null;
        return v.toString();
    }
}
