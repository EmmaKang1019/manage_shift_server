# Shift Coverage Backend Foundation 구현 계획

> 이 문서는 `managingShift` Spring Boot 프로젝트에서 shift coverage MVP 백엔드 기반을 학습하며 구현하기 위한 한국어 계획서다. 영문 기준 문서는 `docs/superpowers/plans/2026-05-23-shift-coverage-backend-foundation-plan.md`이다.

## 목표

현재 `managingShift` 프로젝트에 shift coverage MVP의 백엔드 기반을 만든다.

첫 클라이언트는 별도 React/Vite 모바일 우선 PWA이며, 향후 React Native 앱도 같은 API 계약을 재사용할 수 있어야 한다.

핵심 방향:

- Spring Boot 백엔드를 API-first 서비스로 만든다.
- 안정적인 `/api/v1/**` JSON API를 제공한다.
- 프론트엔드 개발이 백엔드 전체 구현을 기다리지 않도록 controller/dto skeleton을 먼저 열 수 있게 한다.
- controller는 얇게 유지한다.
- 비즈니스 규칙은 service에 둔다.
- DB 매핑 클래스와 enum은 `entity`에 둔다.
- API request/response 객체는 `dto`에 둔다.
- local/dev 및 운영 DB는 MySQL을 기준으로 한다.
- H2는 빠른 JPA 테스트 보조 용도로만 사용한다.
- 테스트는 `src/test/java`에 둔다.
- `src/main/java` 아래에 `test` 패키지를 만들지 않는다.

## 아키텍처

기본 패키지 구조:

```text
com.example.shift
  controller
  dto
  entity
  repository
  service
  config
```

각 패키지 책임:

- `controller`: HTTP 요청 진입점. `/api/v1/**` URI를 제공한다.
- `dto`: API 요청/응답 전용 객체. 엔티티를 직접 노출하지 않는다.
- `entity`: JPA entity와 entity에서 사용하는 enum.
- `repository`: Spring Data JPA repository.
- `service`: transaction boundary와 비즈니스 규칙.
- `config`: 보안, CORS, Spring 설정.

이전 계획의 용어는 다음처럼 바꿔 이해한다.

```text
domain -> entity
web -> controller
request/response record -> dto
```

## 현재 상태

프로젝트 루트:

```text
C:\Users\MJ\Documents\workSpaces\manage_shift_server\managingShift
```

주요 파일:

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
docs/superpowers/plans/2026-05-23-shift-coverage-backend-foundation-plan.md
docs/superpowers/plans/2026-05-23-shift-coverage-backend-foundation-plan.ko.md
```

## DB 전략

local/dev, frontend integration, staging-like check, production은 MySQL을 기준으로 한다.

H2는 빠른 JPA slice test 용도로만 사용한다.

프로필:

- `local/dev`: MySQL. React/Vite PWA가 실제 API를 호출하는 환경.
- `test`: H2. 빠른 테스트용이며 MySQL compatibility mode를 선호한다.
- pre-release integration: MySQL 필수.
- `prod`: MySQL. 개발 CORS origin과 seed/test data는 제외한다.

H2만 믿으면 MySQL에서 DDL, reserved word, enum/string 저장, 날짜/시간 처리, constraint/index 차이로 문제가 늦게 발견될 수 있다.

## 서버 환경 전략

운영 서버와 개발 서버는 분리하는 것을 권장한다.

환경:

```text
local
- 개발자 PC
- 빠른 코드 수정과 unit/slice test
- local MySQL dev 또는 H2 test 사용

development server
- 2026-06-05 전후 첫 배포 대상
- 프론트엔드가 실제 backend URL을 호출하는 개발 환경
- 별도 MySQL dev DB 사용
- mock/minimal API skeleton 배포 가능
- 데이터는 언제든 초기화 가능

production server
- 2026-07-01 운영 시작 목표
- 실제 사용자와 운영 데이터
- production MySQL DB 사용
- 검증된 release candidate만 배포
```

권장:

- 가능하면 개발 서버와 운영 서버를 물리적으로 분리한다.
- 비용이나 관리 부담이 있으면 하나의 VPS 안에서 dev/prod process, DB, env var, domain을 논리적으로 분리한다.
- 하나의 서버/DB만 사용하는 방식은 2026-07-01 출시 목표에는 위험하다.

## 병행 개발 일정

운영 시작 목표는 2026-07-01이지만, 배포와 운영 리허설에 시간이 필요하므로 개발 완료 목표는 2026-06-20으로 앞당긴다. 2026-06-21 이후는 기능 개발이 아니라 release candidate 안정화, 배포 리허설, 모바일 QA, 운영 환경 점검에 사용한다.

프론트엔드는 별도 React/Vite 프로젝트로 2026-06-03에 시작한다. 이때 백엔드 전체 기능이 끝나 있지 않아도, `/api/v1/**` endpoint 이름과 DTO 계약을 먼저 고정하고 mock/minimal response로 화면 개발을 병행한다.

```text
2026-05-30 ~ 2026-06-02
- 현재 테스트 통과 상태 유지
- Employee/Team/Position/EmployeeService 구현 상태 확인 및 부족한 테스트 보강
- 화면별 API 목록과 DTO 계약 확정
- MySQL dev profile, test profile, CORS 설계 정리
- /api/v1 controller + dto skeleton의 실패 테스트 준비

2026-06-03 ~ 2026-06-05
- /api/v1 controller + dto skeleton 구현
- 아직 domain logic이 없는 endpoint는 mock 또는 minimal response 반환
- SecurityConfig/CORS 설정 추가
- 개발 서버 첫 배포 또는 로컬 통합 실행 기준 확정
- CORS, MySQL 연결, API smoke test 확인
- 별도 React/Vite PWA 프로젝트 생성
- 프론트엔드 API client, TypeScript DTO type, 모바일 shell, Home/Schedule 기본 화면 시작

2026-06-08 ~ 2026-06-12
- SchedulePattern, Shift, ScheduleService 실제 구현
- Employee, eligible substitutes API를 실제 service와 연결
- Home/Schedule 화면을 실제 API와 연결
- My Requests/Manager Queue 화면의 정적 shell 준비
- 개발 서버 또는 로컬 통합 환경에서 매일 최소 1회 프론트 연동 확인

2026-06-13 ~ 2026-06-16
- CoverageRequest, absence request, planned leave backend 구현
- Manager Queue read/approve/decline API 구현
- My Requests와 Manager Queue 화면을 실제 API와 연결
- 요청 생성/승인 흐름 end-to-end 확인

2026-06-17 ~ 2026-06-20
- OpenShift application, assignment, approval, decline 구현
- 직원/매니저 핵심 흐름 모바일 QA
- loading/empty/error state 보강
- API error response와 CORS 최종 점검
- MVP 개발 완료

2026-06-21 ~ 2026-06-24
- release candidate 안정화
- MySQL 기반 통합 테스트
- 모바일 브라우저 smoke test
- production deployment 절차 1차 리허설

2026-06-25 ~ 2026-06-30
- 운영 profile/env/domain/DB 점검
- 배포 절차 2차 리허설
- 회귀 테스트와 최종 버그 수정
- 운영 데이터 초기값과 rollback 절차 확인

2026-07-01
- production server launch
```

가장 중요한 milestone은 2026-06-05다. 이때까지 프론트엔드가 안정적인 `/api/v1/**` skeleton endpoint를 호출할 수 있어야 한다. 두 번째 milestone은 2026-06-12다. 이때 Home/Schedule이 실제 API로 동작해야 한다. 세 번째 milestone은 2026-06-20이며, 이때는 신규 기능 개발을 멈추고 release candidate 안정화로 넘어간다.

## 파일 구조

main source:

```text
src/main/java/com/example/shift/entity
src/main/java/com/example/shift/repository
src/main/java/com/example/shift/service
src/main/java/com/example/shift/controller
src/main/java/com/example/shift/dto
```

test source:

```text
src/test/java
  com/example/shift/service
  com/example/shift/controller
```

테스트는 반드시 `src/test/java` 아래에 둔다. `src/main/java` 아래에 `test` 패키지를 만들지 않는다.

## 프로젝트 용어

enum 이름은 다음과 같이 일관되게 사용한다.

```java
public enum EmployeeRole { EMPLOYEE, MANAGER, OWNER }
public enum ShiftStatus { SCHEDULED, OPEN, CANCELLED, COVERED, NEEDS_COVERAGE }
public enum ShiftCreationReason { PATTERN, ABSENCE_REQUEST, LEAVE_REQUEST, EXTRA_STAFFING, NEW_POSITION_COVERAGE, MANUAL }
public enum RequestStatus { PENDING, APPROVED, DECLINED, CANCELLED }
public enum CoverageRequestType { SUBSTITUTE_REQUESTED, ABSENCE_WITHOUT_SUBSTITUTE }
public enum SchedulePatternType { FIXED_WEEKLY }
```

`SchedulePatternType.FIXED_WEEKLY`는 매주 같은 고정 스케줄을 나타낸다. 예를 들어 월/화 full, 수 evening, 목/토 off, 금/일 full 같은 반복 패턴이다.

향후 1주, 2주, 3주, 4주 또는 custom period를 매니저가 직접 작성하고 publish하는 기능은 `PublishedSchedulePeriod`로 확장한다. `SchedulePatternType`은 반복 규칙 자체가 달라질 때만 확장한다.

## Task 1: Maven Wrapper 기준 확인과 임시 controller 제거

목표:

- Maven wrapper가 테스트를 실행할 수 있는지 확인한다.
- `src/main/java` 아래의 임시 test controller를 제거한다.

관련 파일:

```text
src/main/java/com/example/shift/test/LoginSecurityTest.java
.mvn/wrapper/maven-wrapper.properties
mvnw.cmd
```

확인 명령:

```powershell
.\mvnw.cmd -q test
Get-ChildItem -Recurse src/main/java -File | Where-Object { $_.FullName -match "\\test\\" }
```

학습 포인트:

- `src/test/java`는 테스트 코드 위치다.
- `src/main/java` 아래 코드는 실제 애플리케이션 코드로 포함된다.
- 이름이 `Test`여도 `src/main/java`에 있고 `@Controller`가 붙어 있으면 실제 서버 코드다.

## Task 2: Entity Enum 추가

목표:

`entity` 패키지에 앞으로 entity에서 사용할 enum을 추가한다.

생성 파일:

```text
src/main/java/com/example/shift/entity/EmployeeRole.java
src/main/java/com/example/shift/entity/ShiftStatus.java
src/main/java/com/example/shift/entity/ShiftCreationReason.java
src/main/java/com/example/shift/entity/RequestStatus.java
src/main/java/com/example/shift/entity/CoverageRequestType.java
src/main/java/com/example/shift/entity/SchedulePatternType.java
```

진행:

1. `entity` 패키지를 만든다.
2. `EmployeeRole` 하나를 먼저 만든다.
3. `.\mvnw.cmd -q test`로 확인한다.
4. 나머지 enum도 같은 방식으로 추가한다.
5. 전체 테스트를 다시 확인한다.

학습 포인트:

- enum은 정해진 선택지를 타입으로 표현하는 Java 문법이다.
- 문자열보다 오타와 잘못된 값을 줄일 수 있다.
- JPA entity에서 저장될 값이므로 `entity` 패키지에 둔다.

## Task 3: Staff Entity와 Repository 추가

목표:

직원, 팀, 포지션을 DB에 저장할 수 있게 만든다.

생성 파일:

```text
src/main/java/com/example/shift/entity/Position.java
src/main/java/com/example/shift/entity/Team.java
src/main/java/com/example/shift/entity/Employee.java
src/main/java/com/example/shift/repository/PositionRepository.java
src/main/java/com/example/shift/repository/TeamRepository.java
src/main/java/com/example/shift/repository/EmployeeRepository.java
src/test/java/com/example/shift/service/EmployeeServiceTest.java
```

모델 메모:

- `Team`은 substitute eligibility 판단에 사용하는 운영 그룹이다.
- `Position`은 팀 안에서 맡는 직무다.
- 한 직원은 여러 `Position`과 여러 `Team`을 가질 수 있다.
- MVP에서는 `Employee.positions`를 직접 사용한다.
- 향후 시급, 자격 상태, 적용 기간이 필요하면 `EmployeePosition`으로 확장한다.
- morning, afternoon, closing, full 같은 시간대는 position이 아니다. MVP에서는 `Shift`에 실제 시작/종료 시간을 저장한다.

TDD 순서:

1. 직원이 여러 팀/포지션을 가질 수 있다는 실패 테스트를 먼저 작성한다.
2. `.\mvnw.cmd -q -Dtest=EmployeeServiceTest test`로 실패를 확인한다.
3. `Position`, `Team`, `Employee` entity를 만든다.
4. 각 repository를 만든다.
5. 같은 테스트가 통과하는지 확인한다.

학습 포인트:

- `@Entity`는 DB 테이블과 매핑되는 객체다.
- repository는 DB 접근을 담당한다.
- many-to-many 관계와 join table의 기본 구조를 익힌다.

## Task 4: EmployeeService와 대체근무 가능자 조회

목표:

같은 팀을 공유하는 active 직원만 대체근무자로 추천한다.

규칙:

- 대체근무자는 active 상태여야 한다.
- 요청자와 최소 한 팀을 공유해야 한다.
- 자기 자신은 대체근무자가 될 수 없다.

관련 파일:

```text
src/main/java/com/example/shift/service/EmployeeService.java
src/test/java/com/example/shift/service/EmployeeServiceTest.java
```

학습 포인트:

- business rule은 controller가 아니라 service에 둔다.
- entity에는 작은 domain helper method를 둘 수 있다. 예: `sharesTeamWith(Employee other)`.
- 테스트는 한 가지 이유로 실패하도록 나누는 것이 좋다.

## Task 5: Shift와 Fixed Weekly Schedule Pattern 추가

목표:

고정 주간 패턴과 실제 날짜의 shift를 표현한다.

생성 파일:

```text
src/main/java/com/example/shift/entity/Shift.java
src/main/java/com/example/shift/entity/SchedulePattern.java
src/main/java/com/example/shift/entity/SchedulePatternShift.java
src/main/java/com/example/shift/repository/ShiftRepository.java
src/main/java/com/example/shift/repository/SchedulePatternRepository.java
src/test/java/com/example/shift/service/ScheduleServiceTest.java
```

필수 `Shift` 필드:

- `shiftDate`
- `startTime`
- `endTime`
- `position`
- nullable `assignedEmployee`
- `status`
- `creationReason`

필수 `Shift` 메서드:

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

학습 포인트:

- 반복되는 패턴과 실제 날짜의 근무를 분리한다.
- `LocalDate`, `LocalTime`, enum status를 사용한다.
- `Shift`는 예정 근무다. 실제 출퇴근, 조퇴, 지각, 휴게시간, 급여 export는 향후 별도 모델로 분리한다.

## Task 6: ScheduleService Shift 생성

목표:

fixed weekly pattern을 기준으로 날짜 범위 안의 shift를 생성한다.

서비스 메서드:

```java
public void generateFixedWeeklyShifts(LocalDate startDate, LocalDate endDate)
public List<Shift> findSchedule(LocalDate startDate, LocalDate endDate)
```

규칙:

- `endDate`는 `startDate`보다 같거나 뒤여야 한다.
- 모든 schedule pattern을 읽는다.
- `DayOfWeek`가 일치하는 pattern row로 shift를 생성한다.
- 생성된 shift는 `SCHEDULED`, `PATTERN`을 사용한다.
- 같은 직원/날짜/시작/종료 시간의 중복 shift를 만들지 않는다.

학습 포인트:

- service가 비즈니스 동작의 중심이다.
- 날짜 범위와 중복 생성 방지를 테스트한다.

## Task 7: Coverage Request 흐름 추가

목표:

직원이 특정 shift에 대해 대체 요청 또는 대체자 없는 결근 요청을 만들 수 있게 한다.

생성 파일:

```text
src/main/java/com/example/shift/entity/CoverageRequest.java
src/main/java/com/example/shift/repository/CoverageRequestRepository.java
src/main/java/com/example/shift/service/CoverageRequestService.java
src/test/java/com/example/shift/service/CoverageRequestServiceTest.java
```

주요 규칙:

- requester는 target shift에 배정된 직원이어야 한다.
- substitute는 requester와 최소 한 팀을 공유해야 한다.
- substitute request 승인 시 `targetShift.coverBy(substituteEmployee)`를 호출한다.
- absence request 승인 시 `targetShift.open(ShiftCreationReason.ABSENCE_REQUEST)`를 호출한다.

테스트:

- shared team이 없는 substitute request는 실패한다.
- shared team substitute request는 pending request를 만든다.
- substitute request 승인 시 shift가 substitute로 covered 된다.
- substitute 없는 absence 승인 시 shift가 open 된다.

## Task 8: Planned Leave 흐름 추가

목표:

직원이 날짜 범위 기반 leave request를 만들 수 있게 한다.

생성 파일:

```text
src/main/java/com/example/shift/entity/LeaveRequest.java
src/main/java/com/example/shift/repository/LeaveRequestRepository.java
src/main/java/com/example/shift/service/LeaveRequestService.java
src/test/java/com/example/shift/service/LeaveRequestServiceTest.java
```

주요 규칙:

- `endDate`는 `startDate`보다 같거나 뒤여야 한다.
- 승인 시 leave 기간의 employee shift를 open으로 전환한다.
- 승인 시 `approvedBy`, `approvedAt`, `APPROVED`를 설정한다.
- `ScheduleService`는 approved leave 중인 직원에 대해 shift 생성을 건너뛴다.

학습 포인트:

- 날짜 범위 조건을 테스트한다.
- 하나의 승인 동작이 여러 shift 상태를 바꾸는 흐름을 익힌다.

## Task 9: Open Shift Application과 Assignment 추가

목표:

직원이 open shift에 신청하고, 매니저가 승인하거나 직접 배정할 수 있게 한다.

생성 파일:

```text
src/main/java/com/example/shift/entity/OpenShiftApplication.java
src/main/java/com/example/shift/repository/OpenShiftApplicationRepository.java
src/main/java/com/example/shift/service/OpenShiftService.java
src/test/java/com/example/shift/service/OpenShiftServiceTest.java
```

주요 규칙:

- shift status는 `OPEN` 또는 `NEEDS_COVERAGE`여야 한다.
- 직원은 active 상태여야 한다.
- approved leave 중인 직원은 해당 날짜 shift에 배정할 수 없다.
- 배정되면 assigned employee를 설정하고 shift status를 `SCHEDULED`로 바꾼다.

테스트:

- 직원이 open shift에 신청할 수 있다.
- application 승인 시 직원이 배정된다.
- 매니저가 직접 open shift에 직원을 배정할 수 있다.
- approved leave 중인 직원은 배정할 수 없다.

## Task 10: Versioned REST API, CORS, MVP Security

목표:

프론트엔드가 호출할 `/api/v1/**` REST API를 만든다.

이 task는 모든 domain workflow가 끝난 뒤에만 시작하지 않는다. 2026-06-05까지 프론트엔드가 개발을 시작할 수 있도록 controller와 dto skeleton을 먼저 만들고, 아직 구현되지 않은 내부 로직은 mock 또는 minimal response로 둔다. 이후 Task 5~9의 실제 service 구현으로 교체한다.

생성/수정 파일:

```text
src/main/java/com/example/shift/config/CorsProperties.java
src/main/java/com/example/shift/config/CorsConfig.java
src/main/resources/application-dev.yml
src/main/resources/application-test.yml
src/main/java/com/example/shift/controller/EmployeeController.java
src/main/java/com/example/shift/controller/ScheduleController.java
src/main/java/com/example/shift/controller/CoverageRequestController.java
src/main/java/com/example/shift/controller/LeaveRequestController.java
src/main/java/com/example/shift/controller/OpenShiftController.java
src/main/java/com/example/shift/dto/*.java
src/test/java/com/example/shift/controller/CorsConfigTest.java
src/test/java/com/example/shift/controller/ShiftCoverageApiTest.java
```

API 예:

```text
GET  /api/v1/employees/{id}/eligible-substitutes
GET  /api/v1/schedule?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
POST /api/v1/coverage-requests/substitute
POST /api/v1/coverage-requests/absence
POST /api/v1/leave-requests
POST /api/v1/open-shifts/{shiftId}/applications
POST /api/v1/open-shifts/{shiftId}/assignments
```

DTO 예:

```text
ShiftSummaryResponse
CoverageRequestResponse
ManagerQueueItemResponse
ApiErrorResponse
SubstituteRequest
AbsenceRequest
LeaveRequestPayload
OpenShiftApplyRequest
```

`application-dev.yml` 예:

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
```

`application-test.yml` 예:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:managing_shift_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: create-drop
```

학습 포인트:

- entity를 API response로 직접 노출하지 않는다.
- controller는 DTO를 받고 DTO를 반환한다.
- controller는 service를 호출만 하고 business rule을 직접 처리하지 않는다.
- CORS는 별도 React/Vite PWA와 통신하기 위해 필요하다.

## 테스트 원칙

각 기능은 가능한 한 다음 순서로 진행한다.

1. 실패하는 테스트를 먼저 작성한다.
2. 왜 실패하는지 확인한다.
3. 최소 구현으로 테스트를 통과시킨다.
4. 필요한 경우 리팩터링한다.
5. 관련 테스트를 다시 실행한다.

기본 명령:

```powershell
.\mvnw.cmd -q test
```

특정 테스트:

```powershell
.\mvnw.cmd -q -Dtest=EmployeeServiceTest test
```

MySQL dev profile 실행:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

H2 테스트만 통과했다고 완료로 보지 않는다. 주요 API smoke test와 프론트엔드 연동은 MySQL dev 또는 staging-like MySQL 환경에서 확인한다.

## 학습 모드 진행 메모

이 프로젝트는 guided learning mode로 진행한다.

- 먼저 목표를 말로 설명한다.
- 관련 파일과 개념을 확인한다.
- 사용자가 작은 단계를 직접 시도한다.
- 결과를 함께 확인한다.
- 막히면 힌트를 먼저 주고, 필요할 때만 최소 코드 예시를 제공한다.
- 한 단계마다 필요한 테스트를 실행하거나 요청한다.
- 다음 task로 넘어가기 전에 무엇을 배웠는지 요약한다.

구현계획서의 코드 예시는 참고 자료이며, 반드시 그대로 복사해야 하는 정답은 아니다.

## Self-Review

이 계획은 다음 요구를 다룬다.

- Employee, Position, Team, multiple membership.
- shared-team substitute eligibility.
- fixed weekly schedule pattern.
- actual dated Shift.
- coverage request with substitute.
- absence request without substitute.
- planned leave와 shift open conversion.
- open shift application and direct assignment.
- manager/owner approval backend surface.
- React/Vite PWA CORS communication.
- future React Native client를 위한 `/api/v1/**` API contract.
- MySQL-backed local/dev integration.
- H2-backed fast test support.

제외 범위:

- 프론트엔드 화면 구현.
- React Native 앱 구현.
- payroll/overtime calculation.
- tenant/location/auth invitation.
- notification delivery.
