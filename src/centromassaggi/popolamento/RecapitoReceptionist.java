package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;

public class RecapitoReceptionist extends Utils {

    String receptionist;
    String telefono;

    public RecapitoReceptionist(Random R) {
        receptionist = randomString(16, R);
        telefono = this.randomNumeralString(13, R);
    }
    
    public RecapitoReceptionist(Random R, String receptionist) {
        this.receptionist = receptionist;
        telefono = this.randomNumeralString(13, R);
    }
    
    public RecapitoReceptionist(String telefono, Random R) {
        receptionist = randomString(16, R);
        this.telefono = telefono;
    }

    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into recapitoreceptionist(receptionist, telefono)");
        temp.append("values(");
        temp.append("'").append(receptionist).append("',");
        temp.append("'").append(telefono).append("'");
        temp.append(")");
        return temp.toString();
    }

    public String getReceptionist() {
        return receptionist;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return receptionist + " " + telefono;
    }
}
