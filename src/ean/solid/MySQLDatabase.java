package ean.solid;

// ❌ Viola DIP: Clases concretas en lugar de abstracciones
public class MySQLDatabase {
    public void saveOrder(String orderDetails) {
        System.out.println("Guardando en base de datos MySQL: " + orderDetails);
    }
}