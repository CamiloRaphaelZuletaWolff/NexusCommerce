"use client";

import { RequireAuth } from "@/components/auth/require-auth";
import { CheckoutForm } from "@/components/checkout/checkout-form";

export default function CheckoutPage() {
  return (
    <RequireAuth>
      <div className="container-page py-10">
        <h1 className="mb-8 text-3xl font-extrabold">Checkout</h1>
        <CheckoutForm />
      </div>
    </RequireAuth>
  );
}
