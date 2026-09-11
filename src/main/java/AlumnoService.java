import java.util.Optional;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AlumnoService {
    private AlumnoRepository alumnoRepository;
    
    public AlumnoService(AlumnoRepository alumnoRepository){
        this.alumnoRepository = alumnoRepository;
    }

    public AlumnoDTO prepareAlumnoDTO(Alumno alumno) {
        return new AlumnoDTO(alumno.getDni(), alumno.getNombre(), alumno.getApellido(), alumno.getCurso());
    }

    public AlumnoDTO buscarPorDni(String dni) {
        return this.prepareAlumnoDTO(this.buscarPorDniInternal(dni));
    }

    private Alumno buscarPorDniInternal(String dni) {
        Optional<String> alumnoOpt = this.alumnoRepository.findAlumnoByDni(dni);

        if(alumnoOpt.isEmpty()) {
            return null;
        }

        String alumnoString = alumnoOpt.get();

        try {
            return new ObjectMapper().readValue(alumnoString, Alumno.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("No se pudo extraer el alumno desde el JSON", e);
        }
    }

    public boolean registrarNota(String dni, String materia, double nota) {
        Alumno alumno = this.buscarPorDniInternal(dni);

        if(alumno == null) {
            return false;
        }

        try {
            this.alumnoRepository.saveNota(alumno.getId(), materia, nota);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String obtenerNotasYPromedio(String dni) {
        return this.alumnoRepository.findNotasByDni(dni).orElse(null);
    }
}