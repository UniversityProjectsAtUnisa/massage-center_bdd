package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;

public class RecapitoCliente extends Utils {

    String cliente;
    String telefono;

    public RecapitoCliente(Random R) {
        cliente = randomString(16, R);
        telefono = this.randomNumeralString(13, R);
    }
    
    public RecapitoCliente(Random R, String cliente) {
        this.cliente = cliente;
        telefono = this.randomNumeralString(13, R);
    }
    
    public RecapitoCliente(String telefono, Random R) {
        cliente = randomString(16, R);
        this.telefono = telefono;
    }

    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into recapitocliente(cliente, telefono)");
        temp.append("values(");
        temp.append("'").append(cliente).append("',");
        temp.append("'").append(telefono).append("'");
        temp.append(")");
        return temp.toString();
    }

    public String getCliente() {
        return cliente;
    }

    public String getTelefono() {
        return telefono;
    }

    
    @Override
    public String toString() {
        return cliente + " " + telefono;
    }
}
