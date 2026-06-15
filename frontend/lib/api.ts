/**
 * The single typed API client. Every browser call goes through `request`, which
 * attaches the in-memory access token, sends the refresh cookie (`credentials:
 * include`), and on a 401 performs ONE silent refresh + retry before failing.
 */
import { getAccessToken, setAccessToken } from "./token-store";
import type {
  AddCartItemRequest,
  ApiErrorBody,
  AuthResponse,
  BrandResponse,
  CartResponse,
  CategoryResponse,
  CheckoutRequest,
  CustomerProfileResponse,
  LoginRequest,
  OrderResponse,
  OrderSummaryResponse,
  Page,
  ProductCreateRequest,
  ProductResponse,
  RegisterRequest,
  StoreCreateRequest,
  StoreResponse,
  StoreRevenueResponse,
  StoreSalesSummaryResponse,
  TopProductResponse,
  UserResponse,
} from "@/types/api";

export const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080/api";

export class ApiError extends Error {
  status: number;
  body?: ApiErrorBody;
  fieldErrors?: Record<string, string>;

  constructor(status: number, message: string, body?: ApiErrorBody) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
    if (body?.fieldErrors) {
      this.fieldErrors = Object.fromEntries(
        body.fieldErrors.map((f) => [f.field, f.message]),
      );
    }
  }
}

type QueryValue = string | number | boolean | undefined | null | (string | number)[];

function buildQuery(params?: Record<string, QueryValue>) {
  if (!params) return "";
  const sp = new URLSearchParams();
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined || value === null || value === "") continue;
    if (Array.isArray(value)) {
      for (const v of value) if (v !== undefined && v !== null) sp.append(key, String(v));
    } else {
      sp.append(key, String(value));
    }
  }
  const s = sp.toString();
  return s ? `?${s}` : "";
}

let refreshing: Promise<boolean> | null = null;

/** Attempt a single refresh, coalescing concurrent callers. */
async function refreshOnce(): Promise<boolean> {
  if (!refreshing) {
    refreshing = (async () => {
      try {
        const res = await fetch(`${API_BASE}/auth/refresh`, {
          method: "POST",
          credentials: "include",
        });
        if (!res.ok) {
          setAccessToken(null);
          return false;
        }
        const data = (await res.json()) as AuthResponse;
        setAccessToken(data.accessToken);
        return true;
      } catch {
        setAccessToken(null);
        return false;
      } finally {
        refreshing = null;
      }
    })();
  }
  return refreshing;
}

interface RequestOptions {
  method?: string;
  body?: unknown;
  query?: Record<string, QueryValue>;
  /** Skip the silent-refresh retry (used by the auth endpoints themselves). */
  noRetry?: boolean;
  signal?: AbortSignal;
}

async function request<T>(path: string, opts: RequestOptions = {}): Promise<T> {
  const { method = "GET", body, query, noRetry, signal } = opts;

  const doFetch = async () => {
    const headers: Record<string, string> = {};
    const token = getAccessToken();
    if (token) headers.Authorization = `Bearer ${token}`;
    if (body !== undefined) headers["Content-Type"] = "application/json";
    return fetch(`${API_BASE}${path}${buildQuery(query)}`, {
      method,
      headers,
      credentials: "include",
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal,
    });
  };

  let res = await doFetch();

  if (res.status === 401 && !noRetry) {
    const refreshed = await refreshOnce();
    if (refreshed) res = await doFetch();
  }

  if (res.status === 204) return undefined as T;

  const text = await res.text();
  const data = text ? JSON.parse(text) : undefined;

  if (!res.ok) {
    const errBody = data as ApiErrorBody | undefined;
    throw new ApiError(res.status, errBody?.message || res.statusText, errBody);
  }
  return data as T;
}

/* ---------------------------------------------------------------- *
 * Endpoint groups
 * ---------------------------------------------------------------- */

export interface ProductQuery {
  storeId?: string;
  q?: string;
  categories?: string[];
  tags?: string[];
  brands?: string[];
  minPrice?: number;
  maxPrice?: number;
  status?: string;
  page?: number;
  size?: number;
  sort?: string;
}

export const api = {
  auth: {
    login: (data: LoginRequest) =>
      request<AuthResponse>("/auth/login", { method: "POST", body: data, noRetry: true }),
    register: (data: RegisterRequest) =>
      request<AuthResponse>("/auth/register", { method: "POST", body: data, noRetry: true }),
    logout: () => request<void>("/auth/logout", { method: "POST", noRetry: true }),
    refresh: () =>
      request<AuthResponse>("/auth/refresh", { method: "POST", noRetry: true }),
    me: () => request<UserResponse>("/auth/me"),
  },

  catalog: {
    products: (query?: ProductQuery) =>
      request<Page<ProductResponse>>("/catalog/products", { query: query as Record<string, QueryValue> }),
    product: (id: string) => request<ProductResponse>(`/catalog/products/${id}`),
    createProduct: (data: ProductCreateRequest) =>
      request<ProductResponse>("/catalog/products", { method: "POST", body: data }),
    categories: () => request<CategoryResponse[]>("/catalog/categories"),
    brands: () => request<BrandResponse[]>("/catalog/brands"),
    myProfile: () => request<CustomerProfileResponse>("/catalog/me/profile"),
  },

  stores: {
    list: (query?: { status?: string; page?: number; size?: number }) =>
      request<Page<StoreResponse>>("/stores", { query }),
    bySlug: (slug: string) => request<StoreResponse>(`/stores/${slug}`),
    create: (data: StoreCreateRequest) =>
      request<StoreResponse>("/stores", { method: "POST", body: data }),
  },

  cart: {
    get: () => request<CartResponse>("/cart"),
    addItem: (data: AddCartItemRequest) =>
      request<CartResponse>("/cart/items", { method: "POST", body: data }),
    updateItem: (itemId: string, quantity: number) =>
      request<CartResponse>(`/cart/items/${itemId}`, { method: "PUT", body: { quantity } }),
    removeItem: (itemId: string) =>
      request<CartResponse>(`/cart/items/${itemId}`, { method: "DELETE" }),
    clear: () => request<CartResponse>("/cart", { method: "DELETE" }),
  },

  checkout: (data: CheckoutRequest) =>
    request<OrderResponse>("/checkout", { method: "POST", body: data }),

  orders: {
    list: () => request<OrderResponse[]>("/orders"),
    byId: (id: string) => request<OrderResponse>(`/orders/${id}`),
  },

  analytics: {
    storeRevenue: () => request<StoreRevenueResponse[]>("/analytics/store-revenue"),
    aboveAverage: () =>
      request<StoreRevenueResponse[]>("/analytics/store-revenue/above-average"),
    topProducts: (limit = 10) =>
      request<TopProductResponse[]>("/analytics/top-products", { query: { limit } }),
    storeSummary: (storeId: string) =>
      request<StoreSalesSummaryResponse>(`/analytics/stores/${storeId}/summary`),
    ordersSummary: (limit = 20) =>
      request<OrderSummaryResponse[]>("/analytics/orders-summary", { query: { limit } }),
  },
};

export { refreshOnce };
