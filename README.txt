Proyecto Fin de Ciclo - 2º DAM Online
Autora: Laura Pérez Martín
Tutor: Alfredo Hurtado Martín

1. DESCRIPCIÓN DEL PROYECTO

Conecta Voluntariado es una aplicación móvil Android desarrollada como Proyecto Fin de Ciclo de Desarrollo de Aplicaciones Multiplataforma.

La aplicación tiene como objetivo facilitar la conexión entre entidades del tercer sector y personas interesadas en realizar actividades de voluntariado. Permite que las entidades publiquen proyectos de voluntariado y que los usuarios registrados como voluntarios puedan consultar, filtrar e inscribirse en aquellas actividades que se adapten a sus intereses, ubicación y necesidades.

La aplicación incluye funcionalidades de registro e inicio de sesión, diferenciación de roles, gestión de voluntariados, búsqueda mediante filtros, inscripción y cancelación de inscripciones, edición de perfiles y sistema de logros para fomentar la participación.

2. TECNOLOGÍAS UTILIZADAS

- Android Studio
- Kotlin
- XML para el diseño de interfaces
- Firebase Authentication
- Cloud Firestore
- Git / GitHub
- SourceTree

3. ESTRUCTURA DE LA ENTREGA

El ZIP de binarios/proyecto incluye:

- Código fuente del proyecto Android.
- Archivo APK generado para la instalación de la aplicación en un dispositivo Android.
- Este archivo README.txt con las instrucciones básicas de uso y ejecución.

4. REQUISITOS PARA EJECUTAR EL PROYECTO

Para abrir y ejecutar el proyecto es necesario disponer de:

- Android Studio instalado.
- JDK compatible con Android Studio.
- Conexión a Internet para el uso de Firebase.
- Un emulador Android o un dispositivo Android físico.
- Acceso al proyecto Firebase configurado.

5. INSTRUCCIONES PARA ABRIR EL PROYECTO

1. Descomprimir el archivo ZIP del proyecto.
2. Abrir Android Studio.
3. Seleccionar la opción "Open" o "Abrir proyecto".
4. Seleccionar la carpeta raíz del proyecto descomprimido.
5. Esperar a que Android Studio sincronice Gradle y descargue las dependencias necesarias.
6. Ejecutar la aplicación en un emulador Android o en un dispositivo físico conectado por USB.

6. INSTRUCCIONES PARA INSTALAR EL APK

1. Copiar el archivo APK en un dispositivo Android.
2. Permitir la instalación de aplicaciones desde fuentes desconocidas si el dispositivo lo solicita.
3. Abrir el archivo APK desde el dispositivo.
4. Instalar la aplicación.
5. Abrir Conecta Voluntariado desde el menú de aplicaciones.

7. FUNCIONALIDADES PRINCIPALES

- Registro e inicio de sesión de usuarios.
- Diferenciación entre usuarios voluntarios y entidades.
- Registro de entidades.
- Registro de voluntarios.
- Creación, consulta y eliminación de voluntariados por parte de las entidades.
- Búsqueda de voluntariados mediante filtros.
- Inscripción de voluntarios en voluntariados.
- Cancelación de inscripciones.
- Consulta y edición del perfil de voluntario.
- Consulta y edición del perfil de entidad.
- Cierre de sesión.
- Eliminación de cuenta.
- Sistema de logros para voluntarios.

8. BASE DE DATOS

La aplicación utiliza Cloud Firestore como base de datos en tiempo real.

Las principales colecciones utilizadas son:

- users
- entities
- volunteers
- volunteerings
- volunteering_types
- volunteer_registrations
- achievements

Firebase Authentication se utiliza para la gestión del registro, inicio de sesión e identificación de los usuarios.

9. OBSERVACIONES

La aplicación ha sido desarrollada con fines académicos como parte del Proyecto Fin de Ciclo de DAM.

Algunas funcionalidades quedan planteadas como posibles mejoras futuras, como la incorporación de notificaciones, mensajería interna entre entidades y voluntarios, ampliación del sistema de logros y expansión a dispositivos iOS.

10. DATOS DEL PROYECTO

Nombre del proyecto: Conecta Voluntariado
Tipo de aplicación: Aplicación móvil Android
Lenguaje principal: Kotlin
Base de datos: Firebase / Cloud Firestore
Autora: Laura Pérez Martín
Ciclo: Desarrollo de Aplicaciones Multiplataforma
Modalidad: Online