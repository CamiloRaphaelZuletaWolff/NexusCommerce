/**
 * Typed contracts mirroring the Spring Boot DTOs (Phase 2–4 backend).
 * Money is serialized as a JSON number by Jackson; UUIDs/Instants as strings.
 */

export type Role = "CUSTOMER" | "STORE_MANAGER" | "ADMIN";

export type ProductStatus = "ACTIVE" | "DRAFT" | "ARCHIVED";
export type StoreStatus = "ACTIVE" | "INACTIVE" | "SUSPENDED";
export type StoreType =
  | "ELECTRONICS"
  | "FASHION"
  | "FURNITURE"
  | "KITCHENWARE"
  | "HOME_DECOR";
export type OrderStatus = "PENDING" | "PAID" | "CANCELLED" | "FULFILLED";
export type AttributeType = "STRING" | "NUMBER" | "BOOLEAN";

/** Spring Data Page envelope (only the fields the UI consumes). */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  numberOfElements: number;
}

/* ---- Auth & users ---- */
export interface UserResponse {
  id: string;
  email: string;
  fullName: string | null;
  enabled: boolean;
  roles: Role[];
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresInSeconds: number;
  user: UserResponse;
}

export interface LoginRequest {
  email: string;
  password: string;
}
export interface RegisterRequest {
  email: string;
  password: string;
  fullName?: string;
}

export interface CustomerProfileResponse {
  id: string;
  email: string;
  fullName: string | null;
  favoriteCategories: string[];
  favoriteBrands: string[];
  recentlyViewedProductIds: string[];
  createdAt: string;
  updatedAt: string;
}

/** PATCH /catalog/me/profile — a provided list replaces that set; omit to leave unchanged. */
export interface UpdateCustomerProfileRequest {
  favoriteCategories?: string[];
  favoriteBrands?: string[];
}

/* ---- Catalog ---- */
export interface VariantDto {
  sku: string;
  attributes: Record<string, unknown>;
  priceOverride: number | null;
  stock: number | null;
}

export interface ProductResponse {
  id: string;
  storeId: string;
  sku: string;
  name: string;
  slug: string;
  description: string | null;
  price: number;
  currency: string;
  categorySlug: string | null;
  categoryLabels: string[];
  tags: string[];
  brand: string | null;
  variants: VariantDto[];
  attributes: Record<string, unknown>;
  status: ProductStatus;
  ratingAverage: number;
  ratingCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface AttributeDefinitionDto {
  key: string;
  label: string | null;
  type: AttributeType;
  required: boolean;
  unit: string | null;
}

export interface CategoryResponse {
  id: string;
  slug: string;
  name: string;
  parentSlug: string | null;
  attributes: AttributeDefinitionDto[];
  createdAt: string;
}

export interface BrandResponse {
  id: string;
  slug: string;
  name: string;
  description: string | null;
  createdAt: string;
}

export interface ProductCreateRequest {
  storeId: string;
  sku: string;
  name: string;
  description?: string;
  price: number;
  currency?: string;
  categorySlug: string;
  categoryLabels?: string[];
  tags?: string[];
  brand?: string;
  variants?: VariantDto[];
  attributes?: Record<string, unknown>;
  status?: ProductStatus;
}

/* ---- Stores ---- */
export interface StoreResponse {
  id: string;
  ownerUserId: string;
  name: string;
  slug: string;
  description: string | null;
  storeType: StoreType;
  status: StoreStatus;
  createdAt: string;
  updatedAt: string;
}

export interface StoreCreateRequest {
  name: string;
  description?: string;
  storeType: StoreType;
}

/* ---- Cart ---- */
export interface CartItemResponse {
  id: string;
  productId: string;
  storeId: string;
  sku: string;
  name: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface CartResponse {
  id: string;
  status: string;
  items: CartItemResponse[];
  itemCount: number;
  subtotal: number;
}

export interface AddCartItemRequest {
  productId: string;
  quantity: number;
}

/* ---- Checkout & orders ---- */
export interface ShippingAddress {
  name: string;
  line1: string;
  line2?: string;
  city: string;
  region?: string;
  postalCode: string;
  country: string;
}

export interface CardPayment {
  number: string;
  holderName: string;
  expiry: string; // MM/YY
  cvv: string;
}

export interface CheckoutRequest {
  shipping: ShippingAddress;
  payment: CardPayment;
}

export interface PaymentResponse {
  status: string;
  method: string;
  amount: number;
  currency: string;
  cardBrand: string | null;
  cardLast4: string | null;
}

export interface OrderItemResponse {
  id: string;
  productId: string;
  storeId: string;
  sku: string;
  name: string;
  unitPrice: number;
  quantity: number;
  lineTotal: number;
}

export interface OrderResponse {
  id: string;
  orderNumber: string;
  userId: string;
  status: OrderStatus;
  subtotal: number;
  tax: number;
  total: number;
  currency: string;
  shipping: ShippingAddress;
  items: OrderItemResponse[];
  payment: PaymentResponse | null;
  createdAt: string;
}

/* ---- Analytics ---- */
export interface StoreRevenueResponse {
  storeId: string;
  ordersCount: number;
  unitsSold: number;
  revenue: number;
}
export interface TopProductResponse {
  productId: string;
  storeId: string;
  name: string;
  unitsSold: number;
  revenue: number;
}
export interface StoreSalesSummaryResponse {
  ordersCount: number;
  unitsSold: number;
  revenue: number;
}
export interface OrderSummaryResponse {
  orderId: string;
  orderNumber: string;
  userId: string;
  status: OrderStatus;
  total: number;
  itemCount: number;
  createdAt: string;
}

/* ---- Errors ---- */
export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  fieldErrors?: { field: string; message: string }[];
}
