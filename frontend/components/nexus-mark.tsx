"use client";

import { motion, useReducedMotion } from "framer-motion";

const NODES = [
  { x: 200, y: 50, hue: "#1e7fff", name: "Electronics" },
  { x: 342.6, y: 153.6, hue: "#e14b86", name: "Fashion" },
  { x: 288.2, y: 321.4, hue: "#b07636", name: "Furniture" },
  { x: 111.8, y: 321.4, hue: "#2fa56b", name: "Kitchenware" },
  { x: 57.4, y: 153.6, hue: "#c75a3f", name: "Home Decor" },
];
const CENTER = { x: 200, y: 200 };

/**
 * The signature: five store-worlds (each its own hue) wired to one central
 * nexus. Lines draw in, nodes settle, then breathe — "many stores, one system".
 */
export function NexusMark({ className }: { className?: string }) {
  const reduce = useReducedMotion();

  return (
    <svg
      viewBox="0 0 400 400"
      className={className}
      role="img"
      aria-label="Five store-worlds converging on one nexus"
    >
      {/* connective lines */}
      {NODES.map((n, i) => (
        <motion.line
          key={`l-${i}`}
          x1={CENTER.x}
          y1={CENTER.y}
          x2={n.x}
          y2={n.y}
          stroke={n.hue}
          strokeWidth={2}
          strokeLinecap="round"
          initial={reduce ? { pathLength: 1, opacity: 0.5 } : { pathLength: 0, opacity: 0 }}
          animate={{ pathLength: 1, opacity: 0.55 }}
          transition={{ duration: 0.9, delay: 0.15 + i * 0.12, ease: "easeOut" }}
        />
      ))}

      {/* outer store nodes */}
      {NODES.map((n, i) => (
        <motion.g
          key={`n-${i}`}
          initial={reduce ? { scale: 1, opacity: 1 } : { scale: 0, opacity: 0 }}
          animate={{ scale: 1, opacity: 1 }}
          transition={{
            type: "spring",
            stiffness: 320,
            damping: 18,
            delay: 0.7 + i * 0.12,
          }}
          style={{ transformOrigin: `${n.x}px ${n.y}px` }}
        >
          <circle cx={n.x} cy={n.y} r={20} fill={n.hue} fillOpacity={0.12} />
          <circle cx={n.x} cy={n.y} r={9} fill={n.hue} />
        </motion.g>
      ))}

      {/* central nexus */}
      <motion.g
        initial={reduce ? { scale: 1 } : { scale: 0 }}
        animate={{ scale: 1 }}
        transition={{ type: "spring", stiffness: 260, damping: 16, delay: 0.4 }}
        style={{ transformOrigin: "200px 200px" }}
      >
        <motion.circle
          cx={CENTER.x}
          cy={CENTER.y}
          r={34}
          fill="#3f31d6"
          fillOpacity={0.1}
          animate={reduce ? {} : { scale: [1, 1.12, 1] }}
          transition={{ duration: 3.2, repeat: Infinity, ease: "easeInOut" }}
          style={{ transformOrigin: "200px 200px" }}
        />
        <circle cx={CENTER.x} cy={CENTER.y} r={17} fill="#3f31d6" />
        <circle cx={CENTER.x} cy={CENTER.y} r={6} fill="#fff" fillOpacity={0.9} />
      </motion.g>
    </svg>
  );
}

/** Compact static mark for the navbar / footer. */
export function NexusLogo({ className }: { className?: string }) {
  return (
    <svg viewBox="0 0 32 32" className={className} aria-hidden>
      {[
        [16, 5],
        [27, 13],
        [23, 26],
        [9, 26],
        [5, 13],
      ].map(([x, y], i) => (
        <line key={i} x1={16} y1={16} x2={x} y2={y} stroke="currentColor" strokeWidth={1.6} strokeOpacity={0.45} />
      ))}
      {[
        [16, 5],
        [27, 13],
        [23, 26],
        [9, 26],
        [5, 13],
      ].map(([x, y], i) => (
        <circle key={i} cx={x} cy={y} r={2.4} fill="currentColor" fillOpacity={0.7} />
      ))}
      <circle cx={16} cy={16} r={4.5} fill="currentColor" />
    </svg>
  );
}
