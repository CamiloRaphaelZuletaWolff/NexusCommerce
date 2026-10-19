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
import { safeRedirect } from "@/lib/utils";

const schema = z.object({
  fullName: z.string().max(150).optional(),
  email: z.string().min(1, "Email is required").email("Enter a valid email"),
  password: z
    .string()
    .min(8, "Use at least 8 characters")
    .max(100, "That's a little too long"),
});
type Values = z.infer<typeof schema>;

export function RegisterForm({ redirectTo }: { redirectTo?: string }) {
  const { register: registerUser } = useAuth();
  const router = useRouter();
  const [formError, setFormError] = useState<string | null>(null);
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<Values>({ resolver: zodResolver(schema) });

  const onSubmit = async (values: Values) => {
    setFormError(null);
    try {
      await registerUser({
        email: values.email,
        password: values.password,
        fullName: values.fullName || undefined,
      });
      toast.success("Account created", "You're signed in.");
      router.push(safeRedirect(redirectTo));
    } catch (err) {
      const msg =
        err instanceof ApiError && err.status === 409
          ? "An account with this email already exists"
          : err instanceof ApiError
            ? err.message
            : "Something went wrong. Try again.";
      setFormError(msg);
    }
  };

  return (
    <div>
      <h1 className="text-2xl font-extrabold">Create your account</h1>
      <p className="mt-1.5 text-sm text-ink-soft">Shop every store-world from one cart.</p>

      <form onSubmit={handleSubmit(onSubmit)} className="mt-7 space-y-4" noValidate>
        {formError && (
          <p className="rounded-md border border-danger/30 bg-danger/8 px-3 py-2 text-sm font-medium text-danger">
            {formError}
          </p>
        )}
        <Field label="Full name" htmlFor="fullName" error={errors.fullName?.message} hint="Optional">
          <Input id="fullName" autoComplete="name" placeholder="Ada Lovelace" {...register("fullName")} />
        </Field>
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
        <Field label="Password" htmlFor="password" error={errors.password?.message} hint="At least 8 characters">
          <Input
            id="password"
            type="password"
            autoComplete="new-password"
            placeholder="••••••••"
            aria-invalid={!!errors.password}
            {...register("password")}
          />
        </Field>
        <Button type="submit" variant="accent" size="lg" className="w-full" loading={isSubmitting}>
          Create account
        </Button>
      </form>

      <p className="mt-6 text-center text-sm text-ink-soft">
        Already have an account?{" "}
        <Link
          href={redirectTo ? `/login?redirect=${encodeURIComponent(redirectTo)}` : "/login"}
          className="font-semibold text-nexus underline-offset-2 hover:underline"
        >
          Sign in
        </Link>
      </p>
    </div>
  );
}
