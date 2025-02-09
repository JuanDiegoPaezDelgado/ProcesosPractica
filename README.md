# Sistema de Votación para Elegir Representante de DAM

Este proyecto es un sistema de votación sencillo en el que los usuarios pueden votar por su representante de DAM y ver los resultados de la votación. El sistema está compuesto por una aplicación cliente y una aplicación servidor.

## Funcionalidades

### Cliente

- Permite a los usuarios votar por un candidato.
- Permite a los usuarios ver los resultados de la votación.
- Maneja errores de entrada y comunicación con el servidor.

### Servidor

- Gestiona las conexiones de los clientes.
- Recibe y procesa los votos de los clientes.
- Envía los resultados de la votación a los clientes.

## Estructura del Proyecto

El proyecto está organizado en los siguientes paquetes:

- `net.salesianos.client`: Contiene la aplicación cliente.
- `net.salesianos.server`: Contiene la aplicación servidor.
- `net.salesianos.server.threads`: Contiene el manejador de clientes (hilo) para la aplicación servidor.
- `net.salesianos.utils`: Contiene constantes y utilidades utilizadas en el proyecto.

## Clases Principales

### ClientApp (Cliente)

- `main(String[] args)`: Método principal que ejecuta la lógica del cliente.
- `handleVoting(DataInputStream inputStream, DataOutputStream outputStream, Scanner scanner)`: Maneja el proceso de votación.
- `handleResults(DataInputStream inputStream)`: Maneja el proceso de visualización de los resultados.

### ServerApp (Servidor)

- `main(String[] args)`: Método principal que inicia el servidor y maneja las conexiones entrantes.

### ClientHandler (Servidor)

- `run()`: Ejecuta el hilo y maneja las interacciones del cliente.
- `sendVotingOptions(DataOutputStream clientOutputStream)`: Envía las opciones de votación al cliente.
- `sendVotingResults(DataOutputStream clientOutputStream)`: Envía los resultados de la votación al cliente.
- `incrementVote(String option)`: Incrementa el conteo de votos para una opción específica de manera segura.

## Uso

### Requisitos Previos

- Java Development Kit (JDK) 8 o superior.
- Un entorno de desarrollo como IntelliJ IDEA o Eclipse (opcional).

### Ejecución del Servidor

1. Compilar las clases del servidor.
2. Ejecutar la clase `ServerApp` para iniciar el servidor.
3. El servidor estará escuchando conexiones en el puerto definido en `Constants.SERVER_PORT`.

### Ejecución del Cliente

1. Compilar las clases del cliente.
2. Ejecutar la clase `ClientApp` para iniciar el cliente.
3. Introducir el nombre del usuario cuando se solicite.
4. Seguir las instrucciones en pantalla para votar o ver los resultados.

