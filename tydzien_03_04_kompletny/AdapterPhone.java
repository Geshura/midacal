package midacalPakiet;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;

public class AdapterPhone extends XmlAdapter<String, PhoneNumber> {

    private PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    @Override
    public PhoneNumber unmarshal(String v) throws Exception {
        return phoneUtil.parse(v, "PL");
    }

    @Override
    public String marshal(PhoneNumber v) throws Exception {
        return phoneUtil.format(v, PhoneNumberUtil.PhoneNumberFormat.E164);
    }
}
