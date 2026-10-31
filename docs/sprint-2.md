# Sprint 2

Cada ruta utiliza el circuito Resilience4j de su servicio de destino. Si la dependencia falla, el gateway devuelve HTTP 503 y un identificador de correlación. El filtro devuelve X-Correlation-Id y registra método y estado de la respuesta.
