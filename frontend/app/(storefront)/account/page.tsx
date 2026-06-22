"use client";

import { useQuery } from "@tanstack/react-query";
import { FavoritesEditor } from "@/components/account/favorites-editor";
import { IdentityBridge } from "@/components/account/identity-bridge";
import { RecentlyViewed } from "@/components/account/recently-viewed";
import { RequireAuth } from "@/components/auth/require-auth";
import { useAuth } from "@/components/auth-provider";
import { ErrorState } from "@/components/ui/error-state";
import { Skeleton } from "@/components/ui/skeleton";
import { api } from "@/lib/api";

export default function AccountPage() {
  return (
    <RequireAuth>
      <AccountView />
    </RequireAuth>
  );
}

function AccountView() {
  const { user, status } = useAuth();
  const {
    data: profile,
    isLoading,
    isError,
    refetch,
  } = useQuery({
    queryKey: ["myProfile"],
    queryFn: api.catalog.myProfile,
    enabled: status === "authenticated",
  });

  return (
    <div className="container-page py-10">
      <header>
        <h1 className="text-3xl font-extrabold">Your account</h1>
        <p className="mt-1 text-ink-soft">
          Your identity, preferences, and what you’ve been browsing — across both databases.
        </p>
      </header>

      {isLoading || !user ? (
        <div className="mt-8 space-y-6">
          <Skeleton className="h-72 w-full rounded-2xl" />
          <Skeleton className="h-56 w-full rounded-2xl" />
        </div>
      ) : isError || !profile ? (
        <ErrorState
          className="mt-8"
          title="Couldn’t load your profile"
          onRetry={() => refetch()}
        />
      ) : (
        <div className="mt-8 space-y-8">
          <IdentityBridge user={user} profile={profile} />
          <FavoritesEditor profile={profile} />
          <RecentlyViewed productIds={profile.recentlyViewedProductIds} />
        </div>
      )}
    </div>
  );
}
