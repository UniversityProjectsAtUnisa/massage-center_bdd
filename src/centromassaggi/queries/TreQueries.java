package centromassaggi.queries;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;

public class TreQueries {
    /*
        Prima Query:
        
        Dati un massaggiatore ed una durata, si vogliono conoscere tutti i tipi di massaggio che 
        egli è in grado di eseguire e la cui durata non è superiore rispetto a quella data.
    */
    public static String tipiMassaggioDiDurataMassima(Statement stm, String massaggiatore, int durata) {
        
        String s = "";
        try {
            String query = String.format("SELECT tipomassaggio FROM specializzazione S WHERE S.massaggiatore = '%s' "
                                       + "AND (SELECT durata FROM tipomassaggio WHERE tipo = S.tipomassaggio) <= %d;", 
                                         massaggiatore, durata);
            ResultSet rst = stm.executeQuery(query);

            s += "\n--------------------";
            s += String.format("\n%-20s", "TipiMassaggio");
            s += "\n--------------------";
            while(rst.next()) {
                s += String.format("\n%-20s", rst.getString("tipomassaggio"));
            }
            s += "\n--------------------";
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return s;
    }

    /*
        Seconda Query:
        
        Dati una data, un’ora di inizio ed un’ora di fine si vogliono conoscere i numeri di sala
        per tutte le sale in cui è disponibile almeno un lettino.

    */
    public static String saleDisponibili(Statement stm, LocalDate data, LocalTime oraInizio, LocalTime oraFine) {
        
        String s = "";
        try {
            String query = String.format("SELECT S.NumeroSala AS SalaLibera FROM Sala S "
            + "WHERE S.numeroSala NOT IN (SELECT DISTINCT sala FROM Massaggio) AND S.NumeroLettini > 0 "
            + "OR S.NumeroLettini > ( "
            + "   SELECT count(*) FROM Massaggio M "
            + "   WHERE M.datamassaggio = DATE '%s' "
            + "   AND M.orafine > TIME '%s' AND M.orainizio < TIME '%s' "
            + "   AND M.Sala = S.numeroSala);",
            data, oraInizio, oraFine);
            ResultSet rst = stm.executeQuery(query);

            s += "\n--------------------";
            s += String.format("\n%-20s", "SaleLibere");
            s += "\n--------------------";
            while(rst.next()) {
                s += String.format("\n%-20s", rst.getString("SalaLibera"));
            }
            s += "\n--------------------";
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return s;
    }

    /*
        Terza Query:
        
        Si vuole calcolare la busta paga di un dipendente a fine mese.
        Se il dipendente in questione è un Receptionist la busta paga corrisponde all'attributo "Stipendio";
        se il dipendente in questione è un Massaggiatore, la busta paga verrà calcolata come lo stipendio indicato nell'attributo "StipendioBase"
        più il 30% del costo di tutti i massaggi eseguiti durante tale mese.
        
        NOTA: In teoria si dovrebbero considerare solo le date passate per i massaggiatori in quanto non è detto che i massaggi prenotati
        non vengano cancellati o riassegnati. Tuttavia per motivi di testing (si popola solo con date future) si è deciso di permettere
        l'inserimento di date future.
    */
    public static String stipendioDipendente(Statement stm, String dipendente, LocalDate data) {
        
        String s = "";
        try {
            String query = String.format("SELECT sum(quantita * prezzo * 0.3) + StipendioBase AS StipendioDipendente FROM "
                                       		+ "(SELECT count(*) AS quantita, TipoMassaggio, T.Prezzo FROM "
                                       		+ " Massaggio M "
                                       		+ " INNER JOIN TipoMassaggio T "
                                       		+ " ON T.Tipo = M.TipoMassaggio "
                                       		+ " WHERE Massaggiatore = '%1$s' "
                                       		+ " AND EXTRACT (YEAR FROM M.DataMassaggio) = EXTRACT (YEAR FROM Date '%2$s') "
                                       		+ " AND EXTRACT (Month FROM M.DataMassaggio) = EXTRACT (Month FROM Date '%2$s') "
                                       		+ " GROUP BY TipoMassaggio, T.prezzo) AS foobar "
                                       + "INNER JOIN Massaggiatore Mass "
                                       + "ON Mass.codicefiscale = '%1$s' "
                                       + "GROUP BY StipendioBase "
                                       + "UNION "
                                       + "SELECT Stipendio FROM Receptionist WHERE CodiceFiscale = '%1$s';",
                                                   dipendente, data);
            ResultSet rst = stm.executeQuery(query);

            s += "\n--------------------";
            s += String.format("\n%-20s", "Stipendio Dipendente");
            s += "\n--------------------";
            while(rst.next()) {
                s += String.format("\n%-20.02f", rst.getDouble("StipendioDipendente"));
            }
            s += "\n--------------------";
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return s;
    }

}