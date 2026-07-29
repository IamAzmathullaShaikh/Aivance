# 11. Image Loading with Coil

Date: 2026-07-28

## Status

Accepted

## Context

Efficient image loading and caching are essential for a smooth UI, especially when displaying job logos or user profiles.

## Decision

Use Coil as the primary image loading library.
- **Compose Integration**: Leverage `AsyncImage` for seamless integration with Jetpack Compose.
- **Optimization**: Use Coil's built-in support for caching, downsampling, and hardware bitmaps.
- **Cross-fade**: Enable cross-fade transitions for a more polished feel.

## Consequences

- **Pros**:
    - Lightweight and built on top of Coroutines.
    - Modern API designed specifically for Kotlin and Android.
    - Excellent performance and memory management.
- **Cons**:
    - Yet another dependency to manage.
