"use client";

import { useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { useAuth } from "@/components/auth-provider";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Field } from "@/components/ui/field";
import { toast } from "@/stores/toast";
import { ApiError } from "@/lib/api";

const schema = z.object({
  email: z.string().min(1, "Email is required").email("Enter a valid email"),
  password: z.string().min(1, "Password is required"),
});
type Values = z.infer<typeof schema>;

const DEMO = [
  { label: "Admin", email: "admin@nexus.local" },
  { label: "Manager", email: "manager@nexus.local" },
  { label: "Customer", email: "customer@nexus.local" },
];

export function LoginForm({ redirectTo }: { redirectTo?: string }) {
  const { login } = useAuth();
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<Values>({ resolver: zodResolver(schema) });

  const onSubmit = async (values: Values) => {
    setFormError(null);
    try {
      await login(values);
      toast.success("Welcome back");
      router.push(redirectTo || "/");
    } catch (err) {
      const msg =
        err instanceof ApiError && err.status === 401
          ? "Incorrect email or password"
          : err instanceof ApiError
            ? err.message
            : "Something went wrong. Try again.";
      setFormError(msg);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-extrabold">Welcome back</h1>
      <p className="mt-1.5 text-sm text-ink-soft">Sign in to continue shopping.</p>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-7 space-y-4" noValidate>
        {formError && (
          <p className="rounded-md border border-danger/30 bg-danger/8 px-3 py-2 text-sm font-medium text-danger">
            {formError}
          </p>
        )}
        <Field label="Email" htmlFor="email" error={errors.email?.message}>
          <Input
            id="email"
            type="email"
            autoComplete="email"
            placeholder="you@example.com"
            aria-invalid={!!errors.email}
            {...register("email")}
          />
        </Field>
        <Field label="Password" htmlFor="password" error={errors.password?.message}>
          <Input
            id="password"
            type="password"
            autoComplete="current-password"
            placeholder="••••••••"
            aria-invalid={!!errors.password}
            {...register("password")}
          />
        </Field>
        <Button type="submit" variant="accent" size="lg" className="w-full" loading={isSubmitting}>
          Sign in
        </Button>
      </form>

      <div className="mt-6 rounded-lg border border-line bg-surface p-3.5">
        <p className="eyebrow mb-2">Demo accounts · password Password123!</p>
        <div className="flex flex-wrap gap-1.5">
          {DEMO.map((d) => (
            <button
              key={d.email}
              type="button"
              onClick={() => {
                setValue("email", d.email);
                setValue("password", "Password123!");
              }}
              className="rounded-md bg-paper-2 px-2.5 py-1.5 text-xs font-semibold text-ink-soft transition-colors hover:bg-line hover:text-ink"
            >
              {d.label}
            </button>
          ))}
        </div>
      </div>

      <p className="mt-6 text-center text-sm text-ink-soft">
        New here?{" "}
        <Link
          href={redirectTo ? `/register?redirect=${encodeURIComponent(redirectTo)}` : "/register"}
          className="font-semibold text-nexus underline-offset-2 hover:underline"
        >
          Create an account
        </Link>
      </p>
    </div>
  );
}
