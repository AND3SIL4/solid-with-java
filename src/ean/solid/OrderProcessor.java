package ean.solid;

// ❌ Viola SRP: Esta clase procesa precios, guarda en DB y manda emails.
// ❌ Viola OCP: Tiene un montón de IFs para calcular descuentos. Si hay que añadir un nuevo tipo de cliente, hay que modificar esta clase.
// ❌ Viola ISP: Implementa IWorker pero no reparte productos, por lo que tiene métodos inútiles o lanza excepciones.
// ❌ Viola DIP: Depende directamente de MySQLDatabase y EmailService, no de interfaces.
public class OrderProcessor implements IWorker {

    private MySQLDatabase database;
    private EmailService emailService;

    public OrderProcessor() {
        // Dependencias fuertemente acopladas (DIP)
        this.database = new MySQLDatabase();
        this.emailService = new EmailService();
    }

    @Override
    public void processOrder(Product product, String userType) {
        double price = product.getPrice();

        // ❌ OCP: Si agregamos un cliente "PREMIUM", debemos venir a editar este código
        double discount = 0;
        if (userType.equals("NORMAL")) {
            discount = 0;
        } else if (userType.equals("VIP")) {
            discount = price * 0.10;
        } else if (userType.equals("EMPLEADO")) {
            discount = price * 0.30;
        }

        double finalPrice = price - discount;

        // ❌ LSP en ejecución: Si pasamos un DigitalProduct, este método estallará
        double shippingCost = product.getShippingWeight() * 2.5;
        double total = finalPrice + shippingCost;

        // ❌ SRP: Manejo de base de datos dentro del procesador de órdenes
        database.saveOrder("Producto: " + product.getName() + ", Total: $" + total);

        // ❌ SRP: Manejo de notificaciones dentro del procesador de órdenes
        emailService.sendEmail("Su orden de " + product.getName() + " ha sido procesada.");
    }

    @Override
    public void deliverProduct(Product product) {
        // ❌ ISP: El procesador de órdenes no envía productos físicamente, tiene que lanzar un error o quedar vacío
        throw new UnsupportedOperationException("El OrderProcessor no reparte productos.");
    }
}