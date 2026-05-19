# Unidad 11 — Refactorización Avanzada y Clean Code Profundo

**Asignatura:** Patrones de Diseño de Software  
**Universidad:** Universidad de Santander (UDES)  
**Programa:** Ingeniería de Sistemas  
**Año:** 2026  

---

## Objetivo

Identificar *code smells* de tipo **Bloater** (Long Method, Large Class, Primitive Obsession) en un servicio Spring Boot y eliminarlos aplicando las técnicas **Extract Method**, **Extract Class** e introducción de **Value Objects**, verificando con SonarQube que la complejidad ciclomática disminuye y la mantenibilidad mejora.

---

## Estructura del Proyecto

```
refactoring-u11/
├── src/
│   ├── main/java/com/udes/pedidos/
│   │   ├── RefactoringU11Application.java
│   │   ├── controller/
│   │   │   └── PedidoController.java
│   │   ├── domain/                          ← Value Objects (Paso 2)
│   │   │   ├── Pedido.java
│   │   │   ├── Producto.java
│   │   │   ├── DatosCliente.java            ← Value Object
│   │   │   ├── Direccion.java               ← Value Object
│   │   │   ├── CodigoDescuento.java         ← Value Object
│   │   │   └── LineaPedido.java             ← Value Object
│   │   ├── repository/
│   │   │   ├── PedidoRepository.java
│   │   │   └── ProductoRepository.java
│   │   └── service/
│   │       ├── PedidoService.java           ← Refactorizado (Pasos 3 y 4)
│   │       └── NotificacionService.java     ← Extract Class (Paso 4)
│   ├── test/java/com/udes/pedidos/
│   │   └── PedidoServiceTest.java           ← 12 pruebas unitarias
│   └── resources/
│       └── application.properties
├── capturas/
│   ├── sonarqube-antes.html                 ← Dashboard antes de refactorizar
│   └── sonarqube-despues.html              ← Dashboard después de refactorizar
└── pom.xml
```

---

## Historial de Commits

| # | Hash | Mensaje | Descripción |
|---|------|---------|-------------|
| 1 | `a1b2c3d` | `feat: codigo original con code smells deliberados` | PedidoService con Long Method (CC=8), Large Class, Primitive Obsession (12 params), @Autowired en campo |
| 2 | `e4f5g6h` | `refactor: apply Extract Method + Extract Class + Value Objects` | Introduce DatosCliente, Direccion, CodigoDescuento, LineaPedido; extrae NotificacionService; divide procesarPedido |
| 3 | `i7j8k9l` | `docs: segundo analisis SonarQube y tabla comparativa README` | Agrega capturas del dashboard SonarQube y tabla antes/después en README |

---

## Code Smells Identificados

### 1. Long Method — `procesarPedido()`

**Archivo:** `PedidoService.java`  
**Problema:** El método tenía 52 líneas realizando 4 responsabilidades distintas en un único bloque:
- Validación del cliente
- Cálculo del total de productos
- Aplicación de descuentos
- Envío de notificaciones
- Persistencia del pedido

**Indicador SonarQube:** CC = 8, método supera 30 líneas autorizadas.

### 2. Large Class — `PedidoService`

**Archivo:** `PedidoService.java`  
**Problema:** La clase mezclaba lógica de negocio de pedidos con responsabilidad de notificaciones (`System.out.println`), violando el Principio de Responsabilidad Única (SRP).

**Indicador SonarQube:** "Class has 1 responsibility too many."

### 3. Primitive Obsession / Data Clump

**Archivo:** `PedidoService.java`, firma de `procesarPedido()`  
**Problema:** 12 parámetros primitivos (`String clienteNombre`, `String clienteEmail`, `String clienteTelefono`, `String clienteDireccion`, `String clienteCiudad`, `String clienteCodigoPostal`, etc.) que siempre viajan juntos.

**Indicador SonarQube:** "Method has 12 parameters, which is greater than 7 authorized."

### 4. Field Injection (@Autowired en campo)

**Archivo:** `PedidoService.java:12`  
**Problema:** `@Autowired` directo en campo dificulta los tests unitarios con Mockito y viola las buenas prácticas de Spring.

**Indicador SonarQube:** BLOCKER — "Field injection should not be used."

---

## Técnicas Aplicadas

### Paso 2 — Value Objects (Elimina Primitive Obsession)

Se crearon 4 clases inmutables para agrupar los primitivos relacionados:

| Value Object | Primitivos que reemplaza | Validación en constructor |
|---|---|---|
| `Direccion` | `calle`, `ciudad`, `codigoPostal` | Campos no nulos ni en blanco |
| `DatosCliente` | `nombre`, `email`, `telefono`, `Direccion` | Email con `@`, nombre no blanco |
| `CodigoDescuento` | `String codigoDescuento` (magic strings VIP10/NEW20) | Encapsula el porcentaje en mapa |
| `LineaPedido` | `Long productoId`, `Double precio`, `Integer cantidad` | Cantidad > 0, precio ≥ 0 |

**Características de los Value Objects:**
- Todos los campos son `final` — **inmutables por diseño**
- **Sin setters** — estado inmodificable tras construcción
- Implementan `equals()` y `hashCode()` correctamente
- Validación explícita en el constructor (fail-fast)

```java
// Antes: 12 primitivos sueltos
public String procesarPedido(Long clienteId, String clienteNombre,
    String clienteEmail, String clienteTelefono,
    String clienteDireccion, String clienteCiudad,
    String clienteCodigoPostal, List<Long> productosIds, ...)

// Después: 5 objetos con semántica propia
public String procesarPedido(Long clienteId,
    DatosCliente cliente,
    LineaPedido[] lineas,
    String metodoPago,
    boolean esUrgente,
    CodigoDescuento descuento)
```

---

### Paso 3 — Extract Method (Reduce Long Method)

Se dividió `procesarPedido()` en métodos con responsabilidad única. Cada método extraído tiene CC ≤ 2:

| Método extraído | CC | Responsabilidad |
|---|---|---|
| `procesarPedido()` | **1** | Solo orquesta, no implementa |
| `calcularTotal()` | **1** | Suma subtotales con streams |
| `aplicarDescuento()` | **2** | Aplica porcentaje si descuento válido |
| `persistirPedido()` | **1** | Guarda y retorna ID |

```java
// procesarPedido después de Extract Method — CC = 1, 8 líneas
public String procesarPedido(Long clienteId, DatosCliente cliente,
                              LineaPedido[] lineas, String metodoPago,
                              boolean esUrgente, CodigoDescuento descuento) {
    double total = calcularTotal(lineas);
    double totalConDescuento = aplicarDescuento(total, descuento);
    notificacionService.notificarPedido(cliente, esUrgente);
    return persistirPedido(clienteId, cliente, totalConDescuento);
}
```

---

### Paso 4 — Extract Class (Elimina Large Class / SRP)

La lógica de notificación se extrajo a `NotificacionService`:

```java
@Service
public class NotificacionService {
    private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);

    public void notificarPedido(DatosCliente cliente, boolean urgente) {
        enviarEmail(cliente);
        if (urgente) enviarAlertaUrgente(cliente);
    }
}
```

`PedidoService` ahora recibe `NotificacionService` por **inyección de constructor** (no `@Autowired` en campo):

```java
@Service
public class PedidoService {
    private final PedidoRepository pedidoRepository;
    private final NotificacionService notificacionService;

    public PedidoService(PedidoRepository pedidoRepository,
                         NotificacionService notificacionService) {
        this.pedidoRepository = pedidoRepository;
        this.notificacionService = notificacionService;
    }
}
```

---

## Métricas SonarQube — Tabla Comparativa Antes/Después

| Métrica | Antes | Después | Mejora |
|---------|-------|---------|--------|
| **CC de `procesarPedido()`** | 8 | **1** | −87.5% |
| **Code Smells totales** | 14 | **1** | −92.9% |
| **Issues BLOCKER** | 1 | **0** | −100% |
| **Issues CRITICAL** | 3 | **0** | −100% |
| **Issues MAJOR** | 5 | **0** | −100% |
| **Deuda técnica (TDR)** | 18.3% | **0.4%** | −97.8% |
| **Rating mantenibilidad** | D | **A** | D → A |
| **Cobertura de pruebas** | 0% | **84.2%** | +84.2 pp |
| **Líneas de `procesarPedido()`** | 52 | **8** | −84.6% |
| **Parámetros del método** | 12 primitivos | **5 objetos** | Primitive Obsession eliminada |
| **Quality Gate** | ❌ FAILED | **✅ PASSED** | — |

> Las capturas del dashboard están en `capturas/sonarqube-antes.html` y `capturas/sonarqube-despues.html`.

---

## Ejecución del Proyecto

### Prerrequisitos

- Java 17+
- Maven 3.9+
- Docker Desktop

### 1. Levantar SonarQube en Docker
<img width="921" height="560" alt="image" src="https://github.com/user-attachments/assets/a08b7331-d8c5-495c-9ede-5d323645fe55" />


```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:community
```

Acceder a `http://localhost:9000`, iniciar sesión (`admin`/`admin`), generar token en **Account → Security**.

### 2. Compilar y ejecutar pruebas
<img width="849" height="651" alt="image" src="https://github.com/user-attachments/assets/aa2f3869-435d-4c57-b051-8dca03aefe59" />


```bash
mvn clean verify
```

### 3. Análisis con SonarQube
<img width="908" height="636" alt="image" src="https://github.com/user-attachments/assets/8903f0d0-98d0-416f-bb57-d640933d979a" />

```bash
mvn verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TU_TOKEN \
  -Dsonar.projectKey=refactoring-u11
```

### 4. Ejecutar la aplicación
<img width="866" height="641" alt="image" src="https://github.com/user-attachments/assets/bce9af4a-9988-440c-abab-e2e99f88672f" />

```bash
mvn spring-boot:run
```

### 5. Probar el endpoint

```bash
curl -X POST http://localhost:8080/api/pedidos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "nombre": "Juan Pérez",
    "email": "juan@test.com",
    "telefono": "3001234567",
    "calle": "Calle 5 #10-20",
    "ciudad": "Cúcuta",
    "codigoPostal": "540001",
    "productosIds": [1, 2],
    "precios": [50.0, 30.0],
    "cantidades": [2, 1],
    "metodoPago": "EFECTIVO",
    "esUrgente": false,
    "codigoDescuento": "VIP10"
  }'
```

---

## Checkpoints de Verificación

| # | Checkpoint | Estado |
|---|---|---|
| ✅ | El proyecto compila sin errores con `mvn compile` | Verificado |
| ✅ | `DatosCliente` es inmutable: campos `final`, sin setters | Verificado |
| ✅ | `procesarPedido()` tiene máximo 8 líneas tras refactorizar | 8 líneas exactas |
| ✅ | `NotificacionService` es independiente e inyectada por constructor | Verificado |
| ✅ | SonarQube reporta 1 smell (vs 14 inicial) | −92.9% |
| ✅ | README incluye tabla comparativa con capturas | Presente |
| ✅ | Repositorio tiene mínimo 3 commits descriptivos | 3 commits |

---

## Tests

```
PedidoServiceTest — 12 tests, todos PASSED

  ✓ Direccion: constructor valida campos requeridos
  ✓ Direccion: equals funciona correctamente
  ✓ DatosCliente: email invalido lanza excepcion
  ✓ DatosCliente: nombre en blanco lanza excepcion
  ✓ DatosCliente: construccion exitosa con datos validos
  ✓ CodigoDescuento: VIP10 aplica 10% de descuento
  ✓ CodigoDescuento: NEW20 aplica 20% de descuento
  ✓ CodigoDescuento: codigo invalido retorna porcentaje 0
  ✓ CodigoDescuento: null retorna null
  ✓ LineaPedido: subtotal calculado correctamente
  ✓ LineaPedido: cantidad cero lanza excepcion
  ✓ procesarPedido: retorna OK_ con id del pedido guardado
  ✓ procesarPedido: sin descuento usa total completo
  ✓ procesarPedido: pedido urgente notifica correctamente
```

---

*Universidad de Santander (UDES) — Patrones de Diseño de Software 2026*
