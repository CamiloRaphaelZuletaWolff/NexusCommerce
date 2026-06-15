"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import * as Dropdown from "@radix-ui/react-dropdown-menu";
import { LayoutDashboard, LogOut, Package, User } from "lucide-react";
import { useAuth } from "@/components/auth-provider";
import { toast } from "@/stores/toast";

export function UserMenu() {
  const { user, logout, hasRole } = useAuth();
  const router = useRouter();
  if (!user) return null;

  const initial = (user.fullName || user.email).trim().charAt(0).toUpperCase();
  const canManage = hasRole("STORE_MANAGER", "ADMIN");

  const onLogout = async () => {
    await logout();
    toast.success("Signed out");
    router.push("/");
  };

  return (
    <Dropdown.Root>
      <Dropdown.Trigger
        className="grid size-10 place-items-center rounded-full bg-nexus text-sm font-bold text-white outline-none transition-transform hover:scale-105 focus-visible:ring-4 focus-visible:ring-nexus/20"
        aria-label="Account menu"
      >
        {initial || <User className="size-4" />}
      </Dropdown.Trigger>
      <Dropdown.Portal>
        <Dropdown.Content
          align="end"
          sideOffset={8}
          className="z-50 min-w-56 rounded-lg border border-line bg-surface p-1.5 shadow-lg"
        >
          <div className="px-2.5 py-2">
            <p className="truncate text-sm font-semibold text-ink">{user.fullName || "Account"}</p>
            <p className="truncate text-xs text-ink-faint">{user.email}</p>
            <div className="mt-1.5 flex flex-wrap gap-1">
              {user.roles.map((r) => (
                <span
                  key={r}
                  className="rounded-full bg-nexus-tint px-1.5 py-0.5 text-[10px] font-semibold tracking-wide text-nexus"
                >
                  {r.replace("_", " ")}
                </span>
              ))}
            </div>
          </div>
          <Dropdown.Separator className="my-1 h-px bg-line" />
          <MenuLink href="/orders" icon={Package}>
            My orders
          </MenuLink>
          {canManage && (
            <MenuLink href="/dashboard" icon={LayoutDashboard}>
              Dashboard
            </MenuLink>
          )}
          <Dropdown.Separator className="my-1 h-px bg-line" />
          <Dropdown.Item
            onSelect={onLogout}
            className="flex cursor-pointer items-center gap-2.5 rounded-md px-2.5 py-2 text-sm font-medium text-danger outline-none transition-colors data-[highlighted]:bg-danger/10"
          >
            <LogOut className="size-4" />
            Sign out
          </Dropdown.Item>
        </Dropdown.Content>
      </Dropdown.Portal>
    </Dropdown.Root>
  );
}

function MenuLink({
  href,
  icon: Icon,
  children,
}: {
  href: string;
  icon: React.ComponentType<{ className?: string }>;
  children: React.ReactNode;
}) {
  return (
    <Dropdown.Item asChild>
      <Link
        href={href}
        className="flex cursor-pointer items-center gap-2.5 rounded-md px-2.5 py-2 text-sm font-medium text-ink outline-none transition-colors data-[highlighted]:bg-paper-2"
      >
        <Icon className="size-4 text-ink-soft" />
        {children}
      </Link>
    </Dropdown.Item>
  );
}
