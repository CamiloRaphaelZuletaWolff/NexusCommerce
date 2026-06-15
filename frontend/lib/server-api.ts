/**
 * Server-only fetch helpers for PUBLIC catalog/store data, used by Server
 * Components. No credentials (these endpoints are public); cached with a short
 * revalidate window. Authenticated data (cart, orders, me) is fetched on the
 * client via `lib/api.ts` because the access token lives in browser memory.
 */
import "server-only";
import type {
  BrandResponse,
  CategoryResponse,
  Page,
  ProductResponse,
  StoreResponse,
} from "@/types/api";

const BASE =
  process.env.API_INTERNAL_BASE_URL ??
  process.env.NEXT_PUBLIC_API_BASE_URL ??
  "http://localhost:8080/api";

const REVALIDATE = 60;

type Query = Record<string, string | number | string[] | undefined>;

function qs(query?: Query) {
  if (!query) return "";
  const sp = new URLSearchParams();
  for (const [k, v] of Object.entries(query)) {
    if (v === undefined || v === "") continue;
    if (Array.isArray(v)) v.forEach((x) => sp.append(k, x));
    else sp.append(k, String(v));
  }
  const s = sp.toString();
  return s ? `?${s}` : "";
}

async function get<T>(path: string, query?: Query, fallback?: T): Promise<T> {
  try {
    const res = await fetch(`${BASE}${path}${qs(query)}`, {
      next: { revalidate: REVALIDATE },
    });
    if (!res.ok) {
      if (fallback !== undefined) return fallback;
      throw new Error(`Upstream ${res.status} for ${path}`);
    }
    return (await res.json()) as T;
  } catch (err) {
    if (fallback !== undefined) return fallback;
    throw err;
  }
}

const emptyPage = <T,>(): Page<T> => ({
  content: [],
  totalElements: 0,
  totalPages: 0,
  number: 0,
  size: 0,
  first: true,
  last: true,
  numberOfElements: 0,
});

export const serverApi = {
  products: (query?: Query) =>
    get<Page<ProductResponse>>("/catalog/products", query, emptyPage<ProductResponse>()),
  product: (id: string) => get<ProductResponse>(`/catalog/products/${id}`),
  categories: () => get<CategoryResponse[]>("/catalog/categories", undefined, []),
  brands: () => get<BrandResponse[]>("/catalog/brands", undefined, []),
  stores: (query?: Query) =>
    get<Page<StoreResponse>>("/stores", query, emptyPage<StoreResponse>()),
  store: (slug: string) => get<StoreResponse>(`/stores/${slug}`),
};
