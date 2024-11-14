# Práctica 5

Se pide diseñar una aplicación distribuida que implemente un sistema de mensajería instantánea **mediante java RMI**. El servidor aceptará conexiones de múltiples clientes y, cada vez que se conecte uno nuevo, éste deberá realizar diversas acciones. En primer lugar deberá notificar al resto de clientes conectados que hay un nuevo cliente. Por otro lado, deberá informar al recién conectado de los clientes que en ese momento se encuentren en línea. De la misma forma, cuando un cliente se desconecte, se deberá de informar al resto de ese hecho.

Cada cliente deberá de proporcionar un mecanismo para el envío y recepción de mensajes que, bajo ningún concepto, deberá de pasar por el servidor. La comunicación deberá establecerse necesariamente de cliente a cliente.

Opcionalmente se plantea la posibilidad de gestionar grupos de amistad, de forma que cada usuario dispondrá de un grupo de amigos. Para ello sería necesario proporcionar un mecanismo de registro en el sistema (dar de alta al usuario, con nombre de usuario y clave de acceso). Posteriormente debería de implementarse un mecanismo que permita solicitar amistad a otro usuario en el caso de conocer el nombre de dicho usuario. La amistad no se considera completa mientras el usuario al que se le solicita amistad no confirme que está de acuerdo en aceptar dicha solicitud. La solicitud podría realizarse incluso aunque el otro usuario no se encuentre en línea, por lo que habría que almacenar las solicitudes pendientes en el servidor a la espera de que el usuario en cuestión se conecte a fin de preguntarle si está de acuerdo en aceptar la solicitud. Evidentemente, si se gestionan grupos de amistad, la notificación de que un usuario acaba de conectarse o desconectarse se realiza solo a los usuarios del sistema que pertenezcan a su grupo de amistad. Los grupos de amistad son diferentes para cada usuario y el concepto de amistad es biyectivo, esto es, si el usuario A es amigo del usuario B, necesariamente el usuario B es amigo del usuario A.

La práctica se valorará según el siguiente esquema:

a) Un servidor aceptando conexiones de múltiples clientes, donde todos son amigos entre sí. No hay clave de acceso de los clientes (5 puntos).

b) Un servidor aceptando conexiones de múltiples clientes, en donde hay grupos de amigos definidos de forma estática. La clave de acceso al sistema deberá de poder ser modificada por el cliente (2,5 puntos).

c) Un servidor aceptando conexiones de múltiples clientes, en donde hay grupos de amigos definidos de forma dinámica (para ello será necesaria una base de datos que contenga todos los usuarios, la lista de sus amigos y su clave de acceso). Para insertar nuevos usuarios en la base de datos será necesario proporcionar un mecanismo para registrarlo en el sistema que nos pida el nombre de usuario y clave de acceso. Asimismo, deberá de haber un mecanismo que permita añadir desde el cliente un nuevo amigo en caso de conocer su nombre de usuario (2,5 puntos).

Se sobreentiende que el cliente debe de tener una interfaz gráfica con una funcionalidad mínima.

Con el fin de ejercitar la competencia transversal TR2 descrita en la memoria del título de ingeniería informática no solo se permite sino que se recomienda encarecidamente la realización de esta práctica en grupos de dos personas

- TR2 - Personales: Trabajo en equipo. Trabajo en un equipo multidisciplinar y multilingüe. Habilidades en las relaciones interpersonales. Razonamiento crítico. Compromiso ético


