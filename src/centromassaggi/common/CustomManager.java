package centromassaggi.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*
    Classe di appoggio per ottenere una connessione senza ridefinire url e credenziali.

    Prima di utilizzare questa connessione assicurarsi di aver creato un database locale
    chiamato "centromassaggi" e di aver lanciato lo script di creazione, presente
    nella root del progetto con il nome "scriptcreazione.sql".

    Per maggiori informazioni seguire la guida a https://github.com/marco741/CentroMassaggiBDD
*/
public class CustomManager {
    static String url = "jdbc:postgresql://localhost/centromassaggi";
    static String user = "tonino";
    static String pwd = "Pippo";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, pwd);
    }
}