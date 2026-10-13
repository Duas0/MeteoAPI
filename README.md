# MeteoAPI · MDAI · Entrega 1

## Estudiante

| Nombre | Usuario Git | Partes principales en las que ha trabajado |
| --- | --- | --- |
| Isaac Moreno Duaso | Pendiente de completar | Modelo meteorológico, servicio, API REST, vistas, configuración y pruebas. |

## Temática

**La meteorología en tu mano.** Consulta estaciones meteorológicas por nombre, municipio, provincia, coordenadas o punto kilométrico de carretera. La aplicación ofrece páginas HTML y una API JSON para otros programas, pensada para consultar temperatura, humedad, viento, precipitación y rayos.

La entidad principal es `EstacionMeteorologica`: identificador, nombre, municipio, provincia, coordenadas, fecha y seis medidas meteorológicas. Su getter `getVientoMs()` calcula los m/s a partir de los km/h sin almacenar un segundo dato. Los ocho ejemplos y las referencias de carreteras son ficticios. Los datos viven en memoria y se restablecen al reiniciar.

## Vídeo de presentación

**Pendiente de grabar y añadir el enlace público.** El estudiante debe narrarlo con su propia voz y mostrar la versión etiquetada. [Guion de grabación](guion_video.md), de unos cinco minutos y medio.

## Cómo arrancar

Requisitos: JDK 21, o uno posterior compatible. Maven se obtiene mediante el Wrapper incluido; la primera ejecución necesita conexión para descargar las dependencias. No hay que instalar una base de datos ni configurar claves externas.

En Windows, abre la carpeta `MeteoAPI` en VS Code y ejecuta:

```powershell
.\mvnw.cmd spring-boot:run
```

También puedes abrir `iniciar.cmd`. En Linux/macOS:

```bash
chmod +x mvnw
./mvnw spring-boot:run
```

Página de inicio: [http://localhost:8080](http://localhost:8080). Para detener la aplicación lanzada en la terminal, pulsa Ctrl+C.

Compilación, pruebas y empaquetado:

```powershell
.\mvnw.cmd package
java -jar target/meteoapi-0.0.1-SNAPSHOT.jar
```

Si el puerto está ocupado, arranca con `java -jar target/meteoapi-0.0.1-SNAPSHOT.jar --server.port=8081`.

## API REST

| Método | Ruta | Parámetros | Respuesta |
| --- | --- | --- | --- |
| GET | `/api/estaciones` | `nombre`, `provincia`, `municipio`, opcionales y combinables | `200`, lista; sin filtros devuelve las ocho estaciones iniciales. |
| GET | `/api/estaciones/{id}` | Identificador numérico | `200`, estación; `404` si no existe; `400` si el id no es numérico. |
| GET | `/api/estaciones/con-rayos` | Ninguno | `200`, lista que cumple el mínimo de rayos configurado. |
| GET | `/api/estaciones/viento` | `minimo`, opcional; por defecto 30 km/h | `200`, lista con viento igual o superior al mínimo; `400` para valores inválidos. |
| GET | `/api/estaciones/cercana` | `latitud` y `longitud`, o `carretera` y `km` | `200`, objeto con `estacion` y `distanciaKm`; `400` para parámetros inválidos; `404` para puntos no catalogados. |

Las búsquedas por nombre admiten coincidencias parciales. Municipio y provincia usan coincidencia completa; todos los filtros ignoran mayúsculas y espacios exteriores. Las consultas de listas sin resultados responden con `200` y `[]`.

Fechas en UTC, temperatura en °C, humedad en %, viento en km/h y m/s, dirección en grados, precipitación de la última hora en mm y rayos durante esa hora en un radio de 20 km. Una medida ausente es `null`; cero es una medida de cero. Las medidas pertenecen a la estación encontrada y la distancia es geográfica, no de conducción.

| Carretera | Kilómetro ficticio disponible |
| --- | --- |
| A-6 | 10.5 |
| A-5 | 340 |
| A-66 | 550 |
| A-2 | 308 |

### Ejemplos (salidas reales)

```text
$ curl.exe -s http://localhost:8080/api/estaciones/1
{"id":1,"nombre":"Madrid Centro","municipio":"Madrid","provincia":"Madrid","latitud":40.4168,"longitud":-3.7038,"fechaObservacion":"2026-10-13T10:00:00Z","temperaturaC":21.5,"humedadPct":62,"vientoKmh":14.4,"direccionVientoGrados":270,"precipitacionUltimaHoraMm":0.4,"rayosUltimaHora":3,"vientoMs":4.0}
```

```text
$ curl.exe -s "http://localhost:8080/api/estaciones?provincia=Badajoz"
[{"id":3,"nombre":"Badajoz Ribera","municipio":"Badajoz","provincia":"Badajoz","latitud":38.8794,"longitud":-6.9707,"fechaObservacion":"2026-10-13T10:00:00Z","temperaturaC":25.2,"humedadPct":48,"vientoKmh":7.2,"direccionVientoGrados":180,"precipitacionUltimaHoraMm":0.0,"rayosUltimaHora":0,"vientoMs":2.0},{"id":4,"nombre":"Mérida Río","municipio":"Mérida","provincia":"Badajoz","latitud":38.9161,"longitud":-6.3437,"fechaObservacion":"2026-10-13T10:00:00Z","temperaturaC":24.0,"humedadPct":55,"vientoKmh":32.4,"direccionVientoGrados":225,"precipitacionUltimaHoraMm":1.8,"rayosUltimaHora":1,"vientoMs":9.0}]
```

```text
$ curl.exe -s "http://localhost:8080/api/estaciones/cercana?latitud=40.42&longitud=-3.70"
{"estacion":{"id":1,"nombre":"Madrid Centro","municipio":"Madrid","provincia":"Madrid","latitud":40.4168,"longitud":-3.7038,"fechaObservacion":"2026-10-13T10:00:00Z","temperaturaC":21.5,"humedadPct":62,"vientoKmh":14.4,"direccionVientoGrados":270,"precipitacionUltimaHoraMm":0.4,"rayosUltimaHora":3,"vientoMs":4.0},"distanciaKm":0.48}
```

```text
$ curl.exe -s -i http://localhost:8080/api/estaciones/9999
HTTP/1.1 404 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 13 Oct 2026 18:09:42 GMT

{"timestamp":"2026-10-13T18:09:42.505Z","status":404,"error":"Not Found","path":"/api/estaciones/9999"}
```

```text
$ curl.exe -s -i http://localhost:8080/api/estaciones/abc
HTTP/1.1 400 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Tue, 13 Oct 2026 18:09:42 GMT
Connection: close

{"timestamp":"2026-10-13T18:09:42.663Z","status":400,"error":"Bad Request","path":"/api/estaciones/abc"}
```

Alta opcional; se muestran las cabeceras de estado y redirección de la respuesta real:

```text
$ curl.exe -s -i -X POST http://localhost:8080/estaciones/nueva --data-urlencode "nombre=Estación documentada" --data-urlencode "municipio=Madrid" --data-urlencode "provincia=Madrid" --data-urlencode "latitud=40.4" --data-urlencode "longitud=-3.7" --data-urlencode "vientoKmh=18" | Select-String "^(HTTP/|Location:)"
HTTP/1.1 302 
Location: http://localhost:8080/estaciones
```

## Vistas

| Ruta | Qué muestra |
| --- | --- |
| `/` | Inicio estático con logo y enlaces a las funcionalidades. |
| `/estaciones` | Catálogo con nombre, municipio, barra de provincias y filtro activo. |
| `/estaciones/{id}` | Ubicación y observación completa; `404` para una estación inexistente. |
| `/ubicacion` | Formularios por coordenadas y carretera, catálogo de puntos y estación encontrada. |
| `/estaciones/nueva` | Formulario de alta; los campos meteorológicos son opcionales. |

El POST a `/estaciones/nueva` valida en el servidor, asigna el identificador y la fecha, y responde con `302` y `Location: /estaciones`. La redirección evita repetir el alta al recargar; la confirmación se muestra mediante un mensaje flash. Los identificadores, fechas y valores calculados enviados por el cliente se ignoran. Los datos añadidos desaparecen al reiniciar.

## Configuración

| Propiedad | Valor por defecto | Efecto |
| --- | --- | --- |
| `meteoapi.nombre` | `MeteoAPI` | Nombre de las vistas dinámicas. |
| `meteoapi.descripcion` | `La meteorología en tu mano.` | Subtítulo del catálogo. |
| `meteoapi.minimo-rayos` | `1` | Mínimo positivo de rayos para la consulta y el resaltado de tarjetas. |

Las tres propiedades se inyectan mediante `@Value` en el constructor de `EstacionService`; todas tienen un valor alternativo con `${clave:defecto}`. Los textos no ASCII de los `.properties` usan escapes Unicode. La página de inicio es estática y muestra el nombre y eslogan iniciales; los cambios de texto afectan a las vistas Thymeleaf.

Demostración:

```powershell
java -jar target/meteoapi-0.0.1-SNAPSHOT.jar --meteoapi.minimo-rayos=5
```

Con la configuración inicial aparecen cuatro estaciones en `/api/estaciones/con-rayos`: Madrid, Cáceres, Mérida y Valencia. Con el mínimo de 5 aparecen dos: Cáceres y Valencia. El catálogo resalta exactamente el mismo conjunto. Un mínimo menor que 1 impide el arranque con un mensaje de configuración inválida.

## Decisiones de diseño

- Una estación contiene su última observación para mantener un modelo sencillo con texto, números y una fecha. En la siguiente entrega se podrán separar `EstacionMeteorologica` y `Observacion` para conservar un historial.
- `EstacionService` guarda ocho estaciones en `CopyOnWriteArrayList` y asigna ids con `AtomicLong`. Devuelve `Optional` al buscar por id, y listas obtenidas por filtros. Toda la validación, las reglas de rayos y viento y el cálculo de distancias están en el servicio; el getter del modelo convierte las unidades.
- Los dos controladores tienen el mismo servicio, inyectado por constructor en un campo `final`. El REST devuelve objetos que Jackson serializa; el MVC aporta esos mismos datos a las plantillas. No hay inyección sobre atributos ni creación manual de servicios.
- Las consultas propias son rayos y viento: permiten demostrar un umbral configurable y un `@RequestParam` con `defaultValue`. Las búsquedas geográficas amplían esas consultas con un catálogo de carretera pequeño y explícito, sin usar una API externa.
- Las plantillas reutilizan cabecera y pie y usan `th:each`, `th:text`, `th:if`, `th:classappend`, `#numbers`, `#lists` y enlaces con parámetros. El CSS propio adapta las páginas a móvil.
- En la entrega 2 se sustituirá la colección del servicio por persistencia con Spring Data JPA. En esta entrega no se usan JPA ni Lombok.

## Pruebas y entrega

Las pruebas cubren filtros combinados, valor calculado, consultas de rayos y viento, configuración alternativa, errores HTTP, coordenadas inválidas, puntos de carretera, renderizado de vistas y el alta con validación y redirección.

Verificación realizada el 13 de octubre de 2026: nueve pruebas, cero fallos, empaquetado correcto y comprobación del JAR mediante peticiones HTTP reales. Se utilizó JDK 22 con compilación `release 21`; no se ha ejecutado en un JDK 21 instalado en este equipo. Se comprobó también el inicio y el catálogo a una anchura de móvil de 375 px, sin desbordamiento horizontal.

Para comprobarlo: `./mvnw package` (o `.\mvnw.cmd package` en Windows). La aplicación debe compilar para Java 21. El repositorio local incluye commits descriptivos y la etiqueta `entrega-1`; el ZIP de fuentes excluye `target/`, `.git/` y archivos del IDE.

Antes de entregar al profesor falta completar el usuario Git, publicar el repositorio con acceso para `oscarmg@unex.es` y añadir el enlace del vídeo narrado. Después de completar esos datos, actualiza el commit y la etiqueta de entrega y genera un ZIP que coincida con esa versión.
