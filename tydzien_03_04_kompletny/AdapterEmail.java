package midacalPakiet;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.mail.internet.InternetAddress;

public class AdapterEmail extends XmlAdapter<String, InternetAddress> {

    @Override
    public InternetAddress unmarshal(String v) throws Exception {
        if (v == null || v.trim().isEmpty()) {
            return null;
        }
        return new InternetAddress(v);
    }

    @Override
    public String marshal(InternetAddress v) throws Exception {
        if (v == null) {
            return null;
        }
        return v.toString();
    }
}
