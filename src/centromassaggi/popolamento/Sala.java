package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;

public class Sala extends Utils {

    String numeroSala;
    int numeroLettini;

    public Sala(Random R) {
        numeroSala = randomNumeralString(1 + R.nextInt(20), R);
        numeroLettini = 2 + R.nextInt(3); // Circa 3 lettini per sala
    }

    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into sala(numeroSala, numeroLettini)");
        temp.append("values(");
        temp.append("'").append(numeroSala).append("',");
        temp.append(numeroLettini);
        temp.append(")");
        return temp.toString();
    }

    public String getSala() {
        return numeroSala;
    }

    @Override
    public String toString() {
        return numeroSala + " " + numeroLettini;
    }

}
