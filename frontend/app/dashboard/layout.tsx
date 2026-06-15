import { RequireAuth } from "@/components/auth/require-auth";
import { DashboardShell } from "@/components/dashboard/dashboard-shell";

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <RequireAuth roles={["STORE_MANAGER", "ADMIN"]}>
      <DashboardShell>{children}</DashboardShell>
    </RequireAuth>
  );
}
