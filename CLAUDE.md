# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

pg_queryj is a Java wrapper for libpg_query, providing PostgreSQL SQL parsing capabilities using Java's Foreign Function & Memory (FFM) API. It supports parsing SQL to JSON parse trees, Protocol Buffer serialization, and deparsing protobuf back to SQL.

## Build Commands

```bash
# Build the project
mvn clean compile

# Run the demo (parses "SELECT 1")
mvn compile exec:java

# Run tests
mvn test

# Regenerate FFM bindings from C headers (requires jextract-25)
.\jextract-25\bin\jextract --output src\main\java --target-package com.hannos.ffm .\libpg_query\pg_query.h
```

## Architecture

### Public API
- **PgParser** (`src/main/java/com/hannos/PgParser.java`) - Main entry point with three methods:
  - `parse(String sql)` → JSON parse tree
  - `parseProtobuf(String sql)` → protobuf binary
  - `deparse(byte[] protobuf)` → reconstructed SQL

### FFM Layer
Files in `com.hannos.ffm` are **auto-generated** by jextract from `libpg_query/pg_query.h`. Do not manually edit these files.

### Native Library Loading
**NativeLoader** extracts platform-specific binaries (pg_query.dll/so) from JAR resources to a temp file at runtime.

### Memory Management
All FFM calls use `Arena.ofConfined()` for automatic memory cleanup. Native results are freed via `pg_query_free_*` functions.

## Key Technical Notes

- Requires Java 25 (FFM API)
- Native libraries bundled in `src/main/resources/` (Windows dll, Linux so)
- Protocol Buffer schema at `protobuf/pg_query.proto` defines full PostgreSQL AST
- ParseException includes cursor position for syntax error location
