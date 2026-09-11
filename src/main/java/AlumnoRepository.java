import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AlumnoRepository {

    private final DataSource dataSource;

    public AlumnoRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Optional<String> findAlumnoByDni(String dni) {
        String sql = "SELECT id, nombre, apellido, curso FROM alumnos WHERE dni = ?";

        // try-with-resources cierra la conexión y la sentencia automáticamente
        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String json = String.format(
                            "{\"id\":%d, \"nombre\":\"%s\", \"apellido\":\"%s\", \"curso\":\"%s\"}",
                            rs.getLong("id"),
                            rs.getString("nombre"),
                            rs.getString("apellido"),
                            rs.getString("curso"));
                    return Optional.of(json);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error ejecutando SQL: " + e.getMessage());
        }

        return Optional.empty();
    }

    public void saveNota(Long alumnoId, String materia, double nota) {
        String sql = "INSERT INTO notas (alumno_id, materia, nota) VALUES (?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, alumnoId);
            stmt.setString(2, materia);
            stmt.setDouble(3, nota);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error ejecutando SQL: " + e.getMessage());
        }
    }

    public Optional<String> findNotasByDni(String dni) {
        String sql = "SELECT a.nombre, a.apellido, n.materia, n.nota FROM alumnos a JOIN notas n ON n.alumno_id = a.id WHERE a.dni = ? ORDER BY n.materia";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, dni);

            try (ResultSet rs = stmt.executeQuery()) {
                List<String> notas = new ArrayList<>();
                double suma = 0.0;
                int count = 0;
                String nombre = null;
                String apellido = null;

                while (rs.next()) {
                    nombre = rs.getString("nombre");
                    apellido = rs.getString("apellido");

                    String materia = rs.getString("materia");
                    double nota = rs.getDouble("nota");

                    notas.add(String.format(Locale.US,
                            "{\"materia\":\"%s\",\"nota\":%.2f}",
                            materia.replace("\\", "\\\\").replace("\"", "\\\""),
                            nota));
                    suma += nota;
                    count++;
                }

                if (count == 0 || nombre == null || apellido == null) {
                    return Optional.empty();
                }

                double promedio = suma / count;
                StringBuilder json = new StringBuilder();
                json.append("{\"dni\":\"").append(dni)
                    .append("\",\"nombre\":\"").append(nombre.replace("\\", "\\\\").replace("\"", "\\\""))
                    .append("\",\"apellido\":\"").append(apellido.replace("\\", "\\\\").replace("\"", "\\\""))
                    .append("\",\"notas\":[");

                for (int i = 0; i < notas.size(); i++) {
                    if (i > 0) {
                        json.append(',');
                    }
                    json.append(notas.get(i));
                }

                json.append("],\"promedio\":").append(String.format(Locale.US, "%.2f", promedio)).append('}');
                return Optional.of(json.toString());
            }
        } catch (SQLException e) {
            System.err.println("Error ejecutando SQL: " + e.getMessage());
        }

        return Optional.empty();
    }
}