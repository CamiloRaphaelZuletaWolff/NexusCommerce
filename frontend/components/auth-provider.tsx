"use client";

import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useQueryClient } from "@tanstack/react-query";
import { api, refreshOnce } from "@/lib/api";
import { setAccessToken } from "@/lib/token-store";
import type { LoginRequest, RegisterRequest, Role, UserResponse } from "@/types/api";

type Status = "loading" | "authenticated" | "unauthenticated";

interface AuthContextValue {
  user: UserResponse | null;
  status: Status;
  login: (data: LoginRequest) => Promise<void>;
  register: (data: RegisterRequest) => Promise<void>;
  logout: () => Promise<void>;
  hasRole: (...roles: Role[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [status, setStatus] = useState<Status>("loading");
  const queryClient = useQueryClient();

  // Bootstrap from the HttpOnly refresh cookie on first load.
  useEffect(() => {
    let active = true;
    (async () => {
      const ok = await refreshOnce();
      if (!active) return;
      if (!ok) {
        setStatus("unauthenticated");
        return;
      }
      try {
        const me = await api.auth.me();
        if (!active) return;
        setUser(me);
        setStatus("authenticated");
      } catch {
        if (active) setStatus("unauthenticated");
      }
    })();
    return () => {
      active = false;
    };
  }, []);

  const login = useCallback(
    async (data: LoginRequest) => {
      const res = await api.auth.login(data);
      setAccessToken(res.accessToken);
      setUser(res.user);
      setStatus("authenticated");
      await queryClient.invalidateQueries();
    },
    [queryClient],
  );

  const register = useCallback(
    async (data: RegisterRequest) => {
      const res = await api.auth.register(data);
      setAccessToken(res.accessToken);
      setUser(res.user);
      setStatus("authenticated");
      await queryClient.invalidateQueries();
    },
    [queryClient],
  );

  const logout = useCallback(async () => {
    try {
      await api.auth.logout();
    } finally {
      setAccessToken(null);
      setUser(null);
      setStatus("unauthenticated");
      queryClient.clear();
    }
  }, [queryClient]);

  const hasRole = useCallback(
    (...roles: Role[]) => !!user && roles.some((r) => user.roles.includes(r)),
    [user],
  );

  const value = useMemo(
    () => ({ user, status, login, register, logout, hasRole }),
    [user, status, login, register, logout, hasRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within <AuthProvider>");
  return ctx;
}
