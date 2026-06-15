"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Lock, ShieldCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { useCart } from "@/hooks/use-cart";
import { api, ApiError } from "@/lib/api";
import { toast } from "@/stores/toast";
import { formatMoney } from "@/lib/utils";
import type { CheckoutRequest, OrderResponse } from "@/types/api";

const schema = z.object({
  name: z.string().min(1, "Required").max(150),
  line1: z.string().min(1, "Required").max(200),
  line2: z.string().max(200).optional(),
  city: z.string().min(1, "Required").max(120),
  region: z.string().max(120).optional(),
  postalCode: z.string().min(1, "Required").max(20),
  country: z
    .string()
    .trim()
    .toUpperCase()
    .regex(/^[A-Z]{2}$/, "2-letter code, e.g. US"),
  number: z
    .string()
    .transform((s) => s.replace(/\s+/g, ""))
    .pipe(z.string().regex(/^\d{13,19}$/, "13–19 digits")),
  holderName: z.string().min(1, "Required").max(150),
  expiry: z.string().regex(/^(0[1-9]|1[0-2])\/\d{2}$/, "MM/YY"),
  cvv: z.string().regex(/^\d{3,4}$/, "3–4 digits"),
});
type Values = z.infer<typeof schema>;

export function CheckoutForm() {
  const { data: cart, isLoading } = useCart();
  const router = useRouter();
  const queryClient = useQueryClient();

  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors },
  } = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { country: "US" },
  });

  const placeOrder = useMutation({
    mutationFn: (req: CheckoutRequest) => api.checkout(req),
    onSuccess: async (order: OrderResponse) => {
      await queryClient.invalidateQueries({ queryKey: ["cart"] });
      await queryClient.invalidateQueries({ queryKey: ["orders"] });
      router.push(`/orders/${order.id}?success=1`);
    },
    onError: (err) => {
      const msg = err instanceof ApiError ? err.message : "Checkout failed. Try again.";
      toast.error("Checkout failed", msg);
    },
  });

  const onSubmit = (v: Values) => {
    placeOrder.mutate({
      shipping: {
        name: v.name,
        line1: v.line1,
        line2: v.line2 || undefined,
        city: v.city,
        region: v.region || undefined,
        postalCode: v.postalCode,
        country: v.country,
      },
      payment: { number: v.number, holderName: v.holderName, expiry: v.expiry, cvv: v.cvv },
    });
  };

  const fillDemo = () => {
    setValue("name", "Ada Lovelace");
    setValue("line1", "1 Market Street");
    setValue("city", "Lima");
    setValue("postalCode", "15001");
    setValue("country", "PE");
    setValue("number", "4111 1111 1111 1111");
    setValue("holderName", "Ada Lovelace");
    setValue("expiry", "12/30");
    setValue("cvv", "123");
  };

  const items = cart?.items ?? [];

  if (!isLoading && items.length === 0) {
    return (
      <div className="mx-auto max-w-md rounded-2xl border border-dashed border-line-strong bg-surface py-16 text-center">
        <h2 className="text-xl font-bold">Nothing to check out</h2>
        <p className="mt-1 text-ink-soft">Your cart is empty.</p>
        <Button asChild variant="accent" className="mt-6">
          <Link href="/catalog">Browse the catalog</Link>
        </Button>
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="grid items-start gap-8 lg:grid-cols-[1fr_360px]" noValidate>
      <div className="space-y-8">
        {/* Shipping */}
        <section className="rounded-2xl border border-line bg-surface p-6">
          <h2 className="text-lg font-bold">Shipping address</h2>
          <div className="mt-5 grid gap-4 sm:grid-cols-2">
            <Field className="sm:col-span-2" label="Full name" htmlFor="name" error={errors.name?.message}>
              <Input id="name" autoComplete="name" {...register("name")} aria-invalid={!!errors.name} />
            </Field>
            <Field className="sm:col-span-2" label="Address line 1" htmlFor="line1" error={errors.line1?.message}>
              <Input id="line1" autoComplete="address-line1" {...register("line1")} aria-invalid={!!errors.line1} />
            </Field>
            <Field className="sm:col-span-2" label="Address line 2" htmlFor="line2" error={errors.line2?.message} hint="Optional">
              <Input id="line2" autoComplete="address-line2" {...register("line2")} />
            </Field>
            <Field label="City" htmlFor="city" error={errors.city?.message}>
              <Input id="city" autoComplete="address-level2" {...register("city")} aria-invalid={!!errors.city} />
            </Field>
            <Field label="Region / state" htmlFor="region" error={errors.region?.message} hint="Optional">
              <Input id="region" autoComplete="address-level1" {...register("region")} />
            </Field>
            <Field label="Postal code" htmlFor="postalCode" error={errors.postalCode?.message}>
              <Input id="postalCode" autoComplete="postal-code" {...register("postalCode")} aria-invalid={!!errors.postalCode} />
            </Field>
            <Field label="Country" htmlFor="country" error={errors.country?.message} hint="ISO 2-letter">
              <Input id="country" autoComplete="country" maxLength={2} className="uppercase" {...register("country")} aria-invalid={!!errors.country} />
            </Field>
          </div>
        </section>

        {/* Payment */}
        <section className="rounded-2xl border border-line bg-surface p-6">
          <div className="flex items-center justify-between">
            <h2 className="flex items-center gap-2 text-lg font-bold">
              <Lock className="size-4 text-ink-soft" />
              Payment
            </h2>
            <button type="button" onClick={fillDemo} className="text-xs font-semibold text-nexus hover:underline">
              Use test card
            </button>
          </div>
          <div className="mt-5 grid gap-4 sm:grid-cols-2">
            <Field className="sm:col-span-2" label="Card number" htmlFor="number" error={errors.number?.message}>
              <Input id="number" inputMode="numeric" placeholder="4111 1111 1111 1111" className="tabular" {...register("number")} aria-invalid={!!errors.number} />
            </Field>
            <Field className="sm:col-span-2" label="Name on card" htmlFor="holderName" error={errors.holderName?.message}>
              <Input id="holderName" autoComplete="cc-name" {...register("holderName")} aria-invalid={!!errors.holderName} />
            </Field>
            <Field label="Expiry" htmlFor="expiry" error={errors.expiry?.message} hint="MM/YY">
              <Input id="expiry" placeholder="12/30" className="tabular" {...register("expiry")} aria-invalid={!!errors.expiry} />
            </Field>
            <Field label="CVV" htmlFor="cvv" error={errors.cvv?.message}>
              <Input id="cvv" inputMode="numeric" placeholder="123" className="tabular" {...register("cvv")} aria-invalid={!!errors.cvv} />
            </Field>
          </div>
          <p className="mt-4 flex items-start gap-2 rounded-md bg-success/8 px-3 py-2.5 text-xs text-success">
            <ShieldCheck className="mt-0.5 size-4 shrink-0" />
            Card data is encrypted at rest (AES-256-GCM). Only the brand and last 4 digits are
            kept in clear — your CVV is never stored.
          </p>
        </section>
      </div>

      {/* Summary */}
      <div className="sticky top-20 rounded-2xl border border-line bg-surface p-5">
        <h2 className="text-lg font-bold">Order summary</h2>
        <ul className="mt-4 max-h-64 space-y-3 overflow-y-auto">
          {items.map((item) => (
            <li key={item.id} className="flex items-start justify-between gap-3 text-sm">
              <span className="min-w-0">
                <span className="block truncate font-medium text-ink">{item.name}</span>
                <span className="tabular text-xs text-ink-faint">Qty {item.quantity}</span>
              </span>
              <span className="tabular shrink-0 font-semibold">{formatMoney(item.lineTotal)}</span>
            </li>
          ))}
        </ul>
        <div className="my-4 h-px bg-line" />
        <div className="flex justify-between text-sm">
          <span className="text-ink-soft">Subtotal</span>
          <span className="tabular font-semibold">{formatMoney(cart?.subtotal)}</span>
        </div>
        <p className="mt-1 text-xs text-ink-faint">Tax (7%) is added on the next step.</p>
        <Button type="submit" variant="accent" size="lg" className="mt-5 w-full" loading={placeOrder.isPending}>
          Place order
        </Button>
        <p className="mt-3 text-center text-xs text-ink-faint">
          Reserves stock, records an encrypted payment, and creates your order in one
          atomic transaction.
        </p>
      </div>
    </form>
  );
}
