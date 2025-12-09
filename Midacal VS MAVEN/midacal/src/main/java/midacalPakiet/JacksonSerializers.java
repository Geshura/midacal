package midacalPakiet;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import com.google.i18n.phonenumbers.NumberParseException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

/**
 * Klasa zbierająca statyczne wewnętrzne klasy implementujące Jackson JsonSerializer/JsonDeserializer 
 * dla typów, które Jackson domyślnie serializuje nieprawidłowo (PhoneNumber, InternetAddress).
 */
public class JacksonSerializers {

    // --- Serializacja/Deserializacja PhoneNumber ---
    
    public static class PhoneNumberSerializer extends JsonSerializer<PhoneNumber> {
        private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        @Override
        public void serialize(PhoneNumber value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                // Serializacja do formatu E.164 (np. +48501101101)
                gen.writeString(phoneUtil.format(value, PhoneNumberUtil.PhoneNumberFormat.E164));
            } else {
                gen.writeNull();
            }
        }
    }

    public static class PhoneNumberDeserializer extends JsonDeserializer<PhoneNumber> {
        private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        @Override
        public PhoneNumber deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getText();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                // Deserializacja, zakładając domyślny region "PL" do parsowania
                return phoneUtil.parse(value, "PL");
            } catch (NumberParseException e) {
                throw new IOException("Błąd podczas parsowania numeru telefonu: " + value, e);
            }
        }
    }

    // --- Serializacja/Deserializacja InternetAddress ---

    public static class InternetAddressSerializer extends JsonSerializer<InternetAddress> {
        @Override
        public void serialize(InternetAddress value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value != null) {
                // Serializacja do prostego adresu e-mail (String)
                gen.writeString(value.getAddress());
            } else {
                gen.writeNull();
            }
        }
    }

    public static class InternetAddressDeserializer extends JsonDeserializer<InternetAddress> {
        @Override
        public InternetAddress deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getText();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            try {
                return new InternetAddress(value);
            } catch (AddressException e) {
                throw new IOException("Błąd podczas parsowania adresu e-mail: " + value, e);
            }
        }
    }
}