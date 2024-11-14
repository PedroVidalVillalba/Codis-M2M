# Práctica 6. Agentes

Diseñar un sistema multiagente que permita la compra/venta de libros mediante el procedimiento de subasta al alza (english auction). En este caso se entiende que hay un vendedor que posee uno o más libros a la venta y múltiples compradores interesados en alguno de sus libros. Los compradores deben de implementar un valor máximo por el cual están dispuestos a pujar y el vendedor el paso o incremento entre dos pujas sucesivas.

Con el fin de poder apreciar mejor el funcionamiento, asúmase que entre dos pujas sucesivas debe transcurrir un tiempo de 10 segundos, por lo que el vendedor deberá esperar ese tiempo antes de asignar un nuevo precio al libro. La subasta concluirá cuando en una ronda todos los posibles compradores indican que no están interesados en el libro, asignándose el mismo al primer comprador que haya pujado por el mismo en la ronda anterior, o bien cuando en la roda actual exista un único comprador interesado.

La calificación de la práctica se realizará de forma incremental, según el siguiente esquema:

a) Un vendedor, múltiples compradores donde su número no cambia durante la subasta, una única subasta (5 puntos).

b) Un vendedor, múltiples compradores que entran y salen dinámicamente en la subasta, una única subasta (2 puntos).

c) Un vendedor, múltiples compradores (posiblemente interesados en varias subastas) que entran y salen dinámicamente en la subasta, múltiples subastas simultaneas (2 puntos).

d) Interfaz gráfica en el vendedor para iniciar la subasta de nuevos libros y el seguimiento de cada una de las subastas activas. Interfaz gráfica en el comprador ilustrando el estado de la subasta o subastas en las que esté interesado (1 punto).

Nota: El alumno deberá mostrar la práctica en funcionamiento con el agente sniffer, mostrando todos los mensajes intercambiados entre los distintos agentes del sistema multiagente.

# Práctica 7. Ontologías

Realizar la práctica 6 utilizando para el diálogo una ontología. El diseño de la ontología deberá realizarse con Protégé. Es necesario para poder realizar esta práctica el haber realizado previamente TODOS y cada uno de los apartados de la práctica 6.

### Anotaciones
- JADE no admite mensajes en broadcast, que sería lo propio para una subasta inglesa. Por tanto, es el vendedor el que tiene que ir aumentando los precios mientras haya alguien dispuesto a pagar. Los compradores son meramente reactivos: responden que sí si están dispuestos a pagar el precio establecido, y que no en otro caso.
- Los compradores se registran en el servicio de páginas amarillas (que hace de sala de subastas).
- Cada vez que se hace la propuesta hay que consultar el servicio de páginas amarillas, porque pueden entrar o salir compradores de forma dinámica.
- Es el vendedor el que va incrementando los precios, hasta que solo levante la mano una persona o no la levante nadie. En este último caso, hay que determinar entre los que estaban en la ronda anterior a quién se le vende el libro (al azar, del que primero se recibió la propuesta,...).
- Qué pasa si alguien gana una ronda y luego se va de la subasta? Vamos a asumir que los participantes que recibieron un accept-proposal no pueden salirse de la subasta (simplemente se programa así en el comprador, no hay ningún mecanismo a mayores que lo compruebe).
- Asumimos también que el vendedor es ético: si solo queda un comprador se le vende a él, no se intenta incrementar el precio hasta encontrar el límite que está dispuesto a pagar.
- Los agentes tienen que ser **autónomos**. Una vez se inician, no necesitan feedback del usuario. El comprador tiene que poder activarse sin que haya ningún libro que le interese en la sala de subastas. Si en algún momento se subasta un libro que le interese, intentará comprarlo.
- No va a mirar el código, la práctica se corrige según la funcionalidad: hay que familiarizarse con el sniffer.
