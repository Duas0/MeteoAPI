# MeteoAPI · MDAI · Entrega 1

## Estudiante

| Nombre | Usuario Git | Partes principales en las que ha trabajado |
| --- | --- | --- |
| Isaac Moreno Duaso | Pendiente de completar | Modelo meteorológico, servicio, API REST, vistas, configuración y pruebas. |

## Temática

**La meteorología en tu mano.** Consulta estaciones meteorológicas por nombre, municipio, provincia, coordenadas o punto kilométrico de carretera. La aplicación ofrece páginas HTML y una API JSON para otros programas, pensada para consultar temperatura, humedad, viento, precipitación y rayos.

La entidad principal es `EstacionMeteorologica`: identificador, nombre, municipio, provincia, coordenadas, fecha y seis medidas meteorológicas. Su getter `getVientoMs()` calcula los m/s a partir de los km/h sin almacenar un segundo dato. Los ocho ejemplos y las referencias de carreteras son ficticios. Los datos viven en memoria y se restablecen al reiniciar.

## Vídeo de presentación

**Pendiente de grabar y añadir el enlace público.** El estudiante debe narrarlo con su propia voz y mostrar la versión etiquetada. [Guion de grabación](guion_video.md), con texto literal y acciones para cuatro minutos.

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

Capturados el 25 de octubre de 2026 con JDK 21. Las fechas de error varían en cada petición.

```text
$ curl.exe -s -i http://localhost:8080/api/estaciones/1
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Content-Length: 302
Date: Sun, 25 Oct 2026 22:23:50 GMT

{"id":1,"nombre":"Madrid Centro","municipio":"Madrid","provincia":"Madrid","latitud":40.4168,"longitud":-3.7038,"fechaObservacion":"2026-10-13T10:00:00Z","temperaturaC":21.5,"humedadPct":62,"vientoKmh":14.4,"direccionVientoGrados":270,"precipitacionUltimaHoraMm":0.4,"rayosUltimaHora":3,"vientoMs":4.0}
```

```text
$ curl.exe -s -i http://localhost:8080/api/estaciones?provincia=Badajoz
HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Content-Length: 610
Date: Sun, 25 Oct 2026 22:23:50 GMT

[{"id":3,"nombre":"Badajoz Ribera","municipio":"Badajoz","provincia":"Badajoz","latitud":38.8794,"longitud":-6.9707,"fechaObservacion":"2026-10-13T10:00:00Z","temperaturaC":25.2,"humedadPct":48,"vientoKmh":7.2,"direccionVientoGrados":180,"precipitacionUltimaHoraMm":0.0,"rayosUltimaHora":0,"vientoMs":2.0},{"id":4,"nombre":"Mérida Río","municipio":"Mérida","provincia":"Badajoz","latitud":38.9161,"longitud":-6.3437,"fechaObservacion":"2026-10-13T10:00:00Z","temperaturaC":24.0,"humedadPct":55,"vientoKmh":32.4,"direccionVientoGrados":225,"precipitacionUltimaHoraMm":1.8,"rayosUltimaHora":1,"vientoMs":9.0}]
```

```text
$ curl.exe -s -i http://localhost:8080/api/estaciones/9999
HTTP/1.1 404 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 25 Oct 2026 22:23:50 GMT

{"timestamp":"2026-10-25T22:23:50.743Z","status":404,"error":"Not Found","trace":"org.springframework.web.server.ResponseStatusException: 404 NOT_FOUND \"No existe una estación con id 9999\"\r\n\tat es.mdai.meteoapi.controller.EstacionRestController.lambda$obtener$0(EstacionRestController.java:35)\r\n\tat java.base/java.util.Optional.orElseThrow(Optional.java:403)\r\n\tat es.mdai.meteoapi.controller.EstacionRestController.obtener(EstacionRestController.java:34)\r\n\tat java.base/jdk.internal.reflect.DirectMethodHandleAccessor.invoke(DirectMethodHandleAccessor.java:103)\r\n\tat java.base/java.lang.reflect.Method.invoke(Method.java:580)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:252)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:184)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:934)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:853)\r\n\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:86)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866)\r\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000)\r\n\tat org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892)\r\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622)\r\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874)\r\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:132)\r\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:59)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165)\r\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77)\r\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:535)\r\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)\r\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:86)\r\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:71)\r\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:347)\r\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:406)\r\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:71)\r\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:1307)\r\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:2036)\r\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:74)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:949)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:483)\r\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:74)\r\n\tat java.base/java.lang.Thread.run(Thread.java:1583)\r\n","message":"No existe una estación con id 9999","path":"/api/estaciones/9999"}
```

```text
$ curl.exe -s -i http://localhost:8080/api/estaciones/abc
HTTP/1.1 400 
Content-Type: application/json
Transfer-Encoding: chunked
Date: Sun, 25 Oct 2026 22:23:50 GMT
Connection: close

{"timestamp":"2026-10-25T22:23:50.800Z","status":400,"error":"Bad Request","trace":"org.springframework.web.method.annotation.MethodArgumentTypeMismatchException: Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"abc\"\r\n\tat org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.convertIfNecessary(AbstractNamedValueMethodArgumentResolver.java:295)\r\n\tat org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.resolveArgument(AbstractNamedValueMethodArgumentResolver.java:134)\r\n\tat org.springframework.web.method.support.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:122)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.getMethodArgumentValues(InvocableHandlerMethod.java:224)\r\n\tat org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:174)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:117)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:934)\r\n\tat org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:853)\r\n\tat org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:86)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)\r\n\tat org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:866)\r\n\tat org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:1000)\r\n\tat org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:892)\r\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:622)\r\n\tat org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:874)\r\n\tat jakarta.servlet.http.HttpServlet.service(HttpServlet.java:710)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:132)\r\n\tat org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:59)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.springframework.web.filter.RequestContextFilter.doFilterInternal(RequestContextFilter.java:100)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.springframework.web.filter.FormContentFilter.doFilterInternal(FormContentFilter.java:93)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.springframework.web.filter.CharacterEncodingFilter.doFilterInternal(CharacterEncodingFilter.java:199)\r\n\tat org.springframework.web.filter.OncePerRequestFilter.doFilter(OncePerRequestFilter.java:116)\r\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:111)\r\n\tat org.apache.catalina.core.StandardWrapperValve.invoke(StandardWrapperValve.java:165)\r\n\tat org.apache.catalina.core.StandardContextValve.invoke(StandardContextValve.java:77)\r\n\tat org.apache.catalina.authenticator.AuthenticatorBase.invoke(AuthenticatorBase.java:535)\r\n\tat org.apache.catalina.core.StandardHostValve.invoke(StandardHostValve.java:115)\r\n\tat org.apache.catalina.valves.ErrorReportValve.invoke(ErrorReportValve.java:86)\r\n\tat org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:71)\r\n\tat org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:347)\r\n\tat org.apache.coyote.http11.Http11Processor.service(Http11Processor.java:406)\r\n\tat org.apache.coyote.AbstractProcessorLight.process(AbstractProcessorLight.java:71)\r\n\tat org.apache.coyote.AbstractProtocol$ConnectionHandler.process(AbstractProtocol.java:1307)\r\n\tat org.apache.tomcat.util.net.NioEndpoint$SocketProcessor.doRun(NioEndpoint.java:2036)\r\n\tat org.apache.tomcat.util.net.SocketProcessorBase.run(SocketProcessorBase.java:74)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:949)\r\n\tat org.apache.tomcat.util.threads.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:483)\r\n\tat org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:74)\r\n\tat java.base/java.lang.Thread.run(Thread.java:1583)\r\nCaused by: java.lang.NumberFormatException: For input string: \"abc\"\r\n\tat java.base/java.lang.NumberFormatException.forInputString(NumberFormatException.java:67)\r\n\tat java.base/java.lang.Long.parseLong(Long.java:708)\r\n\tat java.base/java.lang.Long.valueOf(Long.java:1156)\r\n\tat org.springframework.util.NumberUtils.parseNumber(NumberUtils.java:204)\r\n\tat org.springframework.beans.propertyeditors.CustomNumberEditor.setAsText(CustomNumberEditor.java:115)\r\n\tat org.springframework.beans.TypeConverterDelegate.doConvertTextValue(TypeConverterDelegate.java:438)\r\n\tat org.springframework.beans.TypeConverterDelegate.doConvertValue(TypeConverterDelegate.java:411)\r\n\tat org.springframework.beans.TypeConverterDelegate.convertIfNecessary(TypeConverterDelegate.java:158)\r\n\tat org.springframework.beans.TypeConverterSupport.convertIfNecessary(TypeConverterSupport.java:75)\r\n\tat org.springframework.beans.TypeConverterSupport.convertIfNecessary(TypeConverterSupport.java:51)\r\n\tat org.springframework.validation.DataBinder.convertIfNecessary(DataBinder.java:846)\r\n\tat org.springframework.web.method.annotation.AbstractNamedValueMethodArgumentResolver.convertIfNecessary(AbstractNamedValueMethodArgumentResolver.java:287)\r\n\t... 43 more\r\n","message":"Method parameter 'id': Failed to convert value of type 'java.lang.String' to required type 'java.lang.Long'; For input string: \"abc\"","path":"/api/estaciones/abc"}
```

Alta opcional comprobada en una instancia temporal en el puerto 8081:

```text
$ curl.exe -s -i -X POST http://localhost:8081/estaciones/nueva --data "nombre=Estacion+documentada&municipio=Madrid&provincia=Madrid&latitud=40.4&longitud=-3.7&vientoKmh=18"
HTTP/1.1 302 
Set-Cookie: JSESSIONID=6DC6924940C80146E587666D6ECED17B; Path=/; HttpOnly
Location: http://localhost:8081/estaciones
Content-Language: es-ES
Content-Length: 0
Date: Sun, 25 Oct 2026 22:23:50 GMT
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
- La portada usa listas de enlaces y el CSS mantiene bordes discretos, tipografía estándar y una rejilla adaptable, sin librerías visuales. Las plantillas reutilizan cabecera y pie y usan `th:each`, `th:text`, `th:if`, `th:classappend`, `#numbers`, `#lists` y enlaces con parámetros. El CSS propio adapta las páginas a móvil.
- En la entrega 2 se sustituirá la colección del servicio por persistencia con Spring Data JPA. En esta entrega no se usan JPA ni Lombok.

## Pruebas y entrega

Las pruebas cubren filtros combinados, valor calculado, consultas de rayos y viento, configuración alternativa, errores HTTP, coordenadas inválidas, puntos de carretera, renderizado de vistas y el alta con validación y redirección.

Verificación realizada el 25 de octubre de 2026 con **JDK 21**: `mvnw.cmd package` terminó con **BUILD SUCCESS**, nueve pruebas, cero fallos y cero errores. Se comprobó el arranque con el Wrapper, las vistas, respuestas HTTP reales 200/400/404 y el JAR con `--meteoapi.minimo-rayos=5` (dos estaciones en vez de cuatro). Se revisó visualmente la portada y el catálogo simplificados. Detalles y tareas finales en [COMPROBACION.md](COMPROBACION.md).

Para comprobarlo: `./mvnw package` (o `.\mvnw.cmd package` en Windows). La aplicación debe compilar para Java 21. El repositorio local incluye commits descriptivos y la etiqueta `entrega-1`; el ZIP de fuentes excluye `target/`, `.git/` y archivos del IDE.

Antes de entregar al profesor falta completar el usuario Git, publicar el repositorio con acceso para `oscarmg@unex.es` y añadir el enlace del vídeo narrado. Después de completar esos datos, actualiza el commit y la etiqueta de entrega y genera un ZIP que coincida con esa versión.
