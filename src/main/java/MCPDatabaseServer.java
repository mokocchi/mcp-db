import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

public class MCPDatabaseServer {
    private McpSyncServer server;
    private AlumnoService alumnoService;


    public MCPDatabaseServer(McpSyncServer server, AlumnoService alumnoService) {
        this.server = server;
        this.alumnoService = alumnoService;
    }

    public void start(){
        
    }

    public void registerTools() {
    // -------------------------------------------------------------
    // TOOL 1: Buscar Alumno por DNI (CRUD Invertido / Solo lectura)
    // -------------------------------------------------------------
    var buscarTool = new Tool(
        "buscar_alumno_por_dni",
        null,
        "Busca los datos de un alumno (nombre, curso, estado) a partir de su DNI. No expone IDs internos de BD.",
        java.util.Map.of(
            "type", "object",
            "properties", java.util.Map.of(
                "dni", java.util.Map.of(
                    "type", "string",
                    "description", "El DNI del alumno a consultar"
                )
            ),
            "required", java.util.List.of("dni"),
            "additionalProperties", false
        ),
        null,
        null,
        null,
        java.util.List.of()
    );

    // Se añade la tool al servidor junto a la lambda de ejecución
        server.addTool(new SyncToolSpecification(
            buscarTool,
            (exchange, request) -> handleBuscarAlumno(request.arguments())
        ));

        var notasTool = new Tool(
            "obtener_notas_alumno",
            null,
            "Devuelve todas las notas de un alumno y su promedio final en formato JSON.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "dni", Map.of(
                        "type", "string",
                        "description", "El DNI del alumno del que se quieren recuperar las notas"
                    )
                ),
                "required", List.of("dni"),
                "additionalProperties", false
            ),
            null,
            null,
            null,
            List.of()
        );

        server.addTool(new SyncToolSpecification(
            notasTool,
            (exchange, request) -> handleObtenerNotasAlumno(request.arguments())
        ));

        var registrarCalificacionTool = new Tool(
            "registrar_calificacion",
            null,
            "Registra la calificación de un alumno identificado por su DNI.",
            Map.of(
                "type", "object",
                "properties", Map.of(
                    "dni", Map.of(
                        "type", "string",
                        "description", "El DNI del alumno"
                    ),
                    "asignatura", Map.of(
                        "type", "string",
                        "description", "La asignatura de la calificación"
                    ),
                    "calificacion", Map.of(
                        "type", "number",
                        "description", "La calificación obtenida"
                    )
                ),
                "required", List.of("dni", "asignatura", "calificacion"),
                "additionalProperties", false
            ),
            null,
            null,
            null,
            List.of()
        );

        server.addTool(new SyncToolSpecification(
            registrarCalificacionTool,
            (exchange, request) -> handleRegistrarCalificacion(request.arguments())
        ));
    }

    private CallToolResult handleBuscarAlumno(Map<String, Object> args) {

        String dni = (String) args.get("dni");

        if (dni == null || dni.isBlank()) {
            return new CallToolResult(
                List.of(new TextContent(null, "El campo DNI es obligatorio.", null)),
                true,
                null,
                null
            );
        }

        // Llamada a nuestro servicio aislado
        var alumnoDto = alumnoService.buscarPorDni(dni);

        if (alumnoDto == null) {
            return new CallToolResult(
            List.of(new TextContent(null, "No se encontró ningún alumno con DNI: " + dni, null)),
                false,
                null,
                null
            );
        }

        // Devolvemos el resultado serializado a JSON para que la IA lo interprete
        String jsonRespuesta = String.format(
            "{\"apellido\": \"%s\", \"nombre\": \"%s\", \"curso\": \"%s\"}",
            alumnoDto.getApellido(),
            alumnoDto.getNombre(),
            alumnoDto.getCurso()
        );

        return new CallToolResult(
            List.of(new TextContent(null, jsonRespuesta, null)),
            false,
            null,
            null
        );
    }

    private CallToolResult handleObtenerNotasAlumno(Map<String, Object> args) {
        String dni = (String) args.get("dni");

        if (dni == null || dni.isBlank()) {
            return new CallToolResult(
                List.of(new TextContent(null, "El campo DNI es obligatorio.", null)),
                true,
                null,
                null
            );
        }

        String jsonRespuesta = alumnoService.obtenerNotasYPromedio(dni);

        if (jsonRespuesta == null) {
            return new CallToolResult(
                List.of(new TextContent(null, "No se encontraron notas para el DNI: " + dni, null)),
                false,
                null,
                null
            );
        }

        return new CallToolResult(
            List.of(new TextContent(null, jsonRespuesta, null)),
            false,
            null,
            null
        );
    }

    private CallToolResult handleRegistrarCalificacion(Map<String, Object> args) {
        String dni = (String) args.get("dni");
        String asignatura = (String) args.get("asignatura");
        Object calificacion = args.get("calificacion");

        if (dni == null || dni.isBlank() || asignatura == null || asignatura.isBlank()
                || !(calificacion instanceof Number)) {
            return new CallToolResult(
                List.of(new TextContent(null,
                    "Los campos DNI, asignatura y calificación son obligatorios.", null)),
                true,
                null,
                null
            );
        }

        alumnoService.registrarNota(
            dni, asignatura, ((Number) calificacion).doubleValue());

        return new CallToolResult(
            List.of(new TextContent(null, "Calificación registrada correctamente.", null)),
            false,
            null,
            null
        );
    }
}
