import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { NexusMark } from "@/components/nexus-mark";
import { Button } from "@/components/ui/button";

export function Hero() {
  return (
    <section className="relative overflow-hidden">
      <div className="nexus-grid absolute inset-0" aria-hidden />
      <div className="container-page relative grid items-center gap-12 py-16 md:grid-cols-[1.05fr_0.95fr] md:py-24">
        <div className="rise">
          <p className="eyebrow">Multi-store marketplace</p>
          <h1 className="mt-4 text-5xl font-extrabold leading-[0.98] sm:text-6xl lg:text-7xl">
            Five store-worlds.
            <br />
            <span className="text-nexus">One nexus.</span>
          </h1>
          <p className="mt-6 max-w-md text-lg leading-relaxed text-ink-soft">
            Electronics, Fashion, Furniture, Kitchenware, and Home Decor — each with its
            own catalog and identity, wired into a single system you shop from one cart.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Button asChild variant="accent" size="lg">
              <Link href="/catalog">
                Browse the catalog
                <ArrowRight className="size-4" />
              </Link>
            </Button>
            <Button asChild variant="outline" size="lg">
              <Link href="/stores">Explore stores</Link>
            </Button>
          </div>
          <p className="eyebrow mt-8">
            Hybrid Postgres + Mongo · UUID-bridged catalog · one checkout
          </p>
        </div>

        <div className="relative mx-auto w-full max-w-md">
          <div className="rounded-3xl border border-line bg-surface/70 p-6 shadow-lg backdrop-blur-sm sm:p-10">
            <NexusMark className="h-auto w-full" />
          </div>
        </div>
      </div>
    </section>
  );
}
