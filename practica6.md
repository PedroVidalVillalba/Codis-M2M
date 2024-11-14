# Pr�ctica 6. Agentes

Dise�ar un sistema multiagente que permita la compra/venta de libros mediante el procedimiento de subasta al alza (english auction). En este caso se entiende que hay un vendedor que posee uno o m�s libros a la venta y m�ltiples compradores interesados en alguno de sus libros. Los compradores deben de implementar un valor m�ximo por el cual est�n dispuestos a pujar y el vendedor el paso o incremento entre dos pujas sucesivas.

Con el fin de poder apreciar mejor el funcionamiento, as�mase que entre dos pujas sucesivas debe transcurrir un tiempo de 10 segundos, por lo que el vendedor deber� esperar ese tiempo antes de asignar un nuevo precio al libro. La subasta concluir� cuando en una ronda todos los posibles compradores indican que no est�n interesados en el libro, asign�ndose el mismo al primer comprador que haya pujado por el mismo en la ronda anterior, o bien cuando en la roda actual exista un �nico comprador interesado.

La calificaci�n de la pr�ctica se realizar� de forma incremental, seg�n el siguiente esquema:

a) Un vendedor, m�ltiples compradores donde su n�mero no cambia durante la subasta, una �nica subasta (5 puntos).

b) Un vendedor, m�ltiples compradores que entran y salen din�micamente en la subasta, una �nica subasta (2 puntos).

c) Un vendedor, m�ltiples compradores (posiblemente interesados en varias subastas) que entran y salen din�micamente en la subasta, m�ltiples subastas simultaneas (2 puntos).

d) Interfaz gr�fica en el vendedor para iniciar la subasta de nuevos libros y el seguimiento de cada una de las subastas activas. Interfaz gr�fica en el comprador ilustrando el estado de la subasta o subastas en las que est� interesado (1 punto).

Nota: El alumno deber� mostrar la pr�ctica en funcionamiento con el agente sniffer, mostrando todos los mensajes intercambiados entre los distintos agentes del sistema multiagente.

# Pr�ctica 7. Ontolog�as

Realizar la pr�ctica 6 utilizando para el di�logo una ontolog�a. El dise�o de la ontolog�a deber� realizarse con Prot�g�. Es necesario para poder realizar esta pr�ctica el haber realizado previamente TODOS y cada uno de los apartados de la pr�ctica 6.

### Anotaciones
- JADE no admite mensajes en broadcast, que ser�a lo propio para una subasta inglesa. Por tanto, es el vendedor el que tiene que ir aumentando los precios mientras haya alguien dispuesto a pagar. Los compradores son meramente reactivos: responden que s� si est�n dispuestos a pagar el precio establecido, y que no en otro caso.
- Los compradores se registran en el servicio de p�ginas amarillas (que hace de sala de subastas).
- Cada vez que se hace la propuesta hay que consultar el servicio de p�ginas amarillas, porque pueden entrar o salir compradores de forma din�mica.
- Es el vendedor el que va incrementando los precios, hasta que solo levante la mano una persona o no la levante nadie. En este �ltimo caso, hay que determinar entre los que estaban en la ronda anterior a qui�n se le vende el libro (al azar, del que primero se recibi� la propuesta,...).
- Qu� pasa si alguien gana una ronda y luego se va de la subasta? Vamos a asumir que los participantes que recibieron un accept-proposal no pueden salirse de la subasta (simplemente se programa as� en el comprador, no hay ning�n mecanismo a mayores que lo compruebe).
- Asumimos tambi�n que el vendedor es �tico: si solo queda un comprador se le vende a �l, no se intenta incrementar el precio hasta encontrar el l�mite que est� dispuesto a pagar.
- Los agentes tienen que ser **aut�nomos**. Una vez se inician, no necesitan feedback del usuario. El comprador tiene que poder activarse sin que haya ning�n libro que le interese en la sala de subastas. Si en alg�n momento se subasta un libro que le interese, intentar� comprarlo.
- No va a mirar el c�digo, la pr�ctica se corrige seg�n la funcionalidad: hay que familiarizarse con el sniffer.
