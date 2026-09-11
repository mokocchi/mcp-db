# Arquitectura por capas de mcp-db

La aplicación está organizada en capas para separar la exposición del protocolo MCP, la lógica de negocio y la persistencia en PostgreSQL.

```text
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Capa de Entrada / Integración                        │
│                                                                              │
│  Cliente / Agente / LLM / MCP Client                                         │
│        │                                                                     │
│        ▼                                                                     │
│  App.java                                                                    │
│  - crea el servidor MCP                                                     │
│  - configura StdioServerTransportProvider                                   │
│  - crea MCPDatabaseServer y registra tools                                  │
│                                                                              │
│        │                                                                     │
│        ▼                                                                     │
│  MCPDatabaseServer.java                                                      │
│  - expone herramientas:                                                      │
│    * buscar_alumno_por_dni                                                   │
│    * obtener_notas_alumno                                                   │
│    * registrar_calificacion                                                  │
│  - valida argumentos y formatea respuesta JSON                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │ delega la lógica
                                     ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Capa de Aplicación / Servicio                         │
│                                                                              │
│  AlumnoService.java                                                          │
│  - orquesta los casos de uso                                                │
│  - transforma entidades/JSON                                                 │
│  - busca alumno por DNI                                                      │
│  - consulta notas y promedio                                                 │
│  - registra calificación                                                     │
│                                                                              │
│  Usa:                                                                        │
│    AlumnoRepository                                                          │
└──────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │ accede a datos
                                     ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Capa de Persistencia / Acceso a Datos                │
│                                                                              │
│  AlumnoRepository.java                                                       │
│  - ejecuta SQL en PostgreSQL                                                │
│  - consulta alumnos por DNI                                                  │
│  - consulta notas por DNI                                                    │
│  - inserta nuevas notas                                                      │
│                                                                              │
│  Usa:                                                                        │
│    DataSource                                                                │
│    PostgresDatasource.createDataSourceFromEnv()                              │
└──────────────────────────────────────────────────────────────────────────────┘
                                     │
                                     │ obtiene conexión JDBC
                                     ▼
┌──────────────────────────────────────────────────────────────────────────────┐
│                         Capa de Infraestructura / BD                         │
│                                                                              │
│  PostgresDatasource.java                                                     │
│  - crea la conexión con PostgreSQL                                           │
│  - lee variables de entorno                                                 │
│  - configura host, puerto, base de datos, usuario y password                │
│                                                                              │
│  PostgreSQL                                                                  │
│  tablas: alumnos, notas                                                      │
│  initdb.sql                                                                  │
└──────────────────────────────────────────────────────────────────────────────┘
```

## Flujo principal

1. El cliente llama a una tool MCP (`buscar_alumno_por_dni`, `obtener_notas_alumno`, `registrar_calificacion`).
2. `MCPDatabaseServer` recibe la petición y la valida.
3. `MCPDatabaseServer` delega en `AlumnoService`.
4. `AlumnoService` consulta o actualiza datos usando `AlumnoRepository`.
5. `AlumnoRepository` ejecuta SQL contra PostgreSQL mediante `DataSource`.
6. La respuesta se serializa a JSON y se devuelve al cliente MCP.

## Observaciones

- La capa de transporte y la lógica de negocio están separadas.
- El servicio actúa como punto de coordinación entre la API MCP y la persistencia.
- El repositorio encapsula todas las consultas SQL y evita que la lógica de negocio conozca detalles de la base de datos.
- La infraestructura de conexión queda centralizada en `PostgresDatasource`.
