import javax.sql.DataSource;
import org.postgresql.ds.PGSimpleDataSource;

public class PostgresDatasource {
    public static DataSource createDataSourceFromEnv() {
        String host = System.getenv().getOrDefault("DB_HOST", "localhost");
        int port = Integer.parseInt(System.getenv().getOrDefault("DB_PORT", "5432"));
        String db = System.getenv().getOrDefault("DB_NAME", "aula_db");
        String user = System.getenv().getOrDefault("DB_USER", "postgres");
        String pass = System.getenv().getOrDefault("DB_PASS", "postgres");

        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setServerNames(new String[] { host });
        ds.setPortNumbers(new int[] { port });
        ds.setDatabaseName(db);
        ds.setUser(user);
        ds.setPassword(pass);

        return ds;
    }
}