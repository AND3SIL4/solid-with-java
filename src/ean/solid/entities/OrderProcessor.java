package ean.solid.entities;

import ean.solid.interfaces.CalculateDiscount;
import ean.solid.interfaces.ProcessOrder;
import ean.solid.interfaces.Shippeable;

public class OrderProcessor implements ProcessOrder {
    @Override
    public double processOrder(Product product, CalculateDiscount userType) {
        double price = product.getPrice();
        double discount = userType.calculateDiscount(price);
        double finalPrice = price - discount;
        double shippingCost = 0;

        // Validate if the product containts a shipping method
        if (product instanceof Shippeable) {
            shippingCost = ((Shippeable) product).getShippingWeight() * 2.5;
        }
        return finalPrice + shippingCost;
    }
}