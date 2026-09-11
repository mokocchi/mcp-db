public class AlumnoDTO {
    private int dni;
    private String nombre;
    private String apellido;
    private String curso;

    public AlumnoDTO() {

    }
    
    public AlumnoDTO(int dni, String nombre, String apellido, String curso) {
        this.dni = dni;
        this.nombre = nombre;
        this.apellido = apellido;
        this.curso = curso;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getCurso() {
        return curso;
    }

    public int getDni() {
        return dni;
    }
}
