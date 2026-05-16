package ean.solid;

public class Product {
    private String name;
    private double price;

    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    // Calcular el costo de envío basado en peso.
    public double getShippingWeight() {
        return 5.0; // Peso por defecto en kg
    }
}