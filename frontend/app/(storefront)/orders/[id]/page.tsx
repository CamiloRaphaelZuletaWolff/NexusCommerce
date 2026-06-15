import { RequireAuth } from "@/components/auth/require-auth";
import { OrderDetail } from "@/components/orders/order-detail";

export default async function OrderPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ success?: string }>;
}) {
  const { id } = await params;
  const { success } = await searchParams;
  return (
    <RequireAuth>
      <OrderDetail id={id} success={success === "1"} />
    </RequireAuth>
  );
}
