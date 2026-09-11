import javax.sql.DataSource;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapperSupplier;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;

public class App {
    public static void main(String[] args) {
        StdioServerTransportProvider transport = new StdioServerTransportProvider(
            new JacksonMcpJsonMapperSupplier().get()
        );

        var server = McpServer.sync(transport)
                .serverInfo("mcp-postgres-aula", "1.0.0")
                .capabilities(ServerCapabilities.builder().tools(true).build())
                .build();

            MCPDatabaseServer dbServer = new MCPDatabaseServer(server, prepareAlumnoService());
            dbServer.registerTools();
    }

    private static AlumnoService prepareAlumnoService() {
        DataSource d = PostgresDatasource.createDataSourceFromEnv();
        AlumnoRepository alumnoRepository = new AlumnoRepository(d);
        return new AlumnoService(alumnoRepository);
    }
}
