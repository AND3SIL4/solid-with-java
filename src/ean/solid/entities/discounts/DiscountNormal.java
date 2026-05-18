package ean.solid.entities.discounts;

import ean.solid.interfaces.CalculateDiscount;

public class DiscountNormal implements CalculateDiscount {
  @Override
  public double calculateDiscount(double price) {
    return price * 0;
  }
}
