// RailSaathi Price Service & Validation Engine
// Enforces allowed unit price tiers and strict server-side calculation.

export const ALLOWED_PRICES = [5, 10, 15, 20, 30, 40, 50] as const;
export type AllowedPrice = typeof ALLOWED_PRICES[number];

export class PriceService {
  static isPriceAllowed(price: number): price is AllowedPrice {
    return ALLOWED_PRICES.includes(price as AllowedPrice);
  }

  // Coerce / clamp quantity within [1, 10] bounds (strictly identical to Android client coerceIn(1, 10))
  static coerceQuantity(qty: any): number {
    const parsed = Math.floor(Number(qty) || 1);
    return Math.max(1, Math.min(10, parsed));
  }

  // Documented Validation Evaluation for all 9 audit cases
  static evaluateQuantity(input: any): {
    input: any;
    isValidInteger: boolean;
    isInRange: boolean;
    clampedValue: number;
    action: 'ACCEPTED' | 'CLAMPED' | 'DEFAULTED';
    ruleExplanation: string;
  } {
    if (input === undefined || input === null) {
      return {
        input,
        isValidInteger: false,
        isInRange: false,
        clampedValue: 1,
        action: 'DEFAULTED',
        ruleExplanation: 'Missing or null quantity defaults to minimum legal boundary of 1.'
      };
    }
    const num = Number(input);
    const isInt = Number.isInteger(num);
    const inRange = isInt && num >= 1 && num <= 10;
    const clamped = this.coerceQuantity(input);

    return {
      input,
      isValidInteger: isInt,
      isInRange: inRange,
      clampedValue: clamped,
      action: inRange ? 'ACCEPTED' : (isInt ? 'CLAMPED' : 'CLAMPED'),
      ruleExplanation: inRange
        ? `Valid integer within [1, 10].`
        : `Invalid/out-of-bounds quantity coerced to ${clamped} (enforcing [1, 10] boundary).`
    };
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
    const validQty = this.coerceQuantity(quantity);
    if (!this.isPriceAllowed(unitPrice)) {
      throw new Error(`Invalid unit price ₹${unitPrice}. Allowed prices are: ₹${ALLOWED_PRICES.join(', ₹')}`);
    }
    return validQty * unitPrice;
  }
}
