# Diagrams

Visual documentation for NexusCommerce. Diagrams are committed as text-first
**Mermaid** (renders on GitHub and in VS Code), so they version with the code.

| Diagram | File | Covers |
|---|---|---|
| PostgreSQL ERD (3NF) | [erd.md](erd.md) | relational model, junction table, snapshot columns, UUID refs to Mongo |
| MongoDB catalog schema | [mongo-schema.md](mongo-schema.md) | document shapes, dynamic attributes, the `$gt`/`$lt`/`$and`/`$or`/`$in` queries |
| Data-flow diagram | [data-flow.md](data-flow.md) | request flow, two data lanes, the UUID bridge (assignment deliverable) |
| Sequence diagrams | [sequences.md](sequences.md) | login + silent refresh, ACID checkout, cross-DB customer sync |
| Screenshots | [screenshots/README.md](screenshots/README.md) | storefront + dashboard captures (manual capture guide) |

## Viewing

GitHub renders the Mermaid blocks inline. In VS Code, use a Mermaid preview
extension, or paste a block into <https://mermaid.live>. An exported PNG/SVG can be
dropped next to each `.md` if a static image is needed for a report.
