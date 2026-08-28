// RailSaathi Strict Order State Machine

export type OrderStatus =
  | 'REQUESTED'
  | 'MATCHING'
  | 'OFFERED_TO_VENDOR'
  | 'VENDOR_ACCEPTED'
  | 'PRICE_CONFIRMED'
  | 'CUSTOMER_CONFIRMED'
  | 'FULFILLING'
  | 'COMPLETED'
  | 'REJECTED'
  | 'EXPIRED'
  | 'CUSTOMER_CANCELLED'
  | 'VENDOR_CANCELLED';

const VALID_TRANSITIONS: Record<OrderStatus, OrderStatus[]> = {
  REQUESTED: ['MATCHING', 'OFFERED_TO_VENDOR', 'REJECTED', 'EXPIRED', 'CUSTOMER_CANCELLED'],
  MATCHING: ['OFFERED_TO_VENDOR', 'REJECTED', 'EXPIRED', 'CUSTOMER_CANCELLED'],
  OFFERED_TO_VENDOR: ['VENDOR_ACCEPTED', 'REJECTED', 'EXPIRED', 'CUSTOMER_CANCELLED'],
  VENDOR_ACCEPTED: ['PRICE_CONFIRMED', 'VENDOR_CANCELLED', 'CUSTOMER_CANCELLED', 'EXPIRED'],
  PRICE_CONFIRMED: ['CUSTOMER_CONFIRMED', 'CUSTOMER_CANCELLED', 'VENDOR_CANCELLED', 'EXPIRED'],
  CUSTOMER_CONFIRMED: ['FULFILLING', 'CUSTOMER_CANCELLED', 'VENDOR_CANCELLED'],
  FULFILLING: ['COMPLETED', 'CUSTOMER_CANCELLED', 'VENDOR_CANCELLED'],
  COMPLETED: [],
  REJECTED: [],
  EXPIRED: [],
  CUSTOMER_CANCELLED: [],
  VENDOR_CANCELLED: []
};

export class OrderStateMachine {
  static canTransition(from: OrderStatus, to: OrderStatus): boolean {
    const allowed = VALID_TRANSITIONS[from];
    return allowed ? allowed.includes(to) : false;
  }

  static assertTransition(from: OrderStatus, to: OrderStatus) {
    if (!this.canTransition(from, to)) {
      throw new Error(`Illegal state transition from ${from} to ${to}`);
    }
  }
}
