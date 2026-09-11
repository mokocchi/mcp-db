# MCP Base de Datos PostgreSQL

Servidor MCP educativo construido con Java que permite a un cliente de IA consultar y actualizar información académica almacenada en PostgreSQL.

El proyecto acompaña el enfoque práctico del video [MCP Base de Datos Postgres local DSSD 2026 HD](https://youtu.be/GM5X3hUd304): partir de una necesidad concreta, exponer operaciones útiles como herramientas MCP y observar cómo una petición en lenguaje natural atraviesa todas las capas de una aplicación real.

## ¿Qué se aprende?

Este repositorio está pensado como material didáctico para trabajar los siguientes conceptos:

- qué problema resuelve el protocolo **Model Context Protocol (MCP)**;
- cómo un cliente MCP descubre y utiliza herramientas;
- cómo diseñar herramientas con nombre, descripción y esquema de argumentos;
- cómo conectar un servidor MCP con una base de datos PostgreSQL;
- cómo separar transporte, lógica de negocio y acceso a datos;
- cómo validar entradas y devolver resultados estructurados en JSON;
- cómo diferenciar operaciones de consulta y de modificación.

La idea central es sencilla: el modelo no accede directamente a la base de datos. Solicita una herramienta con argumentos definidos, y el servidor decide qué operación segura y concreta se ejecuta.

## Escenario didáctico

La aplicación simula un pequeño sistema académico con alumnos, cursos y calificaciones. A través de un cliente MCP se pueden formular peticiones como:

> Busca al alumno con DNI `12345678`.
>
> ¿Qué notas tiene el alumno con DNI `87654321` y cuál es su promedio?
>
> Registra un `7.5` en `Bases de Datos` para el alumno `12345678`.

El cliente transforma la intención en una llamada a una tool. El servidor valida los datos, delega en el servicio correspondiente y devuelve una respuesta que el cliente puede presentar al usuario.

## Herramientas disponibles

| Tool | Tipo | Argumentos | Función |
| --- | --- | --- | --- |
| `buscar_alumno_por_dni` | Lectura | `dni` | Devuelve nombre, apellido y curso del alumno. |
| `obtener_notas_alumno` | Lectura | `dni` | Devuelve las notas y calcula el promedio. |
| `registrar_calificacion` | Escritura | `dni`, `asignatura`, `calificacion` | Guarda una nueva calificación. |

Las tools están declaradas en `MCPDatabaseServer.java`. Allí también se validan los argumentos y se convierten los resultados en respuestas MCP.

## Arquitectura

```text
Cliente MCP / agente / LLM
            |
            v
App.java + transporte stdio
            |
            v
MCPDatabaseServer.java
  herramientas y validación
            |
            v
AlumnoService.java
  casos de uso y reglas de aplicación
            |
            v
AlumnoRepository.java
  consultas SQL parametrizadas
            |
            v
PostgresDatasource.java
            |
            v
PostgreSQL: alumnos y notas
```

El recorrido de una petición es:

1. El cliente MCP solicita una tool.
2. `MCPDatabaseServer` recibe y valida sus argumentos.
3. `AlumnoService` coordina el caso de uso.
4. `AlumnoRepository` ejecuta la consulta SQL mediante JDBC.
5. PostgreSQL devuelve los datos.
6. El servidor serializa la respuesta y la entrega al cliente.

Esta separación permite estudiar cada responsabilidad de forma aislada y facilita reemplazar la base de datos o el cliente sin reescribir toda la aplicación.

## Requisitos

- Java 17 o superior.
- Docker y Docker Compose.
- Un cliente MCP compatible con transporte `stdio`.
- Acceso a internet la primera vez que Gradle descargue las dependencias.

## Puesta en marcha

### 1. Iniciar PostgreSQL

Desde la raíz del proyecto:

```bash
docker compose up -d postgres
```

El contenedor crea automáticamente la base `aula_db`, el usuario `postgres`, las tablas y los datos de ejemplo definidos en `initdb.sql`.

Para comprobar el estado:

```bash
docker compose ps
```

Para detener la base de datos:

```bash
docker compose down
```

### 2. Compilar el servidor

```bash
./gradlew build
```

En Windows:

```powershell
gradlew.bat build
```

También es posible compilar solo el código principal:

```bash
./gradlew compileJava
```

### 3. Ejecutar desde el IDE

Abre el proyecto como proyecto Gradle, espera a que se resuelvan las dependencias y ejecuta la clase:

```text
src/main/java/App.java
```

El servidor utiliza `stdio`, por lo que su entrada y salida estándar están reservadas para la comunicación MCP. Los mensajes de diagnóstico deben observarse desde el cliente MCP o desde la consola de ejecución del IDE.

## Configuración de la base de datos

La conexión se configura mediante variables de entorno. Si no se indica ninguna, se utilizan estos valores:

| Variable | Valor predeterminado |
| --- | --- |
| `DB_HOST` | `localhost` |
| `DB_PORT` | `5432` |
| `DB_NAME` | `aula_db` |
| `DB_USER` | `postgres` |
| `DB_PASS` | `postgres` |

Ejemplo para usar otra configuración:

```bash
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=aula_db
export DB_USER=postgres
export DB_PASS=postgres
```

También se puede aplicar el esquema manualmente con el script incluido:

```bash
./initdb.sh
```

El volumen `postgres_data` conserva los datos entre reinicios. Para empezar de cero y volver a ejecutar la inicialización:

```bash
docker compose down -v
docker compose up -d postgres
```

## Datos de ejemplo

La inicialización incluye:

| DNI | Alumno | Curso | Datos iniciales |
| --- | --- | --- | --- |
| `12345678` | Ana García | `DAW2` | Matemáticas: `8.5` |
| `87654321` | Luis Pérez | `DAM1` | Programación: `9.2` |

Estos datos permiten probar rápidamente las tres herramientas sin crear registros a mano.

## Para usarlo un poco

1. **Observar el problema:** pedir los datos de un alumno sin que el cliente conozca el SQL.
2. **Inspeccionar la tool:** revisar su nombre, descripción, argumentos obligatorios y `additionalProperties: false`.
3. **Seguir la petición:** colocar puntos de interrupción en `MCPDatabaseServer`, `AlumnoService` y `AlumnoRepository`.
4. **Comparar lectura y escritura:** consultar una nota y después registrar una nueva calificación.
5. **Probar validaciones:** omitir el DNI, utilizar un DNI inexistente o enviar una calificación no numérica.
6. **Reflexionar sobre seguridad:** explicar por qué el modelo recibe herramientas limitadas en vez de credenciales y acceso SQL directo.

### Extras para pensar

- ¿Qué información debe aparecer en la descripción de una tool para que el modelo la use correctamente?
- ¿Qué responsabilidad pertenece al servidor MCP y cuál pertenece al servicio?
- ¿Qué controles adicionales habría que agregar antes de usar esta aplicación en producción?
- ¿Qué ventajas ofrece devolver JSON frente a devolver una frase libre?

## Estructura del proyecto

```text
.
├── build.gradle
├── docker-compose.yml
├── initdb.sql
├── initdb.sh
├── arq.md
└── src/main/java
    ├── App.java
    ├── MCPDatabaseServer.java
    ├── AlumnoService.java
    ├── AlumnoRepository.java
    ├── Alumno.java
    ├── AlumnoDTO.java
    └── PostgresDatasource.java
```

`arq.md` contiene un esquema ampliado de la arquitectura y del flujo de ejecución.

## Decisiones y límites del ejemplo

Este proyecto prioriza la claridad didáctica. Antes de utilizar una solución similar en producción convendría añadir autenticación, autorización, auditoría, validación explícita del rango de calificaciones, manejo más preciso de errores y pruebas automatizadas.

La tool `registrar_calificacion` representa una operación de escritura: debe probarse con especial cuidado y no debería habilitarse para cualquier usuario sin controles adicionales.

## Dependencias principales

- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk): implementación del servidor MCP.
- [PostgreSQL JDBC Driver](https://jdbc.postgresql.org/): conexión desde Java a PostgreSQL.
- [Jackson](https://github.com/FasterXML/jackson): lectura y generación de JSON.

## Referencia audiovisual

Video de apoyo: [MCP Base de Datos Postgres local DSSD 2026 HD](https://youtu.be/GM5X3hUd304).

La propuesta de este repositorio es utilizar el video como punto de partida para una práctica guiada: observar la interacción, identificar las herramientas y reconstruir el camino completo desde el mensaje del usuario hasta la consulta en PostgreSQL.
