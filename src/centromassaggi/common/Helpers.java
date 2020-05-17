package centromassaggi.common;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

/*
    Classe che fornisce vari metodi statici per effettuare query di base.
*/
public class Helpers {

    public static final String REGEX_ORA = "^(21:00|(09|1[0-9]|20):[0-5][0-9])$";
    public static final String REGEX_DATA_COMPLETA = "^(3[01]|[12][0-9]|0[1-9])/(1[0-2]|0[1-9])/20[0-9]{2}$";
    public static final String REGEX_DATA_ANNO_MESE = "^(1[0-2]|0[1-9])/20[0-9]{2}$";


    /*
        Visualizza in ordine i codici fiscali di tutti i massaggiatori.
    */
    public static String getMassaggiatori(Statement stm) {
        String s = "";
        try {
            String query = "SELECT codicefiscale FROM massaggiatore ORDER BY codicefiscale;";
            ResultSet rst = stm.executeQuery(query);

            s += "\n----------------";
            s += String.format("\n%-16s", "Massaggiatori");
            s += "\n----------------";
            while(rst.next()) {
                s += String.format("\n%-16s", rst.getString("codicefiscale"));
            }
            s += "\n----------------";
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return s;
    }

    /*
        Visualizza in ordine i codici fiscali di tutti i dipendenti.
    */
    public static String getDipendenti(Statement stm) {
        String s = "";
        try {
            String query = "SELECT codicefiscale FROM massaggiatore "
                         + "UNION "
                         + "SELECT codicefiscale FROM receptionist "
                         + "ORDER BY codicefiscale;";
            ResultSet rst = stm.executeQuery(query);

            s += "\n----------------";
            s += String.format("\n%-16s", "Dipendente");
            s += "\n----------------";
            while(rst.next()) {
                s += String.format("\n%-16s", rst.getString("codicefiscale"));
            }
            s += "\n----------------";
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return s;
    }

    /*
        Visualizza tipo, prezzo, durata e nacchinario di tutti i tipi massaggio.
    */
    public static String getTipiMassaggio(Statement stm) {
        String selectTipiMassaggio = String.format("SELECT Tipo AS TipoMassaggio, Prezzo, Durata, Macchinario FROM TipoMassaggio;");

        String result = "";
        try {
            ResultSet rst = stm.executeQuery(selectTipiMassaggio);
            result += "\n---------------------------------------------------------------";
            result += String.format("\n%-20s %-8s %-12s %20s", "TipoMassaggio", "Prezzo", "Durata (min)", "Macchinario");
            result += "\n---------------------------------------------------------------";
            while(rst.next()) {
                String macchinario = rst.getString("Macchinario");
                if(macchinario == null) {
                    macchinario = "nessuno";
                }
                result += String.format("\n%-20s %-8.2f %-12d %20s",
                        rst.getString("TipoMassaggio"), rst.getDouble("Prezzo"), rst.getInt("Durata"), macchinario);
            }
            result += "\n---------------------------------------------------------------";
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return result;
    }

    /*
        Visualizza data massaggio, ora inizio, ora fine, tipo massaggio, massaggiatore, sala, prezzo di un massaggio dato un cliente, una data ed un orario.
    */
    public static String getMassaggio(Statement stm, String cliente, LocalDate data, LocalTime ora) {
        String s = "";
        try {
            String query = String.format("SELECT M.DataMassaggio, M.OraInizio, M.OraFine, M.TipoMassaggio, M.Massaggiatore, M.Sala, T.Prezzo FROM Massaggio M "
            + "INNER JOIN TipoMassaggio T "
            + "ON M.TipoMassaggio = T.Tipo "
            + "WHERE Cliente='%s' AND DataMassaggio = '%s' AND OraInizio = '%s';",
            cliente, data, ora);
            ResultSet rst = stm.executeQuery(query);

                s += "\n--------------------------------------------------------------------------------------------------------";
                s += String.format("\n%-10s %-10s %-10s %-20s %-20s %-20s %8s", "Data", "Ora Inizio", "Ora Fine", "Tipo Massaggio", "Massaggiatore", "Sala", "Prezzo");
                s += "\n--------------------------------------------------------------------------------------------------------";
                while(rst.next()) {
                    s += String.format("\n%-10s %-10s %-10s %-20s %-20s %-20s %8.2f", 
                    rst.getString("datamassaggio"), rst.getString("orainizio"), rst.getString("orafine"), 
                    rst.getString("tipomassaggio"), rst.getString("massaggiatore"), rst.getString("sala"), rst.getDouble("prezzo"));
                }
                s += "\n--------------------------------------------------------------------------------------------------------";
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return s;
    }

    /*
        Verifica la presenza del tipo dato in input nella tabella tipoMassaggio.
    */
    public static boolean tipoMassaggioContiene(Statement stm, String tipo) {

        String queryTipoMassaggioTipo = String.format(
            "SELECT * FROM TipoMassaggio WHERE Tipo = '%s';", 
            tipo);
        try {
            ResultSet rst = stm.executeQuery(queryTipoMassaggioTipo);
            return rst.next();
        } catch (SQLException ex) {
            return false;
        }
    }

    /*
        Verifica la presenza del codice fiscale dato in input nella tabella cliente.
    */
    public static boolean clienteContiene(Statement stm, String codiceFiscale) {

        String queryClienteCodiceFiscale = String.format(
            "SELECT * FROM Cliente WHERE CodiceFiscale = '%s';", 
            codiceFiscale);
        try {
            ResultSet rst = stm.executeQuery(queryClienteCodiceFiscale);
            return rst.next();
        } catch (SQLException ex) {
            return false;
        }

    }

    /*
        Inserisce un nuovo cliente, avendo cura di utilizzare una transaction per rispettare il vincolo secondo il quale
        un cliente deve avere almeno un recapito telefonico.
        Prende in input codice fiscale, cognome, nome ed una lista di recapiti telefonici da inserire.
    */
    public static boolean inserisciCliente(Connection conn, String codiceFiscale, String cognome, String nome, ArrayList<String> recapiti) throws SQLException {
        conn.setAutoCommit(false);
        Statement stm = conn.createStatement();

        String insertRecapitoQuery = "insert into recapitocliente(cliente, telefono) values";
        while (!recapiti.isEmpty()) {
            insertRecapitoQuery += String.format("('%s', '%s'),", codiceFiscale, recapiti.remove(0));
        }
        insertRecapitoQuery = insertRecapitoQuery.substring(0, insertRecapitoQuery.length() - 1) + ';';
        
        try {
            stm.executeUpdate(insertRecapitoQuery);
            String insertClienteQuery = String.format(
                    "insert into cliente(codicefiscale,cognome,nome) " + "values('%s', '%s', '%s');", codiceFiscale,
                    cognome, nome);
            stm.executeUpdate(insertClienteQuery);
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        conn.commit();
        conn.setAutoCommit(true);
        return true;
    }

}
