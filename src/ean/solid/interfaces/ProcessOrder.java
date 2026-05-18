package ean.solid.interfaces;

import ean.solid.entities.Product;

public interface ProcessOrder {
  double processOrder(Product product, CalculateDiscount userType);
}
