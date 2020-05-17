package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;

public class RecapitoMassaggiatore extends Utils {

    String massaggiatore;
    String telefono;

    public RecapitoMassaggiatore(Random R) {
        massaggiatore = randomString(16, R);
        telefono = this.randomNumeralString(13, R);
    }
    
    public RecapitoMassaggiatore(Random R, String massaggiatore) {
        this.massaggiatore = massaggiatore;
        telefono = this.randomNumeralString(13, R);
    }
    
    public RecapitoMassaggiatore(String telefono, Random R) {
        massaggiatore = randomString(16, R);
        this.telefono = telefono;
    }

    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into recapitomassaggiatore(massaggiatore, telefono)");
        temp.append("values(");
        temp.append("'").append(massaggiatore).append("',");
        temp.append("'").append(telefono).append("'");
        temp.append(")");
        return temp.toString();
    }

    public String getMassaggiatore() {
        return massaggiatore;
    }

    public String getTelefono() {
        return telefono;
    }
    
    @Override
    public String toString() {
        return massaggiatore + " " + telefono;
    }
}
