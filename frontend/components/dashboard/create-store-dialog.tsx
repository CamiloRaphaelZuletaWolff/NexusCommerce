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
import { STORE_HUES, STORE_TYPES } from "@/lib/store-theme";
import type { StoreCreateRequest } from "@/types/api";

const schema = z.object({
  name: z.string().min(1, "Required").max(150),
  description: z.string().max(2000).optional(),
  storeType: z.enum(["ELECTRONICS", "FASHION", "FURNITURE", "KITCHENWARE", "HOME_DECOR"]),
});
type Values = z.infer<typeof schema>;

export function CreateStoreDialog() {
  const [open, setOpen] = useState(false);
  const queryClient = useQueryClient();
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<Values>({ resolver: zodResolver(schema), defaultValues: { storeType: "ELECTRONICS" } });

  const create = useMutation({
    mutationFn: (data: StoreCreateRequest) => api.stores.create(data),
    onSuccess: (store) => {
      queryClient.invalidateQueries({ queryKey: ["stores"] });
      toast.success("Store created", store.name);
      setOpen(false);
      reset();
    },
    onError: (err) => {
      const msg = err instanceof ApiError ? err.message : "Couldn't create store";
      toast.error("Create failed", msg);
    },
  });

  return (
    <Dialog open={open} onOpenChange={setOpen}>
      <DialogTrigger asChild>
        <Button variant="accent" size="sm">
          <Plus className="size-4" />
          New store
        </Button>
      </DialogTrigger>
      <DialogContent title="Create a store" description="Open a new store-world on the platform.">
        <form
          onSubmit={handleSubmit((v) => create.mutate({ name: v.name, description: v.description || undefined, storeType: v.storeType }))}
          className="space-y-4"
          noValidate
        >
          <Field label="Store name" htmlFor="s-name" error={errors.name?.message}>
            <Input id="s-name" {...register("name")} aria-invalid={!!errors.name} />
          </Field>
          <Field label="Store type" htmlFor="s-type" error={errors.storeType?.message}>
            <Select id="s-type" {...register("storeType")}>
              {STORE_TYPES.map((t) => (
                <option key={t} value={t}>{STORE_HUES[t].label}</option>
              ))}
            </Select>
          </Field>
          <Field label="Description" htmlFor="s-desc" error={errors.description?.message} hint="Optional">
            <Textarea id="s-desc" {...register("description")} />
          </Field>
          <div className="flex justify-end gap-2 pt-2">
            <Button type="button" variant="ghost" onClick={() => setOpen(false)}>Cancel</Button>
            <Button type="submit" variant="accent" loading={create.isPending}>Create store</Button>
          </div>
        </form>
      </DialogContent>
    </Dialog>
  );
}
