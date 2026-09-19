# Gestión de Empleados

Sistema de escritorio para el registro y administración de empleados de una empresa, desarrollado como proyecto individual del curso de **Programación 2** (UMG, Campus Jutiapa). Implementa un CRUD completo sobre una interfaz gráfica Swing, con persistencia real en MariaDB y una arquitectura Maven multi-módulo que separa la lógica de datos de la interfaz.

## Dominio del proyecto (Variante A)

Cada empleado registrado en el sistema tiene:

- **Identificador único**, asignado automáticamente por la base de datos (autoincremental).
- **Nombre completo**.
- **Departamento** al que pertenece (texto libre: la empresa no maneja una lista cerrada de departamentos).
- **Salario mensual**, siempre positivo, con soporte para centavos.
- **Fecha de contratación**.
- **Estado activo/inactivo**: un empleado que se retira no se elimina del historial, solo se marca como inactivo. Esto es independiente de la eliminación física, que es una acción aparte con confirmación.

## Arquitectura

El proyecto está organizado como un **Maven multi-módulo** de dos capas, para forzar la separación entre la lógica de acceso a datos y la interfaz gráfica:

```
gestion-empleados/                    (pom padre — packaging: pom)
├── gestion-empleados-core/           (módulo librería — packaging: jar)
│   └── edu.umg.programacion2.proyecto/
│       ├── modelo/Empleado.java
│       └── dao/
│           ├── ConexionBD.java
│           └── EmpleadoDAO.java
└── gestion-empleados-ui/             (módulo aplicación — packaging: jar)
    ├── sql/schema.sql
    └── edu.umg.programacion2.proyecto/
        ├── MainUI.java
        └── ui/VentanaPrincipal.java
```

**`gestion-empleados-core`** es una librería pura: contiene el modelo de dominio (`Empleado`) y el DAO (`EmpleadoDAO`) con las cuatro operaciones del CRUD sobre JDBC. No conoce Swing ni ninguna dependencia de interfaz gráfica.

**`gestion-empleados-ui`** consume `core` como una dependencia declarada en su `pom.xml` — no copia ni duplica sus clases. Contiene únicamente la ventana Swing y el punto de entrada de la aplicación. La UI nunca importa `java.sql.*` directamente: toda la interacción con la base de datos pasa por el DAO del módulo `core`.

`ConexionBD` tiene visibilidad de paquete (sin `public`), por lo que solo `EmpleadoDAO` puede usarla — la UI no tiene forma de abrir una conexión por su cuenta.

## Funcionalidad

La ventana principal (`VentanaPrincipal`) ofrece el CRUD completo:

- **Listar**: tabla (`JTable`) con todos los empleados, activos e inactivos.
- **Registrar**: formulario para dar de alta un nuevo empleado.
- **Actualizar**: selecciona una fila para cargar sus datos en el formulario y editarlos.
- **Eliminar**: borrado físico de la fila, con confirmación previa (`JOptionPane`) antes de aplicarse.

### Validaciones antes de tocar la base de datos

- Nombre y departamento no pueden quedar vacíos, y respetan la longitud máxima definida en el esquema.
- El salario debe ser un número mayor a cero, con máximo dos decimales.
- La fecha de contratación debe tener formato `AAAA-MM-DD` y no puede ser una fecha futura.

### Manejo de errores

Ninguna `SQLException` interrumpe la aplicación: se registra en el log interno y se muestra al usuario un mensaje claro en un `JOptionPane`, sin exponer el stacktrace completo.

## Esquema de base de datos

El script `gestion-empleados-ui/sql/schema.sql` crea la base de datos `empresa_empleados` y la tabla `empleados`:

| Columna | Tipo | Notas |
|---|---|---|
| `id` | `INT` | PK, `AUTO_INCREMENT` |
| `nombre` | `VARCHAR(100)` | `NOT NULL` |
| `departamento` | `VARCHAR(50)` | `NOT NULL`, texto libre |
| `salario` | `DECIMAL(10,2)` | `NOT NULL`, `CHECK (salario > 0)` |
| `fecha_contratacion` | `DATE` | `NOT NULL` |
| `activo` | `BOOLEAN` | `NOT NULL DEFAULT TRUE` |

`DECIMAL` se usa para el salario en vez de `FLOAT`/`DOUBLE` para evitar errores de redondeo con dinero. El juego de caracteres es `utf8mb4` para soportar tildes y `ñ` sin problemas.

El script incluye además tres registros de prueba para poder ejecutar la aplicación de inmediato.

## Requisitos

- JDK 11+
- Maven
- MariaDB (o MySQL) corriendo localmente, con permisos para crear una base de datos nueva

## Cómo ejecutarlo

1. **Crear la base de datos.** Ejecuta el script completo en tu cliente de MariaDB/MySQL (por ejemplo, MySQL Workbench o el CLI):

   ```bash
   mysql -u root -p < gestion-empleados-ui/sql/schema.sql
   ```

2. **Configurar la conexión.** Si tu usuario o contraseña de MariaDB son distintos a `root` sin contraseña, ajústalos en:

   ```
   gestion-empleados-core/src/main/java/edu/umg/programacion2/proyecto/dao/ConexionBD.java
   ```

3. **Compilar el proyecto** desde la raíz (esto instala `core` en el repositorio local de Maven para que `ui` pueda consumirlo):

   ```bash
   mvn clean install
   ```

4. **Ejecutar la aplicación:**

   ```bash
   mvn -pl gestion-empleados-ui exec:java
   ```

## Stack técnico

- Java 11
- Maven (multi-módulo)
- Swing (interfaz gráfica)
- JDBC + MariaDB (`mariadb-java-client`)
- Patrón DAO
