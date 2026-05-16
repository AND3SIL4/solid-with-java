import ean.solid.DigitalProduct;
import ean.solid.OrderProcessor;
import ean.solid.Product;

public class Main {
    public static void main(String[] args) {
        OrderProcessor processor = new OrderProcessor();

        System.out.println("== Procesando Producto Físico ==");
        Product laptop = new Product("Laptop", 1000);
        processor.processOrder(laptop, "VIP");

        System.out.println("\n== Procesando Producto Digital ==");
        Product software = new DigitalProduct("Antivirus", 50);
        
        try {
            // Esto fallará debido a la violación del principio de sustitución de Liskov (LSP)
            processor.processOrder(software, "NORMAL");
        } catch (Exception e) {
            System.err.println("¡Error de sistema! " + e.getMessage());
        }
    }
}