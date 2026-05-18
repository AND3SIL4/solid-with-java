import ean.solid.entities.DigitalProduct;
import ean.solid.entities.OrderProcessor;
import ean.solid.entities.PhysicalProduct;
import ean.solid.entities.Product;
import ean.solid.entities.discounts.DiscountNormal;
import ean.solid.entities.discounts.DiscountVIP;
import ean.solid.services.EmailService;
import ean.solid.services.MySQLDatabase;
import ean.solid.services.OrderService;

public class Main {
    public static void main(String[] args) {
        OrderService orderService = new OrderService(new OrderProcessor(), new EmailService(), new MySQLDatabase());

        Product laptop = new PhysicalProduct("Laptop", 1000);
        Product software = new DigitalProduct("Antivirus", 50);

        DiscountVIP vip = new DiscountVIP();
        System.out.println("== Procesando Producto Físico ==");
        orderService.processOrder(laptop, vip);

        DiscountNormal normal = new DiscountNormal();
        System.out.println("\n== Procesando Producto Digital ==");
        orderService.processOrder(software, normal);
    }
}