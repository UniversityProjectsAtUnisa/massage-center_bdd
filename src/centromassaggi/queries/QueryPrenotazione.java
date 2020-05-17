package centromassaggi.queries;

import centromassaggi.common.CustomManager;
import centromassaggi.common.Helpers;
import java.time.format.DateTimeFormatter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;
import java.util.ArrayList;
import java.time.*;
import java.util.InputMismatchException;
import java.util.Iterator;

public class QueryPrenotazione {
    
    /*
        Restituisce una ListaOrari che rappresenta le fasce orarie all'interno delle quali non sono disponibili sufficienti macchinari
        per eseguire il tipo di massaggio richiesto, dati il tipo massaggio ed una data.
        Qualora il tipo di massaggio non preveda l'uso di un macchinario restituisce una lista di orari vuota. 
    */
    private static ListaOrari getOrariMacchinariOccupati(Statement stm, String tipoMassaggio, LocalDate data) {
        ListaOrari orariMacchinariOccupati = new ListaOrari();

        try {
            String queryMacchinario = String.format("SELECT Macchinario FROM TipoMassaggio WHERE tipo = '%s';", tipoMassaggio);
            ResultSet rst = stm.executeQuery(queryMacchinario); 
            rst.next();
            String macchinario = rst.getString("macchinario");
            if (macchinario == null) {
                return orariMacchinariOccupati;
            }

            String queryQuantitaMacchinario = String.format("SELECT Quantita FROM Macchinario WHERE tipo = '%s';", macchinario);
            rst = stm.executeQuery(queryQuantitaMacchinario);
            rst.next();
            int quantitaMacchinario = rst.getInt("quantita");
            if (quantitaMacchinario == 0) {
                return ListaOrari.orariDiApertura(data.isEqual(LocalDate.now()));
            }
            String queryOrariMacchinariOccupati = String.format(
                      "SELECT OraInizio AS Ora, tipoOra FROM ("
                    + "     SELECT OraInizio, -1 tipoOra FROM Massaggio "
                    + "     WHERE DataMassaggio = DATE '%s' "
                    + "     AND TipoMassaggio IN ( "
                    + "         SELECT Tipo FROM TipoMassaggio WHERE Macchinario = '%s') "
                    + "     UNION ALL "
                    + "     SELECT OraFine, 1 tipoOra FROM Massaggio "
                    + "     WHERE DataMassaggio = DATE '%s' "
                    + "     AND TipoMassaggio IN ( "
                    + "         SELECT Tipo FROM TipoMassaggio WHERE Macchinario = '%s') "
                    + ") AS O "
                    + "ORDER BY Ora;", 
                    data, macchinario, data, macchinario);
            rst = stm.executeQuery(queryOrariMacchinariOccupati);

            LocalTime oraInizio = null, oraFine;
            while (rst.next()) {
                LocalTime ora = rst.getTime("ora").toLocalTime();
                quantitaMacchinario += rst.getInt("tipoOra");
                if (quantitaMacchinario <= 0) {
                    oraInizio = ora;
                } else if (oraInizio != null) {
                    oraFine = ora;
                    orariMacchinariOccupati.add(oraInizio, oraFine);
                    oraInizio = null;
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return orariMacchinariOccupati;
    }


    /*
        Restituisce una ListaOrari che rappresta le fasce orarie all'interno delle quali non sono disponibili sufficienti massaggiatori
        per eseguire un determinato tipo di massaggio dati una data ed il tipo massaggio.
    */
    private static ListaOrari getOrariMassaggiatoriOccupati(Statement stm, String tipoMassaggio, LocalDate data) {
        ListaOrari orariMassaggiatoriOccupati = new ListaOrari();
        try {
            String queryQuantitaMassaggiatoriAbili = String.format(
                                                                    "SELECT count(*) AS quantita FROM specializzazione "
                                                                  + "WHERE tipomassaggio = '%s';", 
                                                                     tipoMassaggio);
            ResultSet rst = stm.executeQuery(queryQuantitaMassaggiatoriAbili);
            rst.next();
            int quantitaMassaggiatoriAbili = rst.getInt("quantita");

            if (quantitaMassaggiatoriAbili == 0) {
                return ListaOrari.orariDiApertura(data.isEqual(LocalDate.now()));
            }
            String queryMassaggiatoriOccupati = String.format(
                    "SELECT OraInizio AS Ora, tipoOra FROM ( "
                    +           "SELECT OraInizio, -1 tipoOra FROM Massaggio "
                    +           "WHERE TipoMassaggio ='%s' "
                    +           "AND DataMassaggio = DATE '%s' "
                    +           "UNION ALL "
                    +           "SELECT OraFine, 1 tipoOra FROM Massaggio "
                    +           "WHERE TipoMassaggio = '%s' "
                    +           "AND DataMassaggio = DATE '%s' "
                    + ") AS O "
                    + "ORDER BY Ora;",
                    tipoMassaggio, data, tipoMassaggio, data);

            rst = stm.executeQuery(queryMassaggiatoriOccupati);
            LocalTime oraInizio = null, oraFine;
            while (rst.next()) {
                LocalTime ora = rst.getTime("ora").toLocalTime();
                quantitaMassaggiatoriAbili += rst.getInt("tipoOra");
                if (quantitaMassaggiatoriAbili <= 0) {
                    oraInizio = ora;
                } else if (oraInizio != null) {
                    oraFine = ora;
                    orariMassaggiatoriOccupati.add(oraInizio, oraFine);
                    oraInizio = null;
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return orariMassaggiatoriOccupati;
    }

    /*
        Restituisce una ListaOrari che rappresenta le fasce orarie all'interno delle quali non sono disponibili
        sufficienti lettini per eseguire un massaggio in una certa data.
    */
    private static ListaOrari getOrariLettiniOccupati(Statement stm, LocalDate data) {
        ListaOrari orariLettiniOccupati = new ListaOrari();
        try {
            String queryTotaleLettini = "SELECT sum(numeroLettini) AS quantita FROM Sala;";
            ResultSet rst = stm.executeQuery(queryTotaleLettini);
            rst.next();
            int totaleLettini = rst.getInt("quantita");

            if (totaleLettini == 0) {
                return ListaOrari.orariDiApertura(data.isEqual(LocalDate.now()));
            }
            String queryLettiniOccupati = String.format(
                    "SELECT OraInizio AS Ora, tipoOra FROM ("
                    + "         SELECT OraInizio, -1 tipoOra FROM Massaggio "
                    + "         WHERE DataMassaggio = DATE '%s' "
                    + "         UNION ALL "
                    + "         SELECT OraFine, 1 tipoOra FROM Massaggio "
                    + "         WHERE DataMassaggio = DATE '%s' "
                    + "         ) AS O "
                    + "ORDER BY Ora;",
                    data, data);

            rst = stm.executeQuery(queryLettiniOccupati);

            int counter = totaleLettini;
            LocalTime oraInizio = null, oraFine;
            while (rst.next()) {
                LocalTime ora = rst.getTime("ora").toLocalTime();
                counter += rst.getInt("tipoOra");
                if (counter <= 0) {
                    oraInizio = ora;
                } else if (oraInizio != null) {
                    oraFine = ora;
                    orariLettiniOccupati.add(oraInizio, oraFine);
                    oraInizio = null;
                }
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return orariLettiniOccupati;
    }

    /*
        Restistuisce una ListaOrari che rappresenta le fasce orarie all'interno delle quali l'utente è già impegnato in altri massaggi
        dati una data ed un cliente.
    */
    private static ListaOrari getOrariClienteOccupato(Statement stm, String cliente, LocalDate data) {
        ListaOrari orariClienteOccupato = new ListaOrari();
        try {
            String queryOrariClienteOccupato = String.format(
                                               "SELECT OraInizio, OraFine FROM Massaggio "
                                             + "WHERE Cliente = '%s' "
                                             + "AND DataMassaggio = DATE '%s' "
                                             + "ORDER BY OraInizio;",
                                                cliente, data);
            ResultSet rst = stm.executeQuery(queryOrariClienteOccupato);

            LocalTime oraInizio, oraFine;
            while (rst.next()) {
                oraInizio = rst.getTime("orainizio").toLocalTime();
                oraFine = rst.getTime("orafine").toLocalTime();
                orariClienteOccupato.add(oraInizio, oraFine);
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return orariClienteOccupato;
    }

    /*
        Restituisce una ListaOrari che rappresentano le fasce orarie all'interno delle quali può essere prenotato il massaggio
        da un dato cliente, per un dato giorno di un certo tipo di massaggio.
    */
    public static ListaOrari getOrariDisponibili(Statement stm, LocalDate data, String tipoMassaggio, String cliente) {

        ListaOrari centroMassaggiAperto = ListaOrari.orariDiApertura(data.isEqual(LocalDate.now()));

        ListaOrari orariMacchinariOccupati =     getOrariMacchinariOccupati(stm, tipoMassaggio, data);
        ListaOrari orariMassaggiatoriOccupati =  getOrariMassaggiatoriOccupati(stm, tipoMassaggio, data);
        ListaOrari orariLettiniOccupati =        getOrariLettiniOccupati(stm, data);
        ListaOrari orariClienteOccupato =        getOrariClienteOccupato(stm, cliente, data);

        ListaOrari orariDisponibili = centroMassaggiAperto.sottraiOrari(orariMassaggiatoriOccupati)
                                                .sottraiOrari(orariMacchinariOccupati)
                                                .sottraiOrari(orariLettiniOccupati)
                                                .sottraiOrari(orariClienteOccupato);

        return orariDisponibili;

    }

    /*
        Dati una data, un tipo massaggio ed un cliente, si vogliono conoscere le fasce orarie all'interno delle quali è possibile prenotare
        un massaggio, tenendo conto della possibilità che possano non esserci sufficienti macchinari, massaggiatori in grado di eseguire il
        tipo massaggio richiesto, lettini liberi (e quindi sale libere), nascondendo anche gli orari per i quali il cliente ha già prenotato
        un massaggio, compatibilmente con gli orari di apertura del centro massagio
     */
    public static ListaOrari getOrariPrenotabili(Statement stm, LocalDate data, String tipoMassaggio, String cliente) {
        ListaOrari orariDisponibili = getOrariDisponibili(stm, data, tipoMassaggio, cliente);

        // A partire dalle fasce orarie all'interno delle quali è possibile effettuare il massaggio richiesto 
        // ottiene le fasce orarie all'interno delle quali è possibile effetutare una prenotazione.

        String queryDurataMassaggio = String.format(
                                                    "SELECT durata FROM TipoMassaggio "
                                                    + "WHERE Tipo = '%s';",
                                                    tipoMassaggio);
        try {
            ResultSet rst = stm.executeQuery(queryDurataMassaggio);
            rst.next();
            int durata = rst.getInt("durata");


            Iterator i = orariDisponibili.iterator();
            while(i.hasNext()) {
                Orario o = (Orario) i.next();
                if (o.getOraFine().compareTo(o.getOraInizio().plusMinutes(durata)) < 0) {
                    i.remove();
                }
                else o.setOraFine(o.getOraFine().minusMinutes(durata));
            }
        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
        }
        return orariDisponibili;
    }

    /*
        Permette di prenotare un massaggio dati un cliente, un tipo di massaggio, una data, un'ora di inizio del massaggio
        ottenuti in input dall'utente, dopo avergli fornito dati sufficienti ad effettuare la sua scelta.
        Restituisce una stringa contenente il massaggio prenotato formattato.
    */
    public static String prenotaMassaggio(Connection conn) throws SQLException {
        Statement stm = conn.createStatement();

        Scanner sc = new Scanner(System.in);
        String ans = "";
        String codiceFiscale = "";

        System.out.print("Il cliente che vuole prenotare un massaggio esiste già? (s/n): ");
        while (!ans.equals("s") && !ans.equals("n")) {
            ans = sc.nextLine();
        }

        // Se l'utente non è ancora stato inserito nel database lo inserisco
        if (ans.equals("n")) {
            boolean utenteInserito = false;
            while(!utenteInserito){
                System.out.print("Inserire il codice fiscale: ");
                codiceFiscale = sc.nextLine();
                while(codiceFiscale.length() != 16) {
                    System.out.println("Il codice fiscale deve essere di 16 caratteri!");
                    System.out.print("Inserire il codice fiscale: ");
                    codiceFiscale = sc.nextLine();
                }

                System.out.print("Inserire il cognome: ");
                String cognome = sc.nextLine();
                System.out.print("Inserire il nome: ");
                String nome = sc.nextLine();
                
                ArrayList<String> recapiti = new ArrayList<>();
                System.out.println("Inserire almeno un recapito telefonico, inserire 0 per terminare: ");
                do {
                    ans = sc.next();
                    if (!ans.equals("0")) {
                        recapiti.add(ans);
                    }
                    sc.nextLine();
                } while (!ans.equals("0"));
                
            
                try {
                    utenteInserito = Helpers.inserisciCliente(conn, codiceFiscale, cognome, nome, recapiti);
                } catch (SQLException ex) {
                    System.out.println("Codice fiscale già esistente. Riprovare.");
                }
            }   
            
        } else {
            System.out.print("Inserire il codice fiscale: ");
            codiceFiscale = sc.nextLine();
            while (!Helpers.clienteContiene(stm, codiceFiscale)) {
                System.out.print("Il codice fiscale non esiste, inserire un codice fiscale esistente: ");
                codiceFiscale = sc.nextLine();
            }
        }

        ListaOrari listaOrariPrenotabili;
        String tipoMassaggio;
        LocalDate data;
        do {
            // Chiede la data in cui si vuole prenotare il massaggio
            System.out.print("Inserire la data in cui vorresti prenotare un massaggio (formato gg/mm/aaaa): ");
            String dataStringa = sc.nextLine();
            // Se la data non è nel formato gg/mm/aaaa o se è una data passata la chiede nuovamente
            while(!dataStringa.matches(Helpers.REGEX_DATA_COMPLETA)
                    || LocalDate.parse(dataStringa, DateTimeFormatter.ofPattern("dd/MM/yyyy")).isBefore(LocalDate.now())){ 
                System.out.print("Inserire una data non passata nel formato corretto (formato gg/mm/aaaa): ");
                dataStringa = sc.nextLine();
            }
            data = LocalDate.parse(dataStringa, DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            System.out.println("Inserire un tipo massaggio tra quelli che verranno mostrati: ");
            System.out.println(Helpers.getTipiMassaggio(stm));

            System.out.print("\nTipo massaggio: ");
            tipoMassaggio = sc.nextLine();
            while (!Helpers.tipoMassaggioContiene(stm, tipoMassaggio)) {
                System.out.print("Il tipo massaggio non esiste, inserire un tipo massaggio tra quelli mostrati sopra: ");
                tipoMassaggio = sc.nextLine();
            }

            listaOrariPrenotabili = QueryPrenotazione.getOrariPrenotabili(stm, data, tipoMassaggio, codiceFiscale);
            if (listaOrariPrenotabili.isEmpty()) {
                System.out.println("Non ci sono orari disponibili nella data richiesta per il tipo massaggio richiesto.");
            }
        } while(listaOrariPrenotabili.isEmpty());
        

        System.out.println(String.format("\nDi seguito saranno mostrati gli orari per cui "+ 
                            "si può prenotare un massaggio di tipo %s in data %s.", tipoMassaggio, data));
        System.out.println("NOTA: Il massaggio inizierà all'ora selezionata, "
                        + "ma a seconda della durata, potrebbe protrarsi anche al di fuori degli intervalli suggeriti.");
                        
        System.out.println(listaOrariPrenotabili);


        System.out.print("Inserire l'orario in cui si vuole cominciare il tuo massaggio: ");
        int prenotazioniEffettuate = 0;
        LocalTime ora = null;
        while(prenotazioniEffettuate == 0) {
            try {
                String oraStringa = sc.nextLine();
                while(!oraStringa.matches(Helpers.REGEX_ORA)) {
                    System.out.print("Inserire l'ora nel formato corretto (formato hh:mm): ");
                    oraStringa = sc.nextLine();
                }
                ora = LocalTime.parse(oraStringa);
                String insertPrenotazioneQuery = String.format("insert into prenotazione(cliente,datamassaggio,orainizio,tipomassaggio) "
                                                            + "values('%s', '%s', '%s', '%s');",
                                                            codiceFiscale, data, ora, tipoMassaggio);
                prenotazioniEffettuate = stm.executeUpdate(insertPrenotazioneQuery);
                System.out.println("\nPrenotazione effettuata con successo.");
            } catch(SQLException ex){
                System.out.print("L'orario inserito non era corretto, inserire un orario tra quelli suggeriti: ");   
            }
        }

        return Helpers.getMassaggio(stm, codiceFiscale, data, ora);
    }


    /*
        Ottiene la connessione al database e la passa al metodo prenotaMassaggio.
        Poi stampa il massaggio prenotato.
    */
    public static void main(String[] args) {
        try (Connection conn = CustomManager.getConnection()) {

            String massaggioPrenotato = QueryPrenotazione.prenotaMassaggio(conn);
            System.out.println(massaggioPrenotato);
            
        } catch (SQLException | InputMismatchException ex) {
            System.err.println(ex.getMessage());
        }
    }

} 