# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a TDD (Test-Driven Development) exercise project for a point management system. The project is built with Spring Boot 3.2.0 and Java 17, focusing on implementing user point charge/use functionality following TDD principles.

## Build Commands

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run a specific test class
./gradlew test --tests "ClassName"

# Run a specific test method
./gradlew test --tests "ClassName.methodName"

# Run the application
./gradlew bootRun

# Clean build
./gradlew clean build
```

## Architecture

### Data Layer Constraints

**IMPORTANT**: The Table classes (`UserPointTable` and `PointHistoryTable`) are provided infrastructure that MUST NOT be modified:
- `UserPointTable` - Manages user point data with in-memory HashMap storage
- `PointHistoryTable` - Manages point transaction history with in-memory List storage
- Both classes simulate database latency with throttling (200-300ms random delay)
- Only use the public APIs provided by these classes

### Domain Model

The core domain is organized around point management:
- `UserPoint` - Record containing user ID, current point balance, and last update timestamp
- `PointHistory` - Record of point transactions (charge/use) with transaction type
- `TransactionType` - Enum with two values: CHARGE, USE

### Controller Layer

`PointController` at `/point` endpoint contains four TODO methods to implement:
1. `GET /{id}` - Query user point balance
2. `GET /{id}/histories` - Query user point transaction history
3. `PATCH /{id}/charge` - Charge points to user account
4. `PATCH /{id}/use` - Use/deduct points from user account

### Implementation Requirements

When implementing the point management features:
1. Create a Service layer to encapsulate business logic (not provided in skeleton)
2. Service should inject and use `UserPointTable` and `PointHistoryTable` via Spring DI
3. Business logic should include validation (e.g., sufficient balance for use, positive amounts)
4. Update both the point balance AND record the transaction history for charge/use operations
5. Follow TDD: write tests first, then implement to make tests pass

### Testing Considerations

- Tests should account for the random throttling delays in Table classes
- JUnit 5 is configured with `useJUnitPlatform()`
- Test failures are ignored in gradle config (`ignoreFailures = true`) but should still be fixed
- Jacoco code coverage plugin is enabled

### Error Handling

- `ApiControllerAdvice` provides global exception handling
- Currently catches all exceptions and returns 500 with generic error message
- Consider extending this with custom exceptions for business validation failures
