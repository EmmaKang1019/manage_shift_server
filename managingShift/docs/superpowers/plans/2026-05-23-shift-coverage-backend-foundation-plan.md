# Shift Coverage Backend Foundation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build an API-first shift coverage MVP backend slice in the current `managingShift` Spring Boot project, usable by the first React/Vite PWA client and a future React Native mobile app. Expose the first stable API contract early so frontend development can proceed in parallel instead of waiting for the full backend domain implementation.

**Architecture:** The current project is a Spring Boot 4.0.6 application under `com.example.shift` with no entity model yet. Implement a common Spring Boot layered backend with `controller`, `dto`, `entity`, `repository`, `service`, and `config` packages. JPA database-mapped classes and their enums live in `entity`. API request/response objects live in `dto` so the backend does not expose persistence entities directly. Business rules live in services. The frontend will live in a separate React/Vite PWA project first, but this backend must expose stable `/api/v1/**` JSON contracts that a future React Native client can reuse.

**Tech Stack:** Java 17, Spring Boot 4.0.6, Spring Web MVC, Spring Data JPA, Spring Security, MySQL for local/dev/frontend integration and production, H2 only for fast JPA slice tests, JUnit/Spring Boot Test.

---

## Current Directory Analysis

Project root:

```text
C:\Users\MJ\Documents\workSpaces\manage_shift_server\managingShift
```

Current important files:

```text
pom.xml
mvnw
mvnw.cmd
.mvn/wrapper/maven-wrapper.properties
src/main/java/com/example/shift/ShiftApplication.java
src/main/java/com/example/shift/config/SecurityConfig.java
src/test/java/com/example/shift/ShiftApplicationTests.java
docs/superpowers/specs/2026-05-22-shift-coverage-mvp-design.md
docs/superpowers/specs/2026-05-22-shift-coverage-mvp-design.ko.md
docs/superpowers/plans/2026-05-23-shift-coverage-backend-foundation-plan.ko.md
```

Observed state:

- As of 2026-05-29, `.\mvnw.cmd -q test` fails during test compilation because `EmployeeRepository`, `PositionRepository`, and `TeamRepository` do not exist yet.
- Entity enums and early `Employee`, `Team`, and `Position` drafts exist, but repository/service/controller/dto implementation is not complete.
- The current `Employee` draft uses one `Position`; this plan requires `Employee.positions` as `Set<Position>` so one staff member can cover multiple duties.
- `mvn` is not installed locally.
- The temporary `src/main/java/com/example/shift/test/LoginSecurityTest.java` MVC controller has been removed.

## Scope Check

The product spec covers frontend screens, notifications, payroll/overtime, tenant/multi-location expansion, and future mobile clients. This plan implements the first backend foundation only.

Included:

- Maven wrapper baseline repair.
- Removal of the temporary `src/main/java/.../test` controller.
- Employee, Team, Position.
- Multiple team membership for employees.
- Shared-team substitute eligibility.
- Fixed weekly schedule pattern.
- Actual dated Shift model.
- Coverage request with substitute.
- Absence request without substitute.
- Planned leave request.
- Approved leave converts assigned shifts into open shifts.
- Open shift application and manager direct assignment.
- Thin `/api/v1/**` REST API.
- Early `/api/v1/**` controller and DTO skeletons so the separate frontend can start before every backend workflow is fully implemented.
- CORS for the separate React/Vite PWA frontend.
- Chrome mobile viewport development flow: `http://localhost:5173` frontend calling `http://localhost:8080/api/v1/**`.
- Optional real phone LAN flow: `http://PC_IP:5173` frontend calling `http://PC_IP:8080/api/v1/**`.
- MySQL-backed local/dev profile for API and frontend integration work.
- H2-backed test profile for fast JPA slice tests only.
- Mobile-client-friendly response DTOs. Desktop-only large dashboard responses are out of scope.
- Stable API contracts for a future React Native client.

Excluded:

- Frontend screen implementation.
- React Native app implementation.
- App Store or Play Store submission.
- Native-specific backend extensions such as push device tokens, biometric login, and offline sync.
- Production authentication and invitation flow.
- Tenant/company/location model.
- Payroll/overtime calculation.
- Notification delivery.
- Published schedule period implementation.

## Database Strategy

Use MySQL as the source of truth for local API development, frontend integration, staging-like checks, and production. Use H2 only as a fast test helper for narrow JPA slice tests.

Profiles:

- `local/dev`: MySQL. The React/Vite PWA should call APIs backed by this profile during integration.
- `test`: H2 is acceptable for fast `@DataJpaTest` coverage, preferably with MySQL compatibility mode.
- pre-release integration: MySQL is required. Do not treat H2-only green tests as production readiness.
- `prod`: MySQL, with development CORS origins and seed/test data excluded.

Reasoning:

This backend uses date ranges, `LocalDate`, `LocalTime`, enums, many-to-many join tables, constraints, and indexes. H2 can pass mappings or SQL that later fail on MySQL because of DDL differences, reserved words, enum/string handling, date/time behavior, or constraint/index differences. Set up the MySQL dev profile early in June so these issues appear while backend and frontend work are still moving together.

## Server Environment Strategy

Use a separate development server from the production server. The project needs frequent real-server checks while the frontend and backend are developed in parallel, so the development server should be available as soon as the `/api/v1/**` skeleton is useful.

Recommended environments:

```text
local
- Developer PC.
- Fast code changes and unit/slice tests.
- Uses local MySQL dev or H2 test profile.

development server
- First deployment target starting by 2026-06-05.
- Used by the frontend to call real backend URLs during development.
- Uses a separate MySQL development database.
- Can deploy mock/minimal API skeletons before all domain logic is complete.
- Data may be reset at any time.

production server
- Target for 2026-07-01 launch.
- Real user and operation data.
- Uses production MySQL database.
- Receives only verified release candidates.
```

Comparison:

```text
Separate development and production servers
- Pros: safest option, protects production data, supports deployment rehearsal, catches CORS/HTTPS/env issues early.
- Cons: extra cost and setup.
- Recommendation: best option for the 2026-07-01 launch target.

Single server only
- Pros: cheapest and simplest.
- Cons: development deployments can break production, test data can mix with real data, rollback pressure is higher.
- Recommendation: risky for this timeline.

Logical separation on one VPS
- Example: separate dev/prod app processes, dev/prod databases, dev/prod env vars, and dev/prod domains.
- Pros: lower cost while keeping some safety boundaries.
- Cons: server outage and resource contention still affect both environments.
- Recommendation: acceptable minimum if two physical/cloud servers are too expensive.
```

Default recommendation: use physically separate development and production servers. If cost or administration overhead is a blocker, use one VPS only if the dev/prod app processes, databases, environment variables, CORS origins, and domains are clearly separated.

## Parallel Delivery Schedule

The production target date is 2026-07-01. Do not wait for the full backend to be complete before starting frontend work. Freeze the first useful API contract early, expose controller/dto skeletons, and replace mock/minimal internals with real service logic as the backend matures.

```text
2026-05-29 to 2026-06-02
- Repair current test compilation failure.
- Finish Employee/Team/Position model and repositories.
- Add MySQL dev profile.
- Define screen-by-screen API list and DTO contract.

2026-06-03 to 2026-06-05
- Implement /api/v1 controller + dto skeletons.
- Return mock or minimal responses where domain logic is not ready.
- Deploy the first backend build to the development server.
- Verify CORS, MySQL connection, and basic API smoke tests on the development server.
- Start frontend API client, routing, and base screens.

2026-06-08 to 2026-06-12
- Implement Schedule, Employee, and eligible substitute workflows.
- Connect Home and Schedule frontend screens to real APIs.
- Deploy to the development server at least once per day and verify frontend integration.

2026-06-15 to 2026-06-19
- Implement coverage, absence, planned leave, and manager queue workflows.
- Connect My Requests and Manager Queue frontend screens.
- Verify request creation and approval flows end-to-end on the development server.

2026-06-22 to 2026-06-24
- Implement open shift application, assignment, approval, and decline workflows.
- Complete MVP feature behavior.
- Treat the development server as the release-candidate verification environment.

2026-06-25 to 2026-06-26
- Harden API error responses, CORS, and tests.
- Verify loading, empty, and error states through frontend integration.
- Rehearse the production deployment process.

2026-06-29 to 2026-06-30
- Run MySQL-backed deployment rehearsal and environment/profile checks.
- Perform mobile browser smoke tests.

2026-07-01
- Production server launch.
```

The key milestone is 2026-06-05: the frontend must be able to call stable `/api/v1/**` skeleton endpoints by then, even if some endpoints still return minimal data.

## File Structure

Create main packages:

```text
src/main/java/com/example/shift/entity
src/main/java/com/example/shift/repository
src/main/java/com/example/shift/service
src/main/java/com/example/shift/controller
src/main/java/com/example/shift/dto
```

Create tests under the test source root while mirroring the main package structure:

```text
src/test/java
  com/example/shift/service
  com/example/shift/controller
```

Tests belong under `src/test/java`. Do not create a `test` package under `src/main/java`.

Responsibilities:

- `entity/*`: JPA entities and enum values that are stored by those entities. No API DTOs.
- `repository/*`: Spring Data JPA repositories and query methods.
- `service/*`: transaction boundaries and business rules.
- `controller/*Controller.java`: `/api/v1/**` endpoints and service calls only.
- `dto/*`: API request/response records shared by web and future mobile clients.
- `config/SecurityConfig.java`: existing login settings plus temporary MVP API access.
- `config/CorsProperties.java`: configurable frontend origins.
- `config/CorsConfig.java`: `/api/**` CORS and preflight handling.
- `src/main/resources/application-dev.yml`: MySQL-backed development datasource plus development CORS origins.
- `src/main/resources/application-test.yml`: H2-backed fast test datasource.

## Project Vocabulary

Use these enum names consistently:

```java
public enum EmployeeRole { EMPLOYEE, MANAGER, OWNER }
public enum ShiftStatus { SCHEDULED, OPEN, CANCELLED, COVERED, NEEDS_COVERAGE }
public enum ShiftCreationReason { PATTERN, ABSENCE_REQUEST, LEAVE_REQUEST, EXTRA_STAFFING, NEW_POSITION_COVERAGE, MANUAL }
public enum RequestStatus { PENDING, APPROVED, DECLINED, CANCELLED }
public enum CoverageRequestType { SUBSTITUTE_REQUESTED, ABSENCE_WITHOUT_SUBSTITUTE }
public enum SchedulePatternType { FIXED_WEEKLY }
```

`SchedulePatternType.FIXED_WEEKLY` represents the fixed recurring weekly template used by restaurants with stable schedules, such as Monday/Tuesday full shifts, Wednesday evening, Thursday/Saturday off, and Friday/Sunday full shifts.

Future manager-created schedules for 1-week, 2-week, 3-week, 4-week, or custom periods should be modeled with a future `PublishedSchedulePeriod`, not by adding many `SchedulePatternType` values. A fixed weekly pattern can generate draft shifts for the selected period, and managers can edit those shifts before publishing.

Expand `SchedulePatternType` only when the repeating rule itself changes, such as adding `ROTATING_MULTI_WEEK` for A/B week or other multi-week cycles.

## Task 1: Verify Maven Wrapper Baseline And Remove Temporary Controller

**Files:**
- Delete: `src/main/java/com/example/shift/test/LoginSecurityTest.java`
- Verify: `.mvn/wrapper/maven-wrapper.properties`

- [x] **Step 1: Verify wrapper baseline**

Run:

```powershell
.\mvnw.cmd -q test
```

Expected: PASS with existing `ShiftApplicationTests.contextLoads`.

- [ ] **Step 2: Patch null target handling in `mvnw.cmd` only if the wrapper fails**

Find:

```powershell
$MAVEN_WRAPPER_DISTS = $null
if ((Get-Item $MAVEN_M2_PATH).Target[0] -eq $null) {
  $MAVEN_WRAPPER_DISTS = "$MAVEN_M2_PATH/wrapper/dists"
} else {
  $MAVEN_WRAPPER_DISTS = (Get-Item $MAVEN_M2_PATH).Target[0] + "/wrapper/dists"
}
```

Replace with:

```powershell
$MAVEN_WRAPPER_DISTS = $null
$MAVEN_M2_ITEM = Get-Item $MAVEN_M2_PATH
if ($null -eq $MAVEN_M2_ITEM.Target -or $MAVEN_M2_ITEM.Target.Count -eq 0 -or $null -eq $MAVEN_M2_ITEM.Target[0]) {
  $MAVEN_WRAPPER_DISTS = "$MAVEN_M2_PATH/wrapper/dists"
} else {
  $MAVEN_WRAPPER_DISTS = $MAVEN_M2_ITEM.Target[0] + "/wrapper/dists"
}
```

- [x] **Step 3: Verify wrapper properties**

Run:

```powershell
Get-Content .\.mvn\wrapper\maven-wrapper.properties
```

Expected:

```properties
wrapperVersion=3.3.4
distributionType=only-script
distributionUrl=https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/3.9.14/apache-maven-3.9.14-bin.zip
```

- [x] **Step 4: Run baseline test**

Run:

```powershell
.\mvnw.cmd -q test
```

Expected: PASS with existing `ShiftApplicationTests.contextLoads`.

- [x] **Step 5: Remove the temporary controller**

Delete:

```text
src/main/java/com/example/shift/test/LoginSecurityTest.java
```

This file is an MVC controller, not a JUnit test. It should not be moved into `src/test/java`.

- [x] **Step 6: Confirm no `test` package remains under main sources**

Run:

```powershell
Get-ChildItem -Recurse src/main/java -File | Where-Object { $_.FullName -match "\\test\\" }
```

Expected: no output.

- [x] **Step 7: Run baseline test again**

Run:

```powershell
.\mvnw.cmd -q test
```

Expected: PASS.

- [ ] **Step 8: Commit**

```powershell
git add src/main/java/com/example/shift/test/LoginSecurityTest.java
git commit -m "build: remove temporary controller"
```

## Task 2: Add Entity Enums

**Files:**
- Create: `src/main/java/com/example/shift/entity/EmployeeRole.java`
- Create: `src/main/java/com/example/shift/entity/ShiftStatus.java`
- Create: `src/main/java/com/example/shift/entity/ShiftCreationReason.java`
- Create: `src/main/java/com/example/shift/entity/RequestStatus.java`
- Create: `src/main/java/com/example/shift/entity/CoverageRequestType.java`
- Create: `src/main/java/com/example/shift/entity/SchedulePatternType.java`

- [ ] **Step 1: Create enum files**

Create each enum exactly as listed in the Project Vocabulary section.

- [ ] **Step 2: Run tests**

```powershell
.\mvnw.cmd -q test
```

Expected: PASS.

- [ ] **Step 3: Commit**

```powershell
git add src/main/java/com/example/shift/entity
git commit -m "feat: add shift coverage entity enums"
```

## Task 3: Add Staff Entities And Repositories

**Files:**
- Create: `src/main/java/com/example/shift/entity/Position.java`
- Create: `src/main/java/com/example/shift/entity/Team.java`
- Create: `src/main/java/com/example/shift/entity/Employee.java`
- Create: `src/main/java/com/example/shift/repository/PositionRepository.java`
- Create: `src/main/java/com/example/shift/repository/TeamRepository.java`
- Create: `src/main/java/com/example/shift/repository/EmployeeRepository.java`
- Test: `src/test/java/com/example/shift/service/EmployeeServiceTest.java`

Modeling note:

- `Team` is an operational grouping used for substitute eligibility, such as Sushi Bar, Kitchen, Hall, or Dinner Service.
- `Position` is a job duty within a team, such as Chef, Kitchen Helper, Roll Man, Sushi Man, Server, or Cashier.
- Employees can be qualified for multiple positions. For the MVP, model this directly as `Employee.positions`. Future payroll/rate expansion can replace or extend it with an `EmployeePosition` model that stores per-position default hourly rates and active status.
- Morning, afternoon, middle, closing, full, and custom times are not positions. The MVP stores actual shift start/end times directly; a future `ShiftTemplate` can provide store-configurable time presets.

- [ ] **Step 1: Write failing persistence test**

Create `EmployeeServiceTest.java`:

```java
package com.example.shift.service;

import com.example.shift.entity.Employee;
import com.example.shift.entity.EmployeeRole;
import com.example.shift.entity.Position;
import com.example.shift.entity.Team;
import com.example.shift.repository.EmployeeRepository;
import com.example.shift.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeServiceTest {
    @Autowired
    EmployeeRepository employees;
    @Autowired
    PositionRepository positions;
    @Autowired
    TeamRepository teams;

    @Test
    void employeeCanBelongToMultipleTeams() {
        Position server = positions.save(new Position("Server"));
        Position kitchenHelper = positions.save(new Position("Kitchen Helper"));
        Team floor = teams.save(new Team("Floor"));
        Team closing = teams.save(new Team("Closing"));

        Employee saved = employees.saveAndFlush(new Employee(
                "Mina",
                "mina@example.com",
                "604-555-0101",
                EmployeeRole.EMPLOYEE,
                Set.of(server, kitchenHelper),
                Set.of(floor, closing),
                true
        ));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPositions()).extracting(Position::getName)
                .containsExactlyInAnyOrder("Server", "Kitchen Helper");
        assertThat(saved.getTeams()).extracting(Team::getName)
                .containsExactlyInAnyOrder("Floor", "Closing");
    }
}
```

- [ ] **Step 2: Verify test fails**

```powershell
.\mvnw.cmd -q -Dtest=EmployeeServiceTest test
```

Expected: FAIL because staff entities and repositories do not exist.

- [ ] **Step 3: Implement entities**

Create `Position`, `Team`, and `Employee` as JPA entities. `Employee` must include `name`, `email`, `phoneNumber`, `role`, `positions`, `teams`, `active`, and `sharesTeamWith(Employee other)`.

- [ ] **Step 4: Implement repositories**

Create `PositionRepository`, `TeamRepository`, and `EmployeeRepository`. `EmployeeRepository` must include:

```java
List<Employee> findByActiveTrue();
```

- [ ] **Step 5: Verify test passes**

```powershell
.\mvnw.cmd -q -Dtest=EmployeeServiceTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/example/shift/entity src/main/java/com/example/shift/repository src/test/java/com/example/shift/service/EmployeeServiceTest.java
git commit -m "feat: add staff entities"
```

## Task 4: Add EmployeeService And Substitute Eligibility

**Files:**
- Create: `src/main/java/com/example/shift/service/EmployeeService.java`
- Modify: `src/test/java/com/example/shift/service/EmployeeServiceTest.java`

- [ ] **Step 1: Add failing service test**

Add:

```java
@Test
void eligibleSubstitutesMustBeActiveAndShareAtLeastOneTeam() {
    Position server = positions.save(new Position("Server"));
    Position kitchenHelper = positions.save(new Position("Kitchen Helper"));
    Team floor = teams.save(new Team("Floor"));
    Team closing = teams.save(new Team("Closing"));
    Team kitchen = teams.save(new Team("Kitchen"));

    Employee requester = employees.save(new Employee("Ari", "ari@example.com", "604-555-0102", EmployeeRole.EMPLOYEE, Set.of(server, kitchenHelper), Set.of(floor, closing), true));
    employees.save(new Employee("Ben", "ben@example.com", "604-555-0103", EmployeeRole.EMPLOYEE, Set.of(server), Set.of(closing), true));
    employees.save(new Employee("Cara", "cara@example.com", "604-555-0104", EmployeeRole.EMPLOYEE, Set.of(kitchenHelper), Set.of(kitchen), true));
    employees.save(new Employee("Inactive", "inactive@example.com", "604-555-0105", EmployeeRole.EMPLOYEE, Set.of(server), Set.of(closing), false));

    EmployeeService service = new EmployeeService(employees);

    assertThat(service.findEligibleSubstitutes(requester.getId()))
            .extracting(Employee::getName)
            .containsExactly("Ben");
}
```

- [ ] **Step 2: Verify test fails**

```powershell
.\mvnw.cmd -q -Dtest=EmployeeServiceTest test
```

Expected: FAIL because `EmployeeService` does not exist.

- [ ] **Step 3: Implement service**

```java
package com.example.shift.service;

import com.example.shift.entity.Employee;
import com.example.shift.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {
    private final EmployeeRepository employees;

    public EmployeeService(EmployeeRepository employees) {
        this.employees = employees;
    }

    @Transactional(readOnly = true)
    public List<Employee> findEligibleSubstitutes(Long requesterId) {
        Employee requester = employees.findById(requesterId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + requesterId));

        return employees.findByActiveTrue().stream()
                .filter(candidate -> !candidate.getId().equals(requester.getId()))
                .filter(requester::sharesTeamWith)
                .toList();
    }
}
```

- [ ] **Step 4: Verify test passes**

```powershell
.\mvnw.cmd -q -Dtest=EmployeeServiceTest test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/example/shift/service/EmployeeService.java src/test/java/com/example/shift/service/EmployeeServiceTest.java
git commit -m "feat: filter substitutes by shared team"
```

## Task 5: Add Shift And Fixed Weekly Schedule Pattern

**Files:**
- Create: `src/main/java/com/example/shift/entity/Shift.java`
- Create: `src/main/java/com/example/shift/entity/SchedulePattern.java`
- Create: `src/main/java/com/example/shift/entity/SchedulePatternShift.java`
- Create: `src/main/java/com/example/shift/repository/ShiftRepository.java`
- Create: `src/main/java/com/example/shift/repository/SchedulePatternRepository.java`
- Test: `src/test/java/com/example/shift/service/ScheduleServiceTest.java`

- [ ] **Step 1: Write failing persistence test**

Test should persist:

- one `Position`
- one `Team`
- one `Employee`
- one `SchedulePattern`
- one `SchedulePatternShift`
- one dated `Shift`

Assert that the pattern has one row and the shift is assigned to the employee.

- [ ] **Step 2: Verify test fails**

```powershell
.\mvnw.cmd -q -Dtest=ScheduleServiceTest test
```

Expected: FAIL because schedule entities and repositories do not exist.

- [ ] **Step 3: Implement schedule entities**

Required `Shift` fields:

- `shiftDate`
- `startTime`
- `endTime`
- `position`
- nullable `assignedEmployee`
- `status`
- `creationReason`

`Shift` represents planned work time. Future actual attendance, early leave, late arrival, breaks, and payroll export should use a separate `TimeClockRecord` or `ActualWorkRecord` model instead of rewriting the planned `Shift` times.

Required `Shift` methods:

```java
public void assignTo(Employee employee) {
    this.assignedEmployee = employee;
    this.status = ShiftStatus.SCHEDULED;
}

public void open(ShiftCreationReason reason) {
    this.assignedEmployee = null;
    this.status = ShiftStatus.OPEN;
    this.creationReason = reason;
}

public void coverBy(Employee employee) {
    this.assignedEmployee = employee;
    this.status = ShiftStatus.COVERED;
}
```

`SchedulePattern` owns `SchedulePatternShift` rows with cascade and orphan removal.

For the MVP, `SchedulePatternType.FIXED_WEEKLY` is the only supported pattern type. Future 1-week, 2-week, 3-week, 4-week, or custom manager-edited schedule periods belong to a future `PublishedSchedulePeriod` model.

- [ ] **Step 4: Implement repositories**

`SchedulePatternRepository` extends `JpaRepository<SchedulePattern, Long>`.

`ShiftRepository` includes:

```java
List<Shift> findByShiftDateBetween(LocalDate startDate, LocalDate endDate);
List<Shift> findByAssignedEmployeeAndShiftDateBetween(Employee employee, LocalDate startDate, LocalDate endDate);
Optional<Shift> findByAssignedEmployeeAndShiftDateAndStartTimeAndEndTime(Employee employee, LocalDate shiftDate, LocalTime startTime, LocalTime endTime);
```

- [ ] **Step 5: Verify test passes**

```powershell
.\mvnw.cmd -q -Dtest=ScheduleServiceTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/example/shift/entity src/main/java/com/example/shift/repository src/test/java/com/example/shift/service/ScheduleServiceTest.java
git commit -m "feat: add schedule and shift model"
```

## Task 6: Add ScheduleService Shift Generation

**Files:**
- Create: `src/main/java/com/example/shift/service/ScheduleService.java`
- Modify: `src/test/java/com/example/shift/service/ScheduleServiceTest.java`

- [ ] **Step 1: Add failing generation test**

Add a test that creates Monday and Wednesday pattern rows, generates shifts for 2026-06-01 through 2026-06-07 twice, and asserts only two shifts exist: 2026-06-01 and 2026-06-03.

- [ ] **Step 2: Verify test fails**

```powershell
.\mvnw.cmd -q -Dtest=ScheduleServiceTest test
```

Expected: FAIL because `ScheduleService` does not exist.

- [ ] **Step 3: Implement `ScheduleService`**

Methods:

```java
public void generateFixedWeeklyShifts(LocalDate startDate, LocalDate endDate)
public List<Shift> findSchedule(LocalDate startDate, LocalDate endDate)
```

Rules:

- `endDate` must be on or after `startDate`.
- Load all schedule patterns.
- Generate shifts for matching `DayOfWeek`.
- Use `ShiftStatus.SCHEDULED` and `ShiftCreationReason.PATTERN`.
- Do not create duplicates for the same employee/date/start/end.

- [ ] **Step 4: Verify test passes**

```powershell
.\mvnw.cmd -q -Dtest=ScheduleServiceTest test
```

Expected: PASS.

- [ ] **Step 5: Commit**

```powershell
git add src/main/java/com/example/shift/service/ScheduleService.java src/test/java/com/example/shift/service/ScheduleServiceTest.java
git commit -m "feat: generate shifts from fixed weekly patterns"
```

## Task 7: Add Coverage Request Flow

**Files:**
- Create: `src/main/java/com/example/shift/entity/CoverageRequest.java`
- Create: `src/main/java/com/example/shift/repository/CoverageRequestRepository.java`
- Create: `src/main/java/com/example/shift/service/CoverageRequestService.java`
- Test: `src/test/java/com/example/shift/service/CoverageRequestServiceTest.java`

- [ ] **Step 1: Write service tests**

Cover:

- substitute request without shared team throws `IllegalArgumentException`
- shared-team substitute request creates pending request
- approving substitute request covers shift with substitute
- approving absence without substitute opens shift

- [ ] **Step 2: Verify tests fail**

```powershell
.\mvnw.cmd -q -Dtest=CoverageRequestServiceTest test
```

Expected: FAIL because coverage request classes do not exist.

- [ ] **Step 3: Implement entity and repository**

`CoverageRequest` fields:

- `requestingEmployee`
- `targetShift`
- `substituteEmployee`
- `reason`
- `requestType`
- `status`

Repository extends `JpaRepository<CoverageRequest, Long>`.

- [ ] **Step 4: Implement service**

Methods:

```java
public CoverageRequest requestSubstitute(Long requesterId, Long shiftId, Long substituteId, String reason)
public CoverageRequest requestAbsenceWithoutSubstitute(Long requesterId, Long shiftId, String reason)
public CoverageRequest approve(Long requestId)
public CoverageRequest decline(Long requestId)
```

Rules:

- requester must be assigned to the target shift.
- substitute must share at least one team with requester.
- approved substitute request calls `targetShift.coverBy(substituteEmployee)`.
- approved absence request calls `targetShift.open(ShiftCreationReason.ABSENCE_REQUEST)`.

- [ ] **Step 5: Verify tests pass**

```powershell
.\mvnw.cmd -q -Dtest=CoverageRequestServiceTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/example/shift/entity/CoverageRequest.java src/main/java/com/example/shift/repository/CoverageRequestRepository.java src/main/java/com/example/shift/service/CoverageRequestService.java src/test/java/com/example/shift/service/CoverageRequestServiceTest.java
git commit -m "feat: add coverage request workflow"
```

## Task 8: Add Planned Leave Flow

**Files:**
- Create: `src/main/java/com/example/shift/entity/LeaveRequest.java`
- Create: `src/main/java/com/example/shift/repository/LeaveRequestRepository.java`
- Create: `src/main/java/com/example/shift/service/LeaveRequestService.java`
- Modify: `src/main/java/com/example/shift/service/ScheduleService.java`
- Test: `src/test/java/com/example/shift/service/LeaveRequestServiceTest.java`

- [ ] **Step 1: Write leave approval test**

Create a test where an employee has two scheduled shifts in a leave date range. Approving leave should clear assigned employee, set shifts to `OPEN`, and set creation reason to `LEAVE_REQUEST`.

- [ ] **Step 2: Verify test fails**

```powershell
.\mvnw.cmd -q -Dtest=LeaveRequestServiceTest test
```

Expected: FAIL because leave request classes do not exist.

- [ ] **Step 3: Implement entity and repository**

`LeaveRequest` fields:

- `employee`
- `startDate`
- `endDate`
- `reason`
- `status`
- `approvedBy`
- `approvedAt`

Repository includes:

```java
List<LeaveRequest> findByEmployeeAndStatusAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
        Employee employee,
        RequestStatus status,
        LocalDate dateForStart,
        LocalDate dateForEnd
);
```

- [ ] **Step 4: Implement service**

Methods:

```java
public LeaveRequest requestLeave(Long employeeId, LocalDate startDate, LocalDate endDate, String reason)
public LeaveRequest approve(Long leaveRequestId, Long managerId)
public LeaveRequest decline(Long leaveRequestId, Long managerId)
```

Rules:

- `endDate` must be on or after `startDate`.
- approval opens employee shifts in range with `ShiftCreationReason.LEAVE_REQUEST`.
- approval sets `approvedBy`, `approvedAt`, and `APPROVED`.

- [ ] **Step 5: Update `ScheduleService` to skip approved leave**

Before generating a shift, check whether the employee has an approved leave request for that date. Skip generation if they do.

- [ ] **Step 6: Verify tests pass**

```powershell
.\mvnw.cmd -q -Dtest=LeaveRequestServiceTest,ScheduleServiceTest test
```

Expected: PASS.

- [ ] **Step 7: Commit**

```powershell
git add src/main/java/com/example/shift/entity/LeaveRequest.java src/main/java/com/example/shift/repository/LeaveRequestRepository.java src/main/java/com/example/shift/service/LeaveRequestService.java src/main/java/com/example/shift/service/ScheduleService.java src/test/java/com/example/shift/service
git commit -m "feat: add planned leave workflow"
```

## Task 9: Add Open Shift Application And Assignment

**Files:**
- Create: `src/main/java/com/example/shift/entity/OpenShiftApplication.java`
- Create: `src/main/java/com/example/shift/repository/OpenShiftApplicationRepository.java`
- Create: `src/main/java/com/example/shift/service/OpenShiftService.java`
- Test: `src/test/java/com/example/shift/service/OpenShiftServiceTest.java`

- [ ] **Step 1: Write service tests**

Cover:

- employee can apply to open shift
- approving application assigns employee and schedules shift
- manager can directly assign employee to open shift
- employee on approved leave cannot be assigned

- [ ] **Step 2: Verify tests fail**

```powershell
.\mvnw.cmd -q -Dtest=OpenShiftServiceTest test
```

Expected: FAIL because open shift application classes do not exist.

- [ ] **Step 3: Implement entity and repository**

`OpenShiftApplication` fields:

- `openShift`
- `applyingEmployee`
- `status`

Repository extends `JpaRepository<OpenShiftApplication, Long>`.

- [ ] **Step 4: Implement service**

Methods:

```java
public OpenShiftApplication apply(Long employeeId, Long shiftId)
public OpenShiftApplication approveApplication(Long applicationId)
public Shift directlyAssign(Long shiftId, Long employeeId)
```

Rules:

- shift status must be `OPEN` or `NEEDS_COVERAGE`.
- employee must be active.
- employee must not be on approved leave for the shift date.
- assignment sets `assignedEmployee` and `ShiftStatus.SCHEDULED`.

- [ ] **Step 5: Verify tests pass**

```powershell
.\mvnw.cmd -q -Dtest=OpenShiftServiceTest test
```

Expected: PASS.

- [ ] **Step 6: Commit**

```powershell
git add src/main/java/com/example/shift/entity/OpenShiftApplication.java src/main/java/com/example/shift/repository/OpenShiftApplicationRepository.java src/main/java/com/example/shift/service/OpenShiftService.java src/test/java/com/example/shift/service/OpenShiftServiceTest.java
git commit -m "feat: add open shift assignment workflow"
```

## Task 10: Add Versioned REST API, CORS, And MVP Security

**Files:**
- Create: `src/main/java/com/example/shift/config/CorsProperties.java`
- Create: `src/main/java/com/example/shift/config/CorsConfig.java`
- Create: `src/main/resources/application-dev.yml`
- Create: `src/main/resources/application-test.yml`
- Create: `src/main/java/com/example/shift/controller/EmployeeController.java`
- Create: `src/main/java/com/example/shift/controller/ScheduleController.java`
- Create: `src/main/java/com/example/shift/controller/CoverageRequestController.java`
- Create: `src/main/java/com/example/shift/controller/LeaveRequestController.java`
- Create: `src/main/java/com/example/shift/controller/OpenShiftController.java`
- Modify: `src/main/java/com/example/shift/config/SecurityConfig.java`
- Test: `src/test/java/com/example/shift/controller/CorsConfigTest.java`
- Test: `src/test/java/com/example/shift/controller/ShiftCoverageApiTest.java`

Execution note:

Do not postpone this task until every domain workflow is finished. By 2026-06-05, create the controller and DTO skeletons that the frontend needs and return mock or minimal responses where necessary. As Tasks 5-9 become real, replace those internals with service calls while keeping endpoint names and DTO shapes stable.

- [ ] **Step 1: Write CORS preflight test**

Use `@SpringBootTest`, `@AutoConfigureMockMvc`, and `@TestPropertySource` to verify:

```java
mockMvc.perform(options("/api/v1/schedule")
        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));
```

- [ ] **Step 2: Verify CORS test fails**

```powershell
.\mvnw.cmd -q -Dtest=CorsConfigTest test
```

Expected: FAIL because CORS configuration does not exist.

- [ ] **Step 3: Implement CORS configuration**

Create `CorsProperties` with `app.cors.allowed-origins`.

Create `CorsConfig` that registers `/api/**` and allows:

- `GET`
- `POST`
- `PUT`
- `PATCH`
- `DELETE`
- `OPTIONS`

Use `allowCredentials(false)` for MVP development.

- [ ] **Step 4: Add `application-dev.yml`**

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/managing_shift_dev?serverTimezone=America/Vancouver&characterEncoding=UTF-8
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update

app:
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://127.0.0.1:5173
      # Replace this with your PC LAN IP for optional phone testing.
      - http://192.168.0.20:5173
```

- [ ] **Step 5: Add `application-test.yml`**

Use H2 only for fast tests:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:managing_shift_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: create-drop
```

- [ ] **Step 6: Enable CORS and permit `/api/**` in `SecurityConfig`**

Add `.cors(cors -> {})` and permit:

```java
.requestMatchers("/", "/home", "/signup", "/api/**").permitAll()
```

- [ ] **Step 7: Verify CORS test passes**

```powershell
.\mvnw.cmd -q -Dtest=CorsConfigTest test
```

Expected: PASS.

- [ ] **Step 8: Write API smoke test**

Use `/api/v1/**` endpoints:

```text
GET  /api/v1/employees/{id}/eligible-substitutes
GET  /api/v1/schedule?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
POST /api/v1/coverage-requests/substitute
POST /api/v1/coverage-requests/absence
POST /api/v1/leave-requests
POST /api/v1/open-shifts/{shiftId}/applications
POST /api/v1/open-shifts/{shiftId}/assignments
```

DTO records should live under `src/main/java/com/example/shift/dto`. Names should avoid web-only terms and stay reusable by a future React Native client, such as:

- `ShiftSummaryResponse`
- `CoverageRequestResponse`
- `ManagerQueueItemResponse`
- `ApiErrorResponse`

- [ ] **Step 9: Verify API smoke test fails**

```powershell
.\mvnw.cmd -q -Dtest=ShiftCoverageApiTest test
```

Expected: FAIL because `/api/v1/**` controllers do not exist yet.

- [ ] **Step 10: Implement controllers**

Controllers call services only. Do not put business rules in controllers.

Request records:

```java
record SubstituteRequest(Long requesterId, Long shiftId, Long substituteId, String reason) {}
record AbsenceRequest(Long requesterId, Long shiftId, String reason) {}
record LeaveRequestPayload(Long employeeId, LocalDate startDate, LocalDate endDate, String reason) {}
record OpenShiftApplyRequest(Long employeeId) {}
```

- [ ] **Step 11: Run all tests**

```powershell
.\mvnw.cmd -q test
```

Expected: PASS.

- [ ] **Step 12: Run the API with MySQL dev profile**

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

Expected: the backend starts against local MySQL, not H2.

- [ ] **Step 13: Check Chrome mobile viewport networking**

In the separate frontend project:

```powershell
npm run dev
```

Open:

```text
http://localhost:5173
```

Use Chrome DevTools device toolbar with an iPhone or Android viewport.

Expected: frontend calls `http://localhost:8080/api/v1/**` and browser console has no CORS preflight errors.

- [ ] **Step 14: Optional real phone LAN networking**

Only do this when testing on an actual phone. Replace `http://192.168.0.20:5173` with the real PC LAN IP in local dev configuration.

Frontend:

```powershell
npm run dev -- --host 0.0.0.0
```

Phone opens:

```text
http://PC_IP:5173
```

Expected: frontend calls `http://PC_IP:8080/api/v1/**` and browser console has no CORS preflight errors.

- [ ] **Step 15: Commit**

```powershell
git add src/main/java/com/example/shift/controller src/main/java/com/example/shift/dto src/main/java/com/example/shift/config src/main/resources/application-dev.yml src/main/resources/application-test.yml src/test/java/com/example/shift/controller
git commit -m "feat: expose shift coverage api for mobile clients"
```

## Self-Review

Spec coverage:

- Employee, Position, Team, multiple team membership: Task 3.
- Shared-team substitute eligibility: Tasks 4 and 7.
- Fixed weekly schedule pattern: Tasks 5 and 6.
- Actual dated shift: Task 5.
- Coverage request with substitute: Task 7.
- Absence request without substitute: Task 7.
- Planned leave date range and shift open conversion: Task 8.
- Open shift application and direct assignment: Task 9.
- Manager/owner approval backend surface: Task 10.
- Separate React/Vite PWA and Chrome mobile viewport CORS communication: Task 10.
- Optional real phone LAN CORS communication: Task 10.
- Stable `/api/v1/**` contract for a future React Native client: Task 10.
- MySQL-backed local/dev integration: Task 10.
- H2-only fast test support: Task 10.
- Employee home, weekly, and monthly frontend screens: out of scope for this backend foundation.
- Payroll/overtime calculation: out of scope.
- Tenant/location/auth invitation: out of scope.

Placeholder scan:

- No unfinished placeholder wording remains.
- Tasks 7-10 specify methods, endpoints, and required assertions. During execution, create test fixtures using the same pattern as Tasks 3-5.

Type consistency:

- request status uses `RequestStatus`.
- shift status uses `ShiftStatus`.
- schedule pattern type uses `SchedulePatternType.FIXED_WEEKLY`.
- absence without substitute uses `CoverageRequestType.ABSENCE_WITHOUT_SUBSTITUTE`.
- planned leave uses `LeaveRequest`.
- open shift application uses `OpenShiftApplication`.

## Execution Handoff

Plan complete and saved to `docs/superpowers/plans/2026-05-23-shift-coverage-backend-foundation-plan.md`. Two execution options:

**1. Subagent-Driven (recommended)** - dispatch a fresh subagent per task and review between tasks.

**2. Inline Execution** - execute tasks in this session using `superpowers:executing-plans`, with checkpoints.

