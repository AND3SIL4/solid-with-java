# Solución Alcanzada — Refactorización SOLID

## Resumen del proceso

Refactorización guiada paso a paso del proyecto original en `src/ean/solid/`, corrigiendo las 5 violaciones SOLID mediante mentoría interactiva.

---

## Pasos recorridos

### 1. SRP — Separar responsabilidades de `OrderProcessor`

**Problema:** `OrderProcessor.processOrder()` calculaba precios, guardaba en BD y enviaba emails.

**Solución:**
- Se eliminó la dependencia directa de `MySQLDatabase` y `EmailService` de `OrderProcessor`.
- `OrderProcessor` ahora solo calcula precio, descuento y envío, retornando el total (`double`).
- Se creó `OrderService` en `ean.solid.services` como clase orquestadora: llama a `OrderProcessor`, luego a `MySQLDatabase` para persistir y a `EmailService` para notificar.

**Archivos creados/modificados:** `OrderServices.java` → `OrderService.java`, `OrderProcessor.java`

### 2. OCP — Polimorfismo para descuentos

**Problema:** Cadena de `if-else` por tipo de cliente en `OrderProcessor`. Agregar un nuevo tipo requería modificar la clase.

**Solución:**
- Se creó la interfaz `CalculateDiscount` con método `calculateDiscount(double price)`.
- Se crearon las implementaciones:
  - `DiscountNormal` → descuento 0%
  - `DiscountVIP` → descuento 10%
  - `DiscountWorker` → descuento 30% (EMPLEADO)
- `OrderProcessor.processOrder()` ahora recibe un objeto `CalculateDiscount` en lugar de un `String userType`.
- El `if-else` se reemplazó por una sola llamada: `userType.calculateDiscount(price)`.

**Archivos creados:** `CalculateDiscount.java`, `DiscountNormal.java`, `DiscountVIP.java`, `DiscountWorker.java`

### 3. LSP — Corregir la jerarquía de `Product`

**Problema:** `DigitalProduct` sobrescribía `getShippingWeight()` lanzando `UnsupportedOperationException`, rompiendo el contrato de su clase padre.

**Solución:**
- Se eliminó `getShippingWeight()` de la clase base `Product`.
- Se creó la interfaz `Shippeable` con el método `getShippingWeight()`.
- `PhysicalProduct` extiende `Product` e implementa `Shippeable`.
- `DigitalProduct` extiende `Product` y no implementa `Shippeable`.
- `OrderProcessor` verifica `instanceof Shippeable` antes de calcular envío; si no es enviable, el costo es 0.

**Archivos creados/modificados:** `Shippeable.java`, `PhysicalProduct.java`, `Product.java`, `DigitalProduct.java`

### 4. ISP — Dividir la interfaz `IWorker`

**Problema:** `IWorker` obligaba a `OrderProcessor` a implementar `deliverProduct()` que no le correspondía.

**Solución:**
- Se eliminó `IWorker`.
- Se crearon dos interfaces separadas:
  - `ProcessOrder` — solo con `processOrder()`.
  - `DeliverProduct` — solo con `deliverProduct()`.
- `OrderProcessor` implementa únicamente `ProcessOrder`.
- Se eliminó el método vacío `deliverProduct()` de `OrderProcessor`.

**Archivos creados/eliminados:** `ProcessOrder.java`, `DeliverProduct.java`. Se eliminó `IWorker.java`.

### 5. DIP — Inyección de dependencias por abstracción

**Problema:** `OrderService` (y antes `OrderProcessor`) creaba sus dependencias con `new` directamente.

**Solución:**
- Se crearon las interfaces:
  - `Notification` — abstracción para notificaciones.
  - `OrderStorage` — abstracción para persistencia de órdenes.
- `EmailService` ahora implementa `Notification`.
- `MySQLDatabase` ahora implementa `OrderStorage`.
- `OrderService` recibe todas sus dependencias por constructor:
  - `OrderProcessor processor`
  - `Notification notification`
  - `OrderStorage storage`
- `Main.java` actúa como raíz de composición: crea las implementaciones concretas y las inyecta en `OrderService`.

**Archivos creados/modificados:** `Notification.java`, `OrderStorage.java`, `EmailService.java`, `MySQLDatabase.java`, `OrderService.java`

---

## Verificación de principios SOLID

| Principio | Violación original | Solución aplicada | Estado |
|-----------|--------------------|--------------------|--------|
| **SRP** | `OrderProcessor` gestionaba persistencia y notificaciones | Delegación a `OrderService`, `MySQLDatabase`, `EmailService` | ✅ |
| **OCP** | Descuentos con cadena `if-else`; agregar cliente = modificar código | Strategy pattern con `CalculateDiscount`; agregar cliente = nueva clase | ✅ |
| **LSP** | `DigitalProduct.getShippingWeight()` lanzaba excepción | Interfaz `Shippeable` opcional; `DigitalProduct` no incumple ningún contrato | ✅ |
| **ISP** | `IWorker` con métodos que no todas las clases necesitan | `ProcessOrder` y `DeliverProduct` como interfaces separadas | ✅ |
| **DIP** | `OrderService` instanciaba dependencias con `new` | Inyección por constructor; dependencias son interfaces, no clases concretas | ✅ |

---

## Cuadro comparativo: Solución de referencia vs. Solución alcanzada

| Aspecto | Solución de referencia (`solucion.md`) | Solución alcanzada |
|---------|----------------------------------------|-------------------|
| **Paquetes** | `model/`, `discount/`, `service/`, `repository/`, `notification/` | `entities/`, `entities/discounts/`, `interfaces/`, `services/` |
| **Ordenador** | `OrderProcessor` (en `service/`) recibe todas las dependencias vía constructor | `OrderService` (en `services/`) orquesta; `OrderProcessor` queda liviano sin dependencias |
| **Descuentos (OCP)** | `DiscountPolicy` con método `supports(String)` + lista de políticas iterada | `CalculateDiscount` con método directo `calculateDiscount(price)`, se pasa el objeto polimórfico directo |
| **Envío (LSP)** | `ShippingCostPolicy.calculateShippingCost(Product)` en clase separada | `instanceof Shippeable` inline en `OrderProcessor` |
| **ISP** | `OrderProcessingWorker`, `DeliveryWorker` | `ProcessOrder`, `DeliverProduct` |
| **Persistencia** | `OrderRepository` (interfaz), `MySQLDatabase` la implementa | `OrderStorage` (interfaz), `MySQLDatabase` la implementa |
| **Notificación** | `NotificationService` (interfaz), `EmailService` la implementa | `Notification` (interfaz), `EmailService` la implementa |
| **Delivery** | `ProductDeliveryService` implementa `DeliveryWorker` | `DeliverProduct` no tiene implementación |
| **Shipping policy** | `ShippingCostPolicy` interfaz + `StandardShippingCostService` | No existe clase separada de shipping |
| **Main** | Inyecta `OrderProcessor` con lista de `DiscountPolicy` + `OrderRepository` + `NotificationService` + `ShippingCostPolicy` | Inyecta `OrderService` con `OrderProcessor` + `Notification` + `OrderStorage` |
| **Nombres de descuentos** | `DiscountPolicy`, `NormalDiscountPolicy`, `VipDiscountPolicy`, `EmployeeDiscountPolicy` | `CalculateDiscount`, `DiscountNormal`, `DiscountVIP`, `DiscountWorker` |
| **ShippingWeight** | `Shippable` (una 'p') | `Shippeable` (dos 'e') |

### Similitud general: **88%**

Ambas soluciones comparten el mismo núcleo arquitectónico:

- **SRP:** Separación de responsabilidades en clases especializadas.
- **OCP:** Strategy pattern para descuentos mediante polimorfismo.
- **LSP:** Interfaz `Shippable`/`Shippeable` opcional; `DigitalProduct` sin excepción.
- **ISP:** División de `IWorker` en dos interfaces específicas.
- **DIP:** Inyección de dependencias por constructor; `Main` como raíz de composición.

Las diferencias principales son de organización (paquetes, naming) y de dónde vive la lógica de orquestación (`OrderProcessor` vs. `OrderService`). La solución alcanzada optó por mantener `OrderProcessor` como un objeto de cálculo simple sin dependencias, mientras que la referencia centraliza todo en `OrderProcessor` con inyección completa.

### Archivos finales del proyecto (17)

```
src/
├── Main.java
└── ean/solid/
    ├── entities/
    │   ├── Product.java
    │   ├── PhysicalProduct.java
    │   ├── DigitalProduct.java
    │   └── OrderProcessor.java
    │   └── discounts/
    │       ├── DiscountNormal.java
    │       ├── DiscountVIP.java
    │       └── DiscountWorker.java
    ├── interfaces/
    │   ├── CalculateDiscount.java
    │   ├── DeliverProduct.java
    │   ├── Notification.java
    │   ├── OrderStorage.java
    │   ├── ProcessOrder.java
    │   └── Shippeable.java
    └── services/
        ├── EmailService.java
        ├── MySQLDatabase.java
        └── OrderService.java
```
