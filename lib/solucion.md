# Solución: Taller de Refactorización SOLID

## Índice

1. [Diagnóstico del código original](#1-diagnóstico-del-código-original)
2. [Paso 1 — SRP: Separar responsabilidades de OrderProcessor](#2-paso-1--srp-separar-responsabilidades-de-orderprocessor)
3. [Paso 2 — OCP: Polimorfismo para descuentos](#3-paso-2--ocp-polimorfismo-para-descuentos)
4. [Paso 3 — LSP: Corregir la jerarquía de Product](#4-paso-3--lsp-corregir-la-jerarquía-de-product)
5. [Paso 4 — ISP: Dividir la interfaz IWorker](#5-paso-4--isp-dividir-la-interfaz-iworker)
6. [Paso 5 — DIP: Inyección de dependencias por abstracción](#6-paso-5--dip-inyección-de-dependencias-por-abstracción)
7. [Paso 6 — Organización en paquetes por capas](#7-paso-6--organización-en-paquetes-por-capas)
8. [Paso 7 — Actualizar Main y validar](#8-paso-7--actualizar-main-y-validar)
9. [Resumen de la arquitectura final](#9-resumen-de-la-arquitectura-final)

---

## 1. Diagnóstico del código original

Antes de refactorizar es necesario identificar qué viola cada principio y dónde. A continuación se muestra el mapa de violaciones en el código de partida:

| Clase / Interfaz | Principio violado | Descripción del problema                                                                                               |
| ---------------- | ----------------- | ---------------------------------------------------------------------------------------------------------------------- |
| `OrderProcessor` | SRP               | Calcula precios, consulta descuentos, guarda en BD y envía emails en un solo método                                    |
| `OrderProcessor` | OCP               | Los descuentos se calculan con `if-else` por tipo de cliente; añadir uno nuevo obliga a modificar esta clase           |
| `OrderProcessor` | ISP               | Implementa `IWorker` y por tanto debe implementar `deliverProduct`, que no le corresponde                              |
| `OrderProcessor` | DIP               | Instancia `MySQLDatabase` y `EmailService` directamente en el constructor con `new`                                    |
| `DigitalProduct` | LSP               | Sobreescribe `getShippingWeight()` lanzando una excepción, rompiendo el contrato de su clase padre                     |
| `IWorker`        | ISP               | Declara `processOrder` y `deliverProduct` en la misma interfaz, forzando a quien solo procesa a implementar la entrega |
| `MySQLDatabase`  | DIP               | No implementa ninguna abstracción; `OrderProcessor` depende directamente de la clase concreta                          |
| `EmailService`   | DIP               | Igual que `MySQLDatabase`, es una clase concreta sin interfaz                                                          |

El síntoma concreto al ejecutar el programa es un crash en tiempo de ejecución cuando se procesa un `DigitalProduct`:

```
¡Error de sistema! Los productos digitales no tienen peso físico para envío.
```

---

## 2. Paso 1 — SRP: Separar responsabilidades de OrderProcessor

### El problema

El método `processOrder` hace tres cosas a la vez:

```java
// src/ean/solid/OrderProcessor.java (código original)
public void processOrder(Product product, String userType) {
    // 1. Calcula precio y descuento
    double finalPrice = price - discount;

    // 2. Guarda en la base de datos — responsabilidad de persistencia
    database.saveOrder("Producto: " + product.getName() + ", Total: $" + total);

    // 3. Envía notificación — responsabilidad de mensajería
    emailService.sendEmail("Su orden de " + product.getName() + " ha sido procesada.");
}
```

Cada vez que cambie la forma de persistir datos o de enviar notificaciones, se tendrá que modificar `OrderProcessor`, aunque la lógica de negocio no haya cambiado.

### La solución

Delegar cada responsabilidad a su propia clase. `OrderProcessor` no debe saber _cómo_ se guarda ni _cómo_ se notifica; solo debe orquestar el flujo:

```java
// OrderProcessor refactorizado — solo coordinación
public void processOrder(Product product, String userType) {
    double discount = resolveDiscount(userType, product.getPrice());
    double finalPrice = product.getPrice() - discount;
    double shippingCost = shippingCostPolicy.calculateShippingCost(product);
    double total = finalPrice + shippingCost;

    repository.saveOrder("Producto: " + product.getName() + ", Total: $" + total);
    notificationService.sendNotification("Su orden de " + product.getName() + " ha sido procesada.");
}
```

`MySQLDatabase` se encarga solo de persistir y `EmailService` solo de notificar. `OrderProcessor` únicamente llama sus contratos sin acoplarse a los detalles.

### ¿Por qué esto resuelve SRP?

Una clase cumple SRP cuando tiene **una sola razón para cambiar**. Tras la separación:

- `OrderProcessor` cambia solo si cambia la lógica de negocio de la orden.
- `MySQLDatabase` cambia solo si cambia la tecnología de persistencia.
- `EmailService` cambia solo si cambia el canal de notificación.

---

## 3. Paso 2 — OCP: Polimorfismo para descuentos

### El problema

La lógica de descuento en el código original es una cadena de `if-else`:

```java
// src/ean/solid/OrderProcessor.java (código original)
if (userType.equals("NORMAL")) {
    discount = 0;
} else if (userType.equals("VIP")) {
    discount = price * 0.10;
} else if (userType.equals("EMPLEADO")) {
    discount = price * 0.30;
}
```

Agregar el tipo `"PREMIUM"` implica abrir `OrderProcessor` y modificarlo, lo que es exactamente lo que el principio prohíbe.

### La solución

Crear una interfaz `DiscountPolicy` y una implementación por cada tipo de cliente. `OrderProcessor` recibe la lista de políticas y la itera sin importarle cuántas haya ni de qué tipo son:

```java
// Nueva interfaz
public interface DiscountPolicy {
    boolean supports(String userType);
    double calculateDiscount(double price);
}

// Una implementación por tipo
public class VipDiscountPolicy implements DiscountPolicy {
    @Override
    public boolean supports(String userType) {
        return "VIP".equalsIgnoreCase(userType);
    }

    @Override
    public double calculateDiscount(double price) {
        return price * 0.10;
    }
}

// OrderProcessor usa polimorfismo, sin if-else
private double resolveDiscount(String userType, double price) {
    for (DiscountPolicy policy : discountPolicies) {
        if (policy.supports(userType)) {
            return policy.calculateDiscount(price);
        }
    }
    return 0;
}
```

Para añadir el tipo `"PREMIUM"` en el futuro bastará con crear `PremiumDiscountPolicy` e incluirla en la lista que se inyecta en `OrderProcessor`. **No se toca ninguna clase existente**.

### ¿Por qué esto resuelve OCP?

El principio dice que las clases deben estar **abiertas a extensión y cerradas a modificación**. Con el patrón Strategy aplicado a los descuentos, extender el comportamiento consiste en agregar una nueva clase, no en editar las que ya funcionan.

Hay que crear las siguientes clases:

- `DiscountPolicy` (interfaz)
- `NormalDiscountPolicy`
- `VipDiscountPolicy`
- `EmployeeDiscountPolicy`

---

## 4. Paso 3 — LSP: Corregir la jerarquía de Product

### El problema

`Product` declara `getShippingWeight()` como método de instancia con valor por defecto. `DigitalProduct` la sobreescribe lanzando una excepción:

```java
// src/ean/solid/Product.java (código original)
public double getShippingWeight() {
    return 5.0; // peso por defecto
}

// src/ean/solid/DigitalProduct.java (código original)
@Override
public double getShippingWeight() {
    // ❌ Rompe el contrato: cualquier código que espere un Product
    //    y reciba un DigitalProduct fallará en tiempo de ejecución
    throw new UnsupportedOperationException("Los productos digitales no tienen peso físico.");
}
```

El **principio de Liskov** establece que si `S` es subtipo de `T`, cualquier instancia de `T` puede ser reemplazada por una de `S` sin alterar el comportamiento del programa. Aquí, usar un `DigitalProduct` donde se espera un `Product` rompe el flujo.

### La solución

Eliminar `getShippingWeight()` de `Product` base y modelar el envío como una capacidad opcional mediante la interfaz `Shippable`. Solo un `PhysicalProduct` la implementa:

```java
// Product es ahora solo nombre + precio, sin comprometer el envío
public class Product {
    private final String name;
    private final double price;
    // ... getters
}

// Capacidad opcional de envío físico
public interface Shippable {
    double getShippingWeight();
}

// Solo los físicos son Shippable
public class PhysicalProduct extends Product implements Shippable {
    private final double shippingWeight;

    @Override
    public double getShippingWeight() {
        return shippingWeight;
    }
}

// DigitalProduct no tiene método de envío: no puede romper nada
public class DigitalProduct extends Product {
    public DigitalProduct(String name, double price) {
        super(name, price);
    }
}
```

La política de envío verifica la interfaz antes de calcular el costo:

```java
public class StandardShippingCostPolicy implements ShippingCostPolicy {
    @Override
    public double calculateShippingCost(Product product) {
        if (product instanceof Shippable) {
            return ((Shippable) product).getShippingWeight() * 2.5;
        }
        return 0; // digital: sin costo de envío
    }
}
```

### ¿Por qué esto resuelve LSP?

`DigitalProduct` ya no sobreescribe ningún método heredado con una excepción. Ambos subtipos se pueden usar indistintamente donde se espera un `Product` sin riesgo. El envío se añade como capacidad separada (`Shippable`) en vez de imponérsela a toda la jerarquía.

---

## 5. Paso 4 — ISP: Dividir la interfaz IWorker

### El problema

La interfaz original fuerza a cualquier clase que la implemente a definir _tanto_ el procesamiento de órdenes como la entrega física:

```java
// src/ean/solid/IWorker.java (código original)
public interface IWorker {
    void processOrder(Product product, String userType);
    void deliverProduct(Product product);  // ← irrelevante para quien solo procesa
}
```

`OrderProcessor` implementa `IWorker` pero no puede entregar productos, así que lanza una excepción en ese método o lo deja vacío; ambas opciones son señal de diseño roto.

### La solución

Dividir en dos interfaces de un solo propósito:

```java
// Solo para quienes procesan órdenes
public interface OrderProcessingWorker {
    void processOrder(Product product, String userType);
}

// Solo para quienes entregan productos físicamente
public interface DeliveryWorker {
    void deliverProduct(Product product);
}
```

`OrderProcessor` implementa únicamente `OrderProcessingWorker`. Si existiera un repartidor, implementaría solo `DeliveryWorker`. Una clase que hace las dos cosas implementaría ambas interfaces sin problemas.

```java
// OrderProcessor ya no tiene métodos "de cartón"
public class OrderProcessor implements OrderProcessingWorker {
    @Override
    public void processOrder(Product product, String userType) { ... }
    // deliverProduct no existe aquí; no hay qué forzar ni simular
}

// Clase separada para la entrega
public class ProductDeliveryService implements DeliveryWorker {
    @Override
    public void deliverProduct(Product product) {
        System.out.println("Producto entregado: " + product.getName());
    }
}
```

### ¿Por qué esto resuelve ISP?

El principio dice que **ninguna clase debe depender de métodos que no usa**. Al tener interfaces granulares, cada implementador solo es responsable de los métodos que realmente tiene sentido que provea. Se elimina el método `deliverProduct` que `OrderProcessor` jamás debía implementar.

---

## 6. Paso 5 — DIP: Inyección de dependencias por abstracción

### El problema

`OrderProcessor` crea sus dependencias con `new` directamente en el constructor:

```java
// src/ean/solid/OrderProcessor.java (código original)
public OrderProcessor() {
    this.database = new MySQLDatabase(); // acoplamiento directo a la implementación
    this.emailService = new EmailService(); // idem
}
```

Esto implica que:

- `OrderProcessor` conoce la tecnología concreta (MySQL, SMTP).
- Es imposible probar `OrderProcessor` en aislamiento sin levantar MySQL o SMTP.
- Cambiar de base de datos o de canal de notificación obliga a modificar `OrderProcessor`.

Además, `MySQLDatabase` y `EmailService` no implementan ninguna interfaz, por lo que no existe un contrato que reemplazar.

### La solución

**Paso 5a — Definir interfaces (puertos de salida):**

```java
public interface OrderRepository {
    void saveOrder(String orderDetails);
}

public interface NotificationService {
    void sendNotification(String message);
}

public interface ShippingCostPolicy {
    double calculateShippingCost(Product product);
}
```

**Paso 5b — Hacer que las implementaciones concretas cumplan esos contratos:**

```java
public class MySQLDatabase implements OrderRepository {
    @Override
    public void saveOrder(String orderDetails) {
        System.out.println("Guardando en base de datos MySQL: " + orderDetails);
    }
}

public class EmailService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        System.out.println("Enviando email por SMTP: " + message);
    }
}
```

**Paso 5c — Inyectar las dependencias por constructor en `OrderProcessor`:**

```java
public class OrderProcessor implements OrderProcessingWorker {

    private final OrderRepository repository;
    private final NotificationService notificationService;
    private final ShippingCostPolicy shippingCostPolicy;
    private final List<DiscountPolicy> discountPolicies;

    public OrderProcessor(
        OrderRepository repository,
        NotificationService notificationService,
        ShippingCostPolicy shippingCostPolicy,
        List<DiscountPolicy> discountPolicies
    ) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.shippingCostPolicy = shippingCostPolicy;
        this.discountPolicies = discountPolicies;
    }
    // ...
}
```

La instanciación y el cableado de dependencias se traslada a `Main.java`, que es el único lugar donde está permitido conocer las implementaciones concretas.

### ¿Por qué esto resuelve DIP?

El principio establece que los módulos de alto nivel (`OrderProcessor`) no deben depender de módulos de bajo nivel (`MySQLDatabase`), sino que **ambos deben depender de abstracciones**. El resultado es:

- `OrderProcessor` solo conoce `OrderRepository`, `NotificationService`, etc.
- `MySQLDatabase` y `EmailService` solo conocen sus interfaces propias.
- Si mañana la BD cambia a PostgreSQL, se crea una nueva implementación de `OrderRepository` y se inyecta sin modificar `OrderProcessor`.

---

## 7. Paso 6 — Organización en paquetes por responsabilidad

Con todas las clases creadas al mismo nivel en `ean.solid` es difícil entender de un vistazo qué hace cada una. La solución es agruparlas por **tipo de responsabilidad**, lo cual es la manera más clásica y directa de organizar proyectos Java:

```
src/
└── ean/solid/
    ├── model/          ← Entidades y contratos del dominio
    │   ├── Product.java
    │   ├── PhysicalProduct.java
    │   ├── DigitalProduct.java
    │   └── Shippable.java
    ├── discount/       ← Reglas de descuento (interfaz + implementaciones)
    │   ├── DiscountPolicy.java
    │   ├── NormalDiscountPolicy.java
    │   ├── VipDiscountPolicy.java
    │   └── EmployeeDiscountPolicy.java
    ├── service/        ← Lógica de negocio, interfaces de servicio e implementaciones
    │   ├── OrderProcessingWorker.java
    │   ├── DeliveryWorker.java
    │   ├── OrderProcessor.java
    │   ├── ShippingCostService.java
    │   ├── StandardShippingCostService.java
    │   └── ProductDeliveryService.java
    ├── repository/     ← Acceso a datos (interfaz + implementación)
    │   ├── OrderRepository.java
    │   └── MySQLDatabase.java
    └── notification/   ← Notificaciones (interfaz + implementación)
        ├── NotificationService.java
        └── EmailService.java
```

### Justificación

| Paquete        | Contenido                                                          | Criterio de agrupación                                   |
| -------------- | ------------------------------------------------------------------ | -------------------------------------------------------- |
| `model`        | Clases e interfaces que representan datos del negocio              | "¿Es una entidad o contrato del dominio?"                |
| `discount`     | Interfaz y políticas de descuento                                  | "¿Contiene una regla de cálculo de descuento?"           |
| `service`      | Interfaces de trabajo y la lógica principal de la orden y el envío | "¿Contiene lógica de negocio o un contrato de servicio?" |
| `repository`   | Interfaz de persistencia y su implementación concreta              | "¿Guarda o recupera datos?"                              |
| `notification` | Interfaz de notificación y su implementación concreta              | "¿Envía mensajes al usuario?"                            |

Esta organización sigue el criterio simple de **"misma razón de ser, mismo paquete"**: cualquier desarrollador que abra el proyecto sabe exactamente en qué carpeta buscar cada tipo de clase sin necesitar conocimientos de arquitecturas avanzadas.

---

## 8. Paso 7 — Actualizar Main y validar

`Main.java` es la **raíz de composición** del sistema: el único lugar donde se instancian las implementaciones concretas y se inyectan como dependencias.

```java
// src/Main.java — después de la refactorización
import ean.solid.discount.DiscountPolicy;
import ean.solid.discount.EmployeeDiscountPolicy;
import ean.solid.discount.NormalDiscountPolicy;
import ean.solid.discount.VipDiscountPolicy;
import ean.solid.model.DigitalProduct;
import ean.solid.model.PhysicalProduct;
import ean.solid.model.Product;
import ean.solid.notification.EmailService;
import ean.solid.repository.MySQLDatabase;
import ean.solid.service.OrderProcessor;
import ean.solid.service.StandardShippingCostService;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<DiscountPolicy> discountPolicies = Arrays.asList(
            new NormalDiscountPolicy(),
            new VipDiscountPolicy(),
            new EmployeeDiscountPolicy()
        );

        OrderProcessor processor = new OrderProcessor(
            new MySQLDatabase(),               // implementación concreta de OrderRepository
            new EmailService(),                // implementación concreta de NotificationService
            new StandardShippingCostService(),
            discountPolicies
        );

        System.out.println("== Procesando Producto Físico ==");
        Product laptop = new PhysicalProduct("Laptop", 1000, 5.0);
        processor.processOrder(laptop, "VIP");

        System.out.println("\n== Procesando Producto Digital ==");
        Product software = new DigitalProduct("Antivirus", 50);
        processor.processOrder(software, "NORMAL"); // ya no lanza excepción
    }
}
```

La salida esperada al ejecutar:

```
== Procesando Producto Físico ==
Guardando en base de datos MySQL: Producto: Laptop, Total: $912.5
Enviando email por SMTP: Su orden de Laptop ha sido procesada.

== Procesando Producto Digital ==
Guardando en base de datos MySQL: Producto: Antivirus, Total: $50.0
Enviando email por SMTP: Su orden de Antivirus ha sido procesada.
```

El producto digital se procesa sin errores: envío = $0.0 porque `DigitalProduct` no implementa `Shippable`.

---

## 9. Resumen de la arquitectura final

### Clases y archivos nuevos creados

| Archivo                                    | Categoría      | Principio que resuelve |
| ------------------------------------------ | -------------- | ---------------------- |
| `model/Shippable.java`                     | Interfaz       | LSP                    |
| `model/PhysicalProduct.java`               | Clase concreta | LSP                    |
| `discount/DiscountPolicy.java`             | Interfaz       | OCP                    |
| `discount/NormalDiscountPolicy.java`       | Implementación | OCP                    |
| `discount/VipDiscountPolicy.java`          | Implementación | OCP                    |
| `discount/EmployeeDiscountPolicy.java`     | Implementación | OCP                    |
| `service/ShippingCostService.java`         | Interfaz       | OCP / LSP              |
| `service/StandardShippingCostService.java` | Implementación | OCP / LSP              |
| `repository/OrderRepository.java`          | Interfaz       | DIP                    |
| `notification/NotificationService.java`    | Interfaz       | DIP                    |
| `service/OrderProcessingWorker.java`       | Interfaz       | ISP                    |
| `service/DeliveryWorker.java`              | Interfaz       | ISP                    |
| `service/ProductDeliveryService.java`      | Implementación | ISP                    |

### Clases originales modificadas

| Archivo               | Cambio principal                                                                                                    | Principio que resuelve |
| --------------------- | ------------------------------------------------------------------------------------------------------------------- | ---------------------- |
| `Product.java`        | Se elimina `getShippingWeight()` del contrato base                                                                  | LSP                    |
| `DigitalProduct.java` | Se elimina el `@Override` que lanzaba excepción                                                                     | LSP                    |
| `MySQLDatabase.java`  | Implementa `OrderRepository`                                                                                        | DIP                    |
| `EmailService.java`   | Implementa `NotificationService`, renombra `sendEmail` → `sendNotification`                                         | DIP                    |
| `OrderProcessor.java` | Movido a `service/`; constructor con inyección, polimorfismo de descuentos, implementa solo `OrderProcessingWorker` | SRP, OCP, ISP, DIP     |
| `IWorker.java`        | Se elimina (reemplazada por `OrderProcessingWorker` y `DeliveryWorker`)                                             | ISP                    |
| `Main.java`           | Actúa como raíz de composición; inyecta todas las dependencias                                                      | DIP                    |

### Principios SOLID y cómo quedaron resueltos

| Principio | Violación original                                                           | Solución aplicada                                                               |
| --------- | ---------------------------------------------------------------------------- | ------------------------------------------------------------------------------- |
| **SRP**   | `OrderProcessor` gestionaba persistencia y notificaciones                    | Delegación a `OrderRepository` y `NotificationService`                          |
| **OCP**   | Descuentos con cadena `if-else`; agregar cliente = modificar código          | Patrón Strategy con `DiscountPolicy`; agregar cliente = nueva clase             |
| **LSP**   | `DigitalProduct.getShippingWeight()` lanzaba `UnsupportedOperationException` | Interfaz `Shippable` opcional; `DigitalProduct` no incumple ningún contrato     |
| **ISP**   | `IWorker` con dos métodos en interfaces que no todas las clases necesitan    | `OrderProcessingWorker` y `DeliveryWorker` como interfaces separadas            |
| **DIP**   | `OrderProcessor` instanciaba `MySQLDatabase` y `EmailService` con `new`      | Inyección por constructor; las dependencias son interfaces, no clases concretas |
