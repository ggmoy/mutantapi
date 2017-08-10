Tecnologías utilizadas
======================

Las tecnologías que se detallan a continuación han sido seleccionadas para el desarrollo de la RESTful API:

* [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/java-archive-downloads-javase7-521261.html)
* [Jersey 2.25.1](https://jersey.github.io/download.html)
* [Apache Maven 3.5.0](https://maven.apache.org/download.cgi)
* [Google Cloud SDK](https://cloud.google.com/sdk/)

Con respecto al almacenamiento persistente de los ADNs y la información estadística, se ha decidido utilizar Google Cloud Datastore.

Descripción de uso
==================

La RESTful API se encuentra hosteada en Google App Engine.  La URL de la misma es http://vibrant-vector-138423.appspot.com.  Para verificar si un humano es mutante se deberá enviar la secuencia de ADN, en formato JSON, mediante un requerimiento HTTP POST al servicio [/mutant](http://vibrant-vector-138423.appspot.com/mutant).  A continuación vemos un ejemplo utilizando el comando curl:

    curl -H "Content-Type: application/json" -X POST -d '{"dna":["ATGCGA","CAGTGC","TTATGT","AGAAGG","CCCCTA","TCACTG"]}' http://vibrant-vector-138423.appspot.com/mutant

En caso de verificar que el ADN es de un humano mutante, el requerimiento HTTP terminará con estado 200 (OK).  Caso contrario, el estado será 403 (Forbidden).

Además del servicio de verificación de mutantes, la API también ofrece un servicio de estadísticas.  Este servicio nos brindará información acerca de la cantidad de mutantes y humanos verificados.  Para consultar dichas estadísticas se deberá enviar un requerimiento HTTP GET al servicio [/stats](http://vibrant-vector-138423.appspot.com/stats).  A continuación vemos un ejemplo utilizando el comando curl:

    curl http://vibrant-vector-138423.appspot.com/stats

## Maven
### Ejecución local

    mvn appengine:devserver

### Despliegue en Google Cloud

    mvn appengine:update
