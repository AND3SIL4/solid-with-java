package ean.solid.entities.discounts;

import ean.solid.interfaces.CalculateDiscount;

public class DiscountVIP implements CalculateDiscount {
  @Override
  public double calculateDiscount(double price) {
    return price * 0.10;
  }
}
