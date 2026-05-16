package ean.solid;

// ❌ Viola ISP: Una interfaz "gorda" con métodos que no todos los trabajadores necesitan
public interface IWorker {
    void processOrder(Product product, String userType);
    void deliverProduct(Product product);
}