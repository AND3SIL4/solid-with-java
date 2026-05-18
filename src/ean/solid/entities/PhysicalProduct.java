package ean.solid.entities;

import ean.solid.interfaces.Shippeable;

public class PhysicalProduct extends Product implements Shippeable {
  public PhysicalProduct(String name, double price) {
    super(name, price);
  }

  @Override
  public double getShippingWeight() {
    return 5.0;
  }
}