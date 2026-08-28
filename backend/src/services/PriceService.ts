// RailSaathi Price Service & Validation Engine
// Enforces allowed unit price tiers and strict server-side calculation.

export const ALLOWED_PRICES = [5, 10, 15, 20, 30, 40, 50] as const;
export type AllowedPrice = typeof ALLOWED_PRICES[number];

export class PriceService {
  static isPriceAllowed(price: number): price is AllowedPrice {
    return ALLOWED_PRICES.includes(price as AllowedPrice);
  }

  static validateQuantity(qty: number): number {
    if (!Number.isInteger(qty) || qty < 1) {
      throw new Error('Quantity must be an integer of at least 1');
    }
    if (qty > 10) {
      throw new Error('Maximum quantity is 10 items per request');
    }
    return qty;
  }

  static calculateTotal(quantity: number, unitPrice: number): number {
    const validQty = this.validateQuantity(quantity);
    if (!this.isPriceAllowed(unitPrice)) {
      throw new Error(`Invalid unit price ₹${unitPrice}. Allowed prices are: ₹${ALLOWED_PRICES.join(', ₹')}`);
    }
    return validQty * unitPrice;
  }
}
