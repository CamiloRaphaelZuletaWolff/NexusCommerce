"use client";

import { useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { Loader2, ShieldAlert } from "lucide-react";
import { useAuth } from "@/components/auth-provider";
import { Button } from "@/components/ui/button";
import type { Role } from "@/types/api";

export function RequireAuth({
  children,
  roles,
}: {
  children: React.ReactNode;
  roles?: Role[];
}) {
  const { status, hasRole } = useAuth();
  const router = useRouter();
  const pathname = usePathname();

  useEffect(() => {
    if (status === "unauthenticated") {
      router.replace(`/login?redirect=${encodeURIComponent(pathname)}`);
    }
  }, [status, pathname, router]);

  if (status === "loading") {
    return (
      <div className="grid min-h-[50vh] place-items-center">
        <Loader2 className="size-6 animate-spin text-ink-faint" />
      </div>
    );
  }

  if (status === "unauthenticated") return null;

  if (roles && !hasRole(...roles)) {
    return (
      <div className="container-page grid min-h-[50vh] place-items-center py-16">
        <div className="max-w-md text-center">
          <div className="mx-auto grid size-14 place-items-center rounded-full bg-danger/10">
            <ShieldAlert className="size-6 text-danger" />
          </div>
          <h1 className="mt-4 text-2xl font-extrabold">Not authorized</h1>
          <p className="mt-2 text-ink-soft">
            This area is for store managers and admins. Your account doesn’t have access.
          </p>
          <Button asChild className="mt-6">
            <Link href="/">Back to home</Link>
          </Button>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
