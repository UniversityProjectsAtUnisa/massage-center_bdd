package centromassaggi.popolamento;

import centromassaggi.common.Utils;
import java.util.Random;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Massaggio extends Utils {

    String cliente;
    LocalDate dataMassaggio;
    LocalTime oraInizio;
    String tipoMassaggio;

    public Massaggio(Random R, String cliente, String tipoMassaggio, LocalDate dataMassaggio) {
        this.cliente = cliente;
        this.dataMassaggio = dataMassaggio;
        this.tipoMassaggio = tipoMassaggio;
        oraInizio = LocalTime.of(9 + R.nextInt(10), R.nextInt(4) * 15);
    }

    @Override
    public String getInsertQuery() {
        StringBuilder temp = new StringBuilder();
        temp.append("insert into prenotazione(cliente, dataMassaggio, oraInizio, tipoMassaggio)");
        temp.append("values(");
        temp.append("'").append(cliente).append("',");
        temp.append("DATE '").append(dataMassaggio).append("',");
        temp.append("TIME '").append(oraInizio.format(DateTimeFormatter.ofPattern("HH:mm"))).append("',");
        temp.append("'").append(tipoMassaggio).append("'");
        temp.append(")");
        return temp.toString();
    }

    @Override
    public String toString() {
        return cliente + " " + dataMassaggio + " " + oraInizio + " " + tipoMassaggio;
    }
}
