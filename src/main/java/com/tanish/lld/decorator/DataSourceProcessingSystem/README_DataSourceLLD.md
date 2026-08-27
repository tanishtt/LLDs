# Data Source — Simple Interview Question

## Problem Statement
Design a simple in-memory Data Source system demonstrating the Decorator pattern. Start with a basic FileDataSource that reads and writes string content, and add decorators to provide cross-cutting behaviors: compression, encryption, and logging.

Keep it focused: no networking, persistence beyond an in-memory map, and no external systems. The goal is to show understanding of wrapping behavior and composition.

## Requirements
- Define a DataSource interface with readData() and writeData(String).
- Implement FileDataSource that stores content in a simple in-memory map (simulates disk).
- Implement decorators:
  - CompressionDataSource: compresses data before writing and decompresses after reading.
  - EncryptionDataSource: encodes data on write and decodes on read.
  - LoggingDataSource: logs read/write operations.
- Provide a builder or manual composition to chain decorators and demonstrate nesting order.
- Provide a driver (main) that builds pipelines, writes data, and reads it back showing transformed behavior and logs.

## Example Usage (matches code in this package)
Manual chaining:
DataSource pipeline = new LoggingDataSource(new EncryptionDataSource(new CompressionDataSource(new FileDataSource("abc.txt"))));

Or using builder:
DataSource pipeline = new DataSourcePipelineBuilder("audit.log")
    .withEncryption()
    .withCompression()
    .withLogging()
    .build();

pipeline.writeData("example payload");
String result = pipeline.readData();

## Expected Behavior
- writeData applies decorators from outermost to innermost (e.g., Logging -> Encryption -> Compression -> File).
- readData reverses effects to return original content.
- Compression and Encryption implementations may be simple/toy but should show transformation and reversal.
- Logging prints timestamps and payload lengths for reads/writes.

## Test Ideas
- Writing and then reading returns the original string when pipeline includes matching compression/encryption decorators.
- Order matters: composing encryption before compression should still allow correct read if reversed appropriately.
- Logging decorator does not alter payload content, only logs activity.
- Builder produces same pipeline as manual nesting.

## Optional Extensions
- Add checksum/verification decorator.
- Simulate failures and add a Retry decorator.
- Make compression non-lossy (e.g., use a reversible placeholder algorithm) for correctness.

This simplified question matches the DataSourceProcessingSystemDriver implementation in this package.