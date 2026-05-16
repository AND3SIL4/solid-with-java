# Taller de Refactorización: Principios SOLID

## Contexto de la Aplicación

Este proyecto simula un sistema simplificado de procesamiento de órdenes comerciales. Actualmente, la aplicación es capaz de:

- Procesar órdenes de productos considerando el precio y el tipo de cliente (NORMAL, VIP, EMPLEADO) para aplicar descuentos.
- Calcular costos de envío basados en el peso del producto.
- Almacenar la información de la orden procesada en una base de datos (simulada por `MySQLDatabase`).
- Enviar notificaciones por correo electrónico al cliente (simulado por `EmailService`).

## El Problema

El código actual en la carpeta `src` está diseñado intencionalmente con malas prácticas arquitectónicas y **viola los cinco principios SOLID**.

Si exploras el código (en especial la clase `OrderProcessor`), notarás que:

- **SRP (Single Responsibility Principle):** Hay clases haciendo demasiadas cosas (cálculos, base de datos, correos).
- **OCP (Open/Closed Principle):** Agregar un nuevo tipo de cliente requiere modificar el código existente.
- **LSP (Liskov Substitution Principle):** El comportamiento de clases derivadas (`DigitalProduct`) rompe el flujo del programa cuando se espera una clase padre.
- **ISP (Interface Segregation Principle):** Las interfaces obligan a implementar métodos innecesarios (como repartir productos a quien solo los procesa).
- **DIP (Dependency Inversion Principle):** El procesador de órdenes depende de implementaciones concretas de la base de datos y el servicio de correo, no de abstracciones.

## Instrucciones del Taller

Tu objetivo como estudiante es **refactorizar este proyecto** para que cumpla completamente con los principios SOLID.

### Pasos a seguir:

1. **Analiza el código base:** Revisa las clases actuales en `src`. Observa cómo interactúan y lee los comentarios explicativos sobre las infracciones de SOLID.
2. **SRP - Responsabilidad Única:** Extrae las responsabilidades de persistencia (Base de datos) y notificaciones (Email) fuera de la lógica principal de la orden, delegándolas a sus respectivas clases.
3. **OCP - Abierto/Cerrado:** Refactoriza el cálculo de descuentos utilizando polimorfismo para que se puedan agregar nuevos tipos de clientes sin usar sentencias `if-else` o `switch`.
4. **LSP - Sustitución de Liskov:** Corrige la jerarquía de `Product` y `DigitalProduct`. Un producto digital no debería causar una excepción al calcular el costo de envío basado en el peso. Usa herencia o interfaces apropiadas.
5. **ISP - Segregación de Interfaces:** Divide la interfaz `IWorker` en interfaces más pequeñas y específicas (por ejemplo, una para procesar y otra para entregar/repartir), de modo que ninguna clase deba implementar métodos inútiles.
6. **DIP - Inversión de Dependencias:** Modifica las clases para que dependan de abstracciones (interfaces) y no de clases concretas. Permite que las dependencias, como la base de datos o el email, se inyecten mediante el constructor.
7. **Prueba tus cambios:** Asegúrate de que el punto de entrada de la aplicación `Main.java` se actualice adecuadamente con la nueva arquitectura y que el programa se ejecute sin errores y realice las mismas tareas originales de manera limpia.

## Recursos recomendados

- https://login.bdbiblioteca.universidadean.edu.co/login?qurl=https://doi.org%2f10.1007%2f978-1-4842-7971-7_1
- https://www.youtube.com/watch?v=2X50sKeBAcQ
- https://www.youtube.com/watch?v=g1shhx5Nvv8&t=311s

## Recomendación

Intenta refactorizar el código de acuerdo a lo que entendiste acerca de los principios SOLID. Sin embargo, si tienes dudas o si simplemente quieres comparar tu solución con la solución propuesta, puedes revisar el paso a paso de [esta solución](lib/solucion.md).

¡Buena suerte con la refactorización!
