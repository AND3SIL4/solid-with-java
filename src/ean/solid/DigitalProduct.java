package ean.solid;

// ❌ Viola LSP: Un producto digital no tiene peso de envío, rompiendo el contrato padre
public class DigitalProduct extends Product {

    public DigitalProduct(String name, double price) {
        super(name, price);
    }

    @Override
    public double getShippingWeight() {
        throw new UnsupportedOperationException("Los productos digitales no tienen peso físico para envío.");
    }
}