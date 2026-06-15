import { hashToIndex } from "@/lib/utils";
import { cn } from "@/lib/utils";

/**
 * The catalog API exposes no image URLs, so each product gets deterministic
 * generative art: a store-hue gradient plus one of several geometric motifs,
 * chosen by hashing the seed. Pure + server-renderable.
 */
export function ProductArt({
  seed,
  accent,
  label,
  className,
}: {
  seed: string;
  accent: string;
  label?: string;
  className?: string;
}) {
  const variant = hashToIndex(seed, 5);
  const rot = hashToIndex(seed + "r", 8) * 45;
  const uid = `pa-${hashToIndex(seed + "uid", 1_000_000)}`;
  const initial = (label ?? seed).trim().charAt(0).toUpperCase() || "N";

  return (
    <div className={cn("relative overflow-hidden", className)} aria-hidden>
      <svg viewBox="0 0 400 400" className="h-full w-full" preserveAspectRatio="xMidYMid slice">
        <defs>
          <linearGradient id={`${uid}-bg`} x1="0" y1="0" x2="1" y2="1">
            <stop offset="0%" stopColor={accent} stopOpacity="0.14" />
            <stop offset="100%" stopColor={accent} stopOpacity="0.04" />
          </linearGradient>
          <radialGradient id={`${uid}-glow`} cx="50%" cy="38%" r="65%">
            <stop offset="0%" stopColor={accent} stopOpacity="0.28" />
            <stop offset="100%" stopColor={accent} stopOpacity="0" />
          </radialGradient>
        </defs>

        <rect width="400" height="400" fill="var(--color-surface, #fff)" />
        <rect width="400" height="400" fill={`url(#${uid}-bg)`} />
        <rect width="400" height="400" fill={`url(#${uid}-glow)`} />

        <g transform={`rotate(${rot} 200 200)`} stroke={accent} fill="none">
          {variant === 0 &&
            [40, 90, 140, 190].map((r) => (
              <circle key={r} cx="200" cy="200" r={r} strokeOpacity={0.22} strokeWidth={1.5} />
            ))}
          {variant === 1 &&
            Array.from({ length: 9 }).map((_, i) => (
              <line
                key={i}
                x1={i * 50}
                y1="0"
                x2={i * 50 - 120}
                y2="400"
                strokeOpacity={0.18}
                strokeWidth={1.5}
              />
            ))}
          {variant === 2 &&
            Array.from({ length: 6 }).map((_, r) =>
              Array.from({ length: 6 }).map((_, c) => (
                <circle
                  key={`${r}-${c}`}
                  cx={40 + c * 64}
                  cy={40 + r * 64}
                  r={4}
                  fill={accent}
                  fillOpacity={0.22}
                  stroke="none"
                />
              )),
            )}
          {variant === 3 &&
            [0, 1, 2, 3].map((i) => (
              <rect
                key={i}
                x={70 + i * 18}
                y={70 + i * 18}
                width={260 - i * 36}
                height={260 - i * 36}
                strokeOpacity={0.2}
                strokeWidth={1.5}
                rx={8}
              />
            ))}
          {variant === 4 && (
            <g>
              {[[120, 110], [290, 150], [250, 300], [110, 280]].map(([x, y], i) => (
                <g key={i}>
                  <line x1="200" y1="200" x2={x} y2={y} strokeOpacity={0.22} strokeWidth={1.5} />
                  <circle cx={x} cy={y} r={7} fill={accent} fillOpacity={0.3} stroke="none" />
                </g>
              ))}
              <circle cx="200" cy="200" r={11} fill={accent} stroke="none" />
            </g>
          )}
        </g>

        <text
          x="200"
          y="200"
          textAnchor="middle"
          dominantBaseline="central"
          fontFamily="var(--font-bricolage), sans-serif"
          fontSize="150"
          fontWeight="700"
          fill={accent}
          fillOpacity="0.16"
        >
          {initial}
        </text>
      </svg>
    </div>
  );
}
