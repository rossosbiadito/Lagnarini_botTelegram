import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
public class ConfigurationSingleton {
    private static ConfigurationSingleton instance;
    private Configuration config;

    // Costruttore privato: carica il file config.properties una sola volta
    private ConfigurationSingleton() {
        Configurations configurations = new Configurations();
        try {
            config = configurations.properties("config.properties");
        } catch (ConfigurationException e) {
            System.err.println("File non esiste, stai attento!");
            System.exit(-1);
        }
    }

    // Restituisce l'istanza unica del singleton
    public static ConfigurationSingleton getInstance() {
        if (instance == null)
            instance = new ConfigurationSingleton();
        return instance;
    }

    // Legge il valore di una chiave dal file di configurazione
    public String getProperty(String key) {
        return config.getString(key);
    }
}
