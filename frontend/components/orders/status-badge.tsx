import { Badge } from "@/components/ui/badge";
import { titleCase } from "@/lib/utils";
import type { OrderStatus } from "@/types/api";

const TONE: Record<OrderStatus, "success" | "warning" | "danger" | "neutral"> = {
  PAID: "success",
  FULFILLED: "success",
  PENDING: "warning",
  CANCELLED: "danger",
};

export function OrderStatusBadge({ status }: { status: OrderStatus }) {
  return <Badge tone={TONE[status] ?? "neutral"}>{titleCase(status)}</Badge>;
}
