"use client";

import { useState } from "react";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { Dialog, DialogContent, DialogTrigger } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input, Textarea, Select } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { api, ApiError } from "@/lib/api";
import { toast } from "@/stores/toast";
import type { CategoryResponse, StoreResponse } from "@/types/api";

const schema = z.object({
  storeId: z.string().min(1, "Select a store"),
  sku: z.string().min(1, "Required").max(80),
  name: z.string().min(1, "Required").max(200),
  price: z.coerce.number().min(0, "Must be ≥ 0"),
  currency: z.string().length(3).optional().or(z.literal("")),
  categorySlug: z.string().min(1, "Select a category"),
  brand: z.string().optional(),
  tags: z.string().optional(),
  description: z.string().max(5000).optional(),
  status: z.enum(["ACTIVE", "DRAFT", "ARCHIVED"]),
});
type Values = z.infer<typeof schema>;

export function CreateProductDialog({
  stores,
  categories,
}: {
  stores: StoreResponse[];
  categories: CategoryResponse[];
}) {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Values>({
    resolver: zodResolver(schema),
    defaultValues: { status: "ACTIVE", currency: "USD" },
  });

  const create = useMutation({
    mutationFn: (v: Values) =>
      api.catalog.createProduct({
        storeId: v.storeId,
        sku: v.sku,
        name: v.name,
        price: v.price,
        currency: v.currency || "USD",
        categorySlug: v.categorySlug,
        brand: v.brand || undefined,
        tags: v.tags ? v.tags.split(",").map((t) => t.trim()).filter(Boolean) : undefined,
        description: v.description || undefined,
        status: v.status,
      }),
    onSuccess: (p) => {
      queryClient.invalidateQueries({ queryKey: ["products"] });
      toast.success("Product created", p.name);
      setOpen(false);
      reset();
    },
    onError: (err) => {
      const msg = err instanceof ApiError ? err.message : "Couldn't create product";
      toast.error("Create failed", msg);
    },
  });

  const disabled = stores.length === 0;

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="accent" size="sm" disabled={disabled} title={disabled ? "Create a store first" : undefined}>
          <Plus className="size-4" />
          New product
        </Button>
      </DialogTrigger>
      <DialogContent title="Create a product" description="Add a product to one of your stores.">
        <form onSubmit={handleSubmit((v) => create.mutate(v))} className="space-y-4" noValidate>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Store" htmlFor="p-store" error={errors.storeId?.message}>
              <Select id="p-store" {...register("storeId")}>
                <option value="">Select…</option>
                {stores.map((s) => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </Select>
            </Field>
            <Field label="Category" htmlFor="p-cat" error={errors.categorySlug?.message}>
              <Select id="p-cat" {...register("categorySlug")}>
                <option value="">Select…</option>
                {categories.map((c) => (
                  <option key={c.slug} value={c.slug}>{c.name}</option>
                ))}
              </Select>
            </Field>
          </div>
          <Field label="Name" htmlFor="p-name" error={errors.name?.message}>
            <Input id="p-name" {...register("name")} aria-invalid={!!errors.name} />
          </Field>
          <div className="grid gap-4 sm:grid-cols-3">
            <Field label="SKU" htmlFor="p-sku" error={errors.sku?.message}>
              <Input id="p-sku" className="tabular" {...register("sku")} aria-invalid={!!errors.sku} />
            </Field>
            <Field label="Price" htmlFor="p-price" error={errors.price?.message}>
              <Input id="p-price" type="number" step="0.01" min="0" className="tabular" {...register("price")} aria-invalid={!!errors.price} />
            </Field>
            <Field label="Currency" htmlFor="p-cur" error={errors.currency?.message}>
              <Input id="p-cur" maxLength={3} className="tabular uppercase" {...register("currency")} />
            </Field>
          </div>
          <div className="grid gap-4 sm:grid-cols-2">
            <Field label="Brand" htmlFor="p-brand" hint="Optional">
              <Input id="p-brand" {...register("brand")} />
            </Field>
            <Field label="Tags" htmlFor="p-tags" hint="Comma-separated">
              <Input id="p-tags" placeholder="portable, premium" {...register("tags")} />
            </Field>
          </div>
          <Field label="Description" htmlFor="p-desc" hint="Optional">
            <Textarea id="p-desc" {...register("description")} />
          </Field>
          <Field label="Status" htmlFor="p-status">
            <Select id="p-status" {...register("status")}>
              <option value="ACTIVE">Active</option>
              <option value="DRAFT">Draft</option>
              <option value="ARCHIVED">Archived</option>
            </Select>
          </Field>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setOpen(false)}>Cancel</Button>
            <Button type="submit" variant="accent" loading={create.isPending}>Create product</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
