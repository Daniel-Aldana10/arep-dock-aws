# Framework concurrente

## Descripcion

El HttpServer se ha optimizado para soportar peticiones concurrentes de manera eficiente y mantener la simplicidad del código.

## Video ejecución
[Video de la ejecucion](https://www.loom.com/share/242d418ee141443fab877865e873e490?sid=667d8ab0-0f03-408a-b8a4-263be2d8c5b3)

## Características Principales

- **Sistema de Anotaciones Personalizado**  
  Implementa anotaciones como `@RestController`, `@GetMapping` y `@RequestParam` para simplificar la creación de controladores.

- **Carga Automática de Componentes**  
  Descubrimiento automático de controladores mediante **reflexión**, evitando configuraciones manuales.
- **Concurrencia**
  En este taller se implemento para que fuera concurrente y se pudiera apagar de una buena manera.

### Cambios Realizados

1. **Thread Pool Simple**: 
   ```java
   executor = Executors.newFixedThreadPool(10);
   ```

2. **Manejo Concurrente**:
   ```java
   executor.submit(() -> {
       try {
           handleClient(clientSocket);
       } catch (Exception e) {
           // Handle error
       }
   });
   ```

3. **Método `handleClient`**: Procesa cada conexión en un hilo separado

### Beneficios

- **Concurrencia**: Múltiples requests simultáneos
- **Compatibilidad**: Mantiene toda la funcionalidad existente


### Pruebas

1. **Compilar y ejecutar**:
   ```bash
   mvn compile
   java -cp target/classes co.edu.escuelaing.microspringboot.HttpServer
   ```

2. **Pruebas automáticas**:
   ```bash
   # Tests de concurrencia
   mvn test -Dtest=HttpServerConcurrencyTest
   
   # Tests básicos del framework
   mvn test -Dtest=HttpServerTest
   
   # Tests de integración
   mvn test -Dtest=IntegrationControllerTest
   ```

### Características

- **10 hilos concurrentes** por defecto
- **Manejo de errores**
- **Código  mantenible**


### Shutdown 

El servidor incluye un **shutdown hook** silencioso que permite un apagado elegante:

- **Ctrl+C**: Apagado graceful con cierre del thread pool
- **System.exit()**: Manejo automático del shutdown
- **Timeout**: 30 segundos para cierre graceful, luego forzado
- **Silencioso**: Sin mensajes de logging, solo funcionalidad

## Endpoints Disponibles

Una vez que el servidor esté ejecutándose en `http://localhost:35000`, puedes acceder a:

### Servicios REST
- `GET /hello` - Saludo simple
- `GET /greeting` - Devuelve Hola World
- `GET /greeting?name=TuNombre` - Saludo personalizado
- `GET /user?name=nombre&age=18` - Saludo y te devuleve la edad ingresada
- `GET /userInfo` - Te devuelve la informacion de un usuario

### Archivos Estáticos
- `GET /` o `GET /index.html` - Página principal
- `GET /style.css` - Estilos CSS
- `GET /script.js` - JavaScript
- `GET /serveis-watch.png` - Imagen PNG
- `GET /time.jpg` - Imagen JPG

### Ejecución Local

```bash
# Clonar el repositorio
git clone https://github.com/Daniel-Aldana10/arep-dock-aws
cd arep-dock-aws

# Compilar el proyecto
mvn clean compile

# Ejecutar el servidor
java -cp target/classes co.edu.escuelaing.microspringboot.MicroSpringBoot

# Acceder a la aplicación
http://localhost:35000

```
### Descargar y ejecutar la imagen desde Docker Hub

```bash

# Descargar la imagen
docker pull daniel10a/taller-arep

# Ejecutar el contenedor 
docker run -p 35000:35000 daniel10a/taller-arep

# Acceder a la aplicación
http://localhost:35000

```
##  Tecnologías Utilizadas

- **Java 17** 
- **Maven** 
- **JUnit 5** 

## Autor

**Daniel Aldana** - [GitHub](https://github.com/Daniel-Aldana10)