# Data Source — Low-Level Design (LLD) Interview Question

## Problem Statement (Interview Style)
You are designing a "Data Source" service responsible for ingesting, normalizing, and serving data from multiple external providers to downstream systems. Each provider exposes data in different formats, update frequencies, and quality. The service must provide a unified API for consumers to query the latest data, historical snapshots, and subscription-style change notifications.

Design a robust, extensible, and testable low-level architecture for this service. Focus on component responsibilities, data models, ingestion pipelines, failure-handling, scaling, and operational concerns.

---

## Goals and Scope
- Ingest data from 3rd-party providers (files, REST APIs, message queues).
- Normalize disparate schemas into a canonical internal model.
- Persist raw and normalized data; support point-in-time queries and historical snapshots.
- Provide query API: current state, history, and filtered queries.
- Publish change events (push or stream) to subscribers when data changes.
- Ensure high availability, near-real-time updates, and data correctness.

Out of scope: downstream analytics, complex OLAP queries, and UI design.

---

## Functional Requirements
1. Connect to multiple provider types: SFTP/CSV, HTTP/JSON, and Kafka/Avro.
2. Periodic polling and near-real-time streaming ingestion support.
3. Schema mapping/configuration per provider with transformation hooks.
4. Deduplication, validation, and enrichment of records.
5. Store raw payloads and normalized records with timestamps and source metadata.
6. Serve current record by key and retrieve historical versions within a time range.
7. Emit change events whenever a record is inserted, updated, or deleted.
8. Expose operational endpoints/metrics for monitoring pipeline health.

---

## Non-functional Requirements
- Scalability: handle spikes and growth in providers/data volume.
- Reliability: at-least-once ingestion with idempotent processing.
- Consistency: eventual consistency acceptable; strong consistency for point reads is optional.
- Performance: typical end-to-end latency under 5s for streaming sources; batch sources may be longer.
- Extensibility: add new provider types and transformations without major changes.
- Observability: logs, metrics, traces, error tracking.

---

## Key Components (LLD)
- Source Connector Layer: pluggable connectors for SFTP, HTTP, and Kafka; responsible for fetching raw payloads and metadata.
- Ingestion Queue: durable, partitioned queue (e.g., Kafka, Pub/Sub) decoupling fetching from processing.
- Normalizer/Transformer: applies provider-specific mappings and validation, emits normalized records.
- Deduplication & Idempotency Service: ensures each logical record is processed once (idempotent keys, vector clocks, or tombstones).
- Storage:
  - Raw Store: append-only blob store for raw payloads (e.g., S3/GCS).
  - Normalized Store: primary data store for current state (e.g., transactional DB or key-value store).
  - History Store: append-only change-log / time-series store for historical queries (could be the same as event log).
- Change Publisher: publishes normalized change events to subscribers (Kafka topics, webhooks).
- API Layer: read-only APIs for current and historical data; admin endpoints for ingestion control and schema management.
- Schema & Config Service: stores mapping rules, transformation scripts, and connector configs.
- Monitoring & Admin UI: dashboards, health checks, job status, and replay controls.

---

## Data Model (Canonical)
- Record {
  id: string (logical key),
  sourceId: string,
  payload: JSON (normalized fields),
  rawRef: string (link to raw blob),
  version: long (monotonic or timestamp),
  createdAt: timestamp,
  updatedAt: timestamp,
  metadata: { provider, rawTimestamp, checksum }
}

History is stored as append-only events: { recordId, version, changeType, payload, timestamp, sourceId }

---

## APIs (Examples)
- GET /v1/records/{id} -> current record
- GET /v1/records/{id}/history?from=...&to=... -> historical versions
- POST /v1/query -> filter-based query with pagination
- POST /v1/admin/replay -> request replay of source data for a provider/time-range
- Webhook or WebSocket subscription endpoints for real-time change notifications

---

## Failure Modes & Handling
- Connector failures: circuit-breaker, backoff retries, alerting. Persist last successful cursor for resume.
- Partial transform failures: route bad records to a dead-letter queue with context for manual inspection.
- Duplicate deliveries: dedupe using logical keys + nonce or idempotency tokens.
- Storage failures: fall back to retry, switch to read-only degraded mode, and alert.
- Schema changes: versioned schemas with migration strategy and backward-compatible transforms.

---

## Scalability & Ops
- Partition ingestion by provider and key-hash; scale consumers horizontally.
- Use autoscaling for processing consumers based on backlog and lag metrics.
- Compact history store with TTL or snapshots for older data to reduce storage costs.
- Provide replay capability: rehydrate normalized store from raw blobs or from event log.
- SLOs: availability 99.9% for read API; processing latency SLOs per source type.

---

## Trade-offs & Design Considerations
- At-least-once vs exactly-once: choose at-least-once with idempotency for simplicity and robustness.
- Event log as source of truth vs normalized DB: event log enables replay and auditing but requires materialized views for fast reads.
- Strong consistency vs low latency: prioritize eventual consistency for streaming sources; offer transactional reads for critical keys if needed.

---

## Candidate Discussion Prompts
- How would you design schema evolution and backward compatibility for transformations?
- Describe your approach to deduplication and ensuring idempotent processing.
- How to guarantee low-latency updates for high-frequency providers?
- What monitoring and alerting would you implement to detect data correctness issues?
- How to implement a secure replay mechanism without affecting live traffic?

---

## Extension / Challenge Tasks
- Add multi-tenant support with tenant-specific schema mappings.
- Implement rate-limited webhooks with retry and backoff for unreliable subscribers.
- Provide per-record lineage tracing linking normalized records to raw payload and transformation steps.
- Implement ACID snapshot reads for a subset of keys using lightweight locking or MVCC.

---

## Acceptance Criteria (Interview)
- Clear component decomposition and justification for choices.
- Data model that supports current and historical queries and provenance.
- Solid handling of failures, deduplication, and schema evolution.
- Reasonable scalability plan and operational controls.
- Well-explained trade-offs and follow-up ideas.

---

Prepared for use as a prompt/README for interviewing or design practice.
