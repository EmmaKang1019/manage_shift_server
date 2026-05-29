# Shift Coverage Backend Foundation 구현 계획

> 이 문서는 `managingShift` Spring Boot 백엔드를 학습하면서 구현하기 위한 한국어 계획서이다.
> 상세 코드 예제와 긴 테스트 코드는 영어 계획서
> `docs/superpowers/plans/2026-05-23-shift-coverage-backend-foundation-plan.md`를 기준으로 확인한다.

## 목표

현재 `managingShift` 프로젝트에 근무 대체 요청 MVP의 백엔드 기반을 만든다.

첫 번째 클라이언트는 별도 React/Vite 모바일 우선 PWA이고, 이후 React Native 앱도 같은 API 계약을 재사용할 수 있어야 한다.

핵심 방향:

- Spring Boot 백엔드를 API-first 서비스로 만든다.
- 안정적인 `/api/v1/**` JSON API를 제공한다.
- 프론트엔드 개발이 백엔드 전체 구현을 기다리지 않도록 API 계약과 controller/dto skeleton을 먼저 연다.
- 컨트롤러는 얇게 유지한다.
- 비즈니스 규칙은 서비스 클래스에 둔다.
- DB와 매핑되는 객체는 `entity`에 둔다.
- API 요청/응답 객체는 `dto`에 둔다.
- 운영 DB와 로컬 프론트 연동 DB는 MySQL을 기준으로 한다.
- H2는 빠른 JPA 단위 테스트 보조용으로만 사용한다.
- 테스트는 `src/test/java` 아래에 둔다.
- `src/main/java` 아래에는 `test` 패키지를 만들지 않는다.

## 아키텍처

기본 패키지 구조는 가장 대중적인 Spring Boot 계층형 구조를 따른다.

```text
com.example.shift
  controller
  dto
  entity
  repository
  service
  config
```

각 패키지의 책임은 다음과 같다.

- `controller`: HTTP 요청을 받는 진입점. `@RestController`와 `/api/v1/**` URI를 둔다.
- `dto`: API 요청/응답 전용 객체. 프론트엔드와 주고받는 JSON 계약이다.
- `entity`: JPA 엔티티와 엔티티에서 사용하는 enum 값. DB 저장 구조와 연결된다.
- `repository`: Spring Data JPA repository. DB 저장과 조회를 담당한다.
- `service`: 트랜잭션 경계와 비즈니스 규칙을 담당한다.
- `config`: 보안, CORS, 기타 Spring 설정을 담당한다.

이전 계획서의 용어는 다음처럼 바꿔서 이해한다.

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

현재 중요한 파일:

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

확인된 상태:

- 2026-05-29 기준 `.\mvnw.cmd -q test`는 컴파일 단계에서 실패한다.
- 실패 원인은 `EmployeeRepository`, `PositionRepository`, `TeamRepository`가 아직 없기 때문이다.
- 임시 MVC 컨트롤러였던 `src/main/java/com/example/shift/test/LoginSecurityTest.java`는 제거되었다.
- `entity` enum과 `Employee`, `Team`, `Position` 초안은 추가되었지만, repository/service/controller/dto 구현은 아직 진행 전이다.
- 현재 `Employee`가 단일 `Position`만 가지므로, 계획대로 여러 포지션을 지원하려면 `Set<Position>` 구조로 맞춰야 한다.

## DB 전략

운영 서버와 프론트엔드 연동 개발은 MySQL을 기준으로 한다.

- `local/dev` profile: MySQL을 사용한다. React/Vite PWA가 호출하는 실제 API는 이 profile로 검증한다.
- `test` profile: 빠른 JPA 단위 테스트에는 H2를 사용할 수 있다.
- 운영 전 통합 테스트: 반드시 MySQL 기준으로 실행한다.
- `prod` profile: MySQL을 사용하고, 개발용 CORS origin과 seed/test data가 남지 않도록 분리한다.

이 프로젝트는 날짜 범위, `LocalDate`, `LocalTime`, enum, 다대다 join table, unique/index 조건을 많이 사용한다. H2만 기준으로 개발하면 MySQL에서 DDL, 예약어, enum/string 저장, 날짜/시간 처리 차이를 늦게 발견할 수 있으므로, 6월 초부터 MySQL dev profile을 준비한다.

## 서버 환경 전략

개발 중 수시로 실제 서버에 올려 프론트와 API가 잘 붙는지 확인해야 하므로, 개발서버와 운영서버를 분리한다.

추천 환경:

```text
local
- 개인 PC 개발 환경
- 빠른 코드 수정과 단위 테스트
- MySQL dev 또는 H2 test 사용

development server
- 2026-06-05부터 상시 배포 대상
- 프론트 개발자가 실제 API를 호출하며 확인하는 환경
- 별도 MySQL dev DB 사용
- mock/minimal API skeleton도 배포 가능
- 데이터는 언제든 초기화될 수 있음

production server
- 2026-07-01 운영 오픈 대상
- 실제 사용자/운영 데이터
- 운영 MySQL DB 사용
- 검증된 버전만 배포
```

운영서버와 개발서버를 분리하는 이유:

- 개발 중 API가 깨져도 운영 사용자는 영향을 받지 않는다.
- 프론트가 실제 네트워크/CORS/배포 환경에서 API를 빨리 검증할 수 있다.
- MySQL, 환경변수, 도메인, CORS, HTTPS 같은 운영에 가까운 문제를 6월 초부터 발견할 수 있다.
- 테스트 데이터와 실제 운영 데이터를 섞지 않는다.

비교:

```text
개발/운영 서버 분리
- 장점: 안전함, 배포 연습 가능, 프론트 연동 확인 쉬움, 운영 데이터 보호
- 단점: 비용과 설정이 조금 늘어남
- 추천도: 가장 좋음

서버 하나만 사용
- 장점: 비용과 설정이 단순함
- 단점: 개발 배포가 운영에 영향을 줄 수 있음, 테스트 데이터와 운영 데이터 분리 어려움
- 추천도: 7/1 운영 오픈 목표에는 위험함

같은 VPS 안에서 논리적으로 분리
- 예: dev/prod 앱 프로세스, dev/prod DB, dev/prod 환경변수, dev/prod 도메인 분리
- 장점: 비용을 줄이면서 최소한의 안전장치 확보
- 단점: 서버 장애나 리소스 문제는 같이 영향을 받을 수 있음
- 추천도: 비용이 부담될 때의 현실적인 최소안
```

최종 추천은 개발서버와 운영서버를 물리적으로 분리하는 것이다. 예산이나 관리 부담 때문에 어렵다면, 최소한 같은 서버 안에서도 dev/prod 프로세스와 DB를 분리한다.

## 병행 개발 일정

운영 오픈 목표는 2026-07-01이다. 백엔드 전체 완성 후 프론트를 시작하면 일정 리스크가 크므로, API 계약을 먼저 고정하고 백엔드와 프론트를 병행한다.

```text
2026-05-29 ~ 2026-06-02
- 현재 컴파일 실패 복구
- Employee/Team/Position 모델과 repository 정리
- MySQL dev profile 추가
- 화면별 API 목록과 DTO 계약 확정

2026-06-03 ~ 2026-06-05
- /api/v1 controller + dto skeleton 구현
- mock 또는 minimal 응답으로 프론트 호출 가능 상태 만들기
- 개발서버 첫 배포
- 개발서버에서 CORS, MySQL 연결, 기본 API smoke test 확인
- 프론트 프로젝트에서 API client와 기본 화면 개발 시작

2026-06-08 ~ 2026-06-12
- Schedule, Employee, eligible substitutes 실제 구현
- Home/Schedule 화면과 실제 API 연동
- 개발서버에 최소 하루 1회 배포하고 프론트 연동 확인

2026-06-15 ~ 2026-06-19
- coverage/absence/leave request와 manager queue 구현
- My Requests와 Manager Queue 화면 연동
- 개발서버에서 요청 생성/승인 흐름 end-to-end 확인

2026-06-22 ~ 2026-06-24
- open shift 신청/배정, 승인/거절 흐름 구현
- MVP 핵심 기능 완성
- 개발서버를 release candidate 환경처럼 사용

2026-06-25 ~ 2026-06-26
- API 오류 응답, CORS, 테스트 보강
- 로딩/빈 상태/에러 상태와 통합 QA
- 운영서버 배포 절차 리허설

2026-06-29 ~ 2026-06-30
- MySQL 기준 운영 profile/env/배포 리허설
- 모바일 브라우저 smoke test

2026-07-01
- 운영 서버 오픈
```

핵심 마일스톤은 2026-06-05까지 프론트가 호출할 수 있는 API skeleton을 여는 것이다. 내부 비즈니스 로직은 이후 실제 구현으로 교체하되, request/response DTO와 endpoint 이름은 최대한 안정적으로 유지한다.

## 포함 범위

이번 백엔드 foundation 계획에 포함되는 것:

- Employee, Team, Position 모델
- 직원의 여러 팀 소속
- 같은 팀 기반의 대체 근무 가능 여부 판단
- 고정 주간 스케줄 패턴
- 실제 날짜 기반 Shift
- 대체자를 지정한 coverage request
- 대체자 없는 absence request
- planned leave request
- 승인된 leave 기간의 assigned shift를 open shift로 전환
- open shift application
- manager direct assignment
- `/api/v1/**` REST API
- 프론트 병행 개발을 위한 API 계약과 skeleton endpoint
- 별도 React/Vite PWA와 통신하기 위한 CORS 설정
- MySQL 기반 local/dev profile
- H2 기반 빠른 JPA 테스트 profile
- 미래 React Native 클라이언트가 재사용할 수 있는 DTO 기반 API 계약

이번 계획에서 제외하는 것:

- 프론트엔드 화면 구현
- React Native 앱 구현
- 앱스토어/플레이스토어 배포
- push notification, biometric login, offline sync
- production authentication/invitation flow
- tenant/company/location 모델
- payroll/overtime 계산
- notification delivery
- published schedule period

## 프로젝트 어휘

다음 enum 이름을 일관되게 사용한다.

```java
public enum EmployeeRole {
    EMPLOYEE,
    MANAGER,
    OWNER
}

public enum ShiftStatus {
    SCHEDULED,
    OPEN,
    CANCELLED,
    COVERED,
    NEEDS_COVERAGE
}

public enum ShiftCreationReason {
    PATTERN,
    ABSENCE_REQUEST,
    LEAVE_REQUEST,
    EXTRA_STAFFING,
    NEW_POSITION_COVERAGE,
    MANUAL
}

public enum RequestStatus {
    PENDING,
    APPROVED,
    DECLINED,
    CANCELLED
}

public enum CoverageRequestType {
    SUBSTITUTE_REQUESTED,
    ABSENCE_WITHOUT_SUBSTITUTE
}

public enum SchedulePatternType {
    FIXED_WEEKLY
}
```

`SchedulePatternType.FIXED_WEEKLY`는 매주 같은 고정 스케줄을 쓰는 식당을 위한 반복 템플릿 유형이다. 예를 들어 매주 월/화 full, 수요일 evening, 목/토 off처럼 같은 패턴이 반복되는 운영을 표현한다.

차후 매니저가 1주, 2주, 3주, 4주 또는 커스텀 기간의 실제 스케줄을 직접 만들고 게시하는 기능은 `SchedulePatternType`을 늘리는 방식이 아니라 future `PublishedSchedulePeriod` 모델로 다룬다. 이때 고정 weekly pattern은 선택한 기간의 draft shift를 빠르게 만드는 템플릿으로 사용할 수 있고, 매니저는 생성된 shift를 직접 수정한 뒤 게시한다.

`SchedulePatternType`은 반복 규칙 자체가 달라질 때만 확장한다. 예를 들어 A주/B주가 자동 반복되는 요구가 생기면 `ROTATING_MULTI_WEEK` 같은 값을 추가할 수 있다.

## Task 1: Maven Wrapper 기준 확인과 임시 컨트롤러 제거

상태:

- `.\mvnw.cmd -q test` 통과 확인 완료
- `LoginSecurityTest.java` 제거 완료
- `src/main/java` 아래 `test` 패키지 없음 확인 완료

확인 명령:

```powershell
.\mvnw.cmd -q test
Get-ChildItem -Recurse src/main/java -File | Where-Object { $_.FullName -match "\\test\\" }
```

학습 포인트:

- `src/test/java`는 테스트 코드 위치이다.
- `src/main/java` 아래 코드는 실제 애플리케이션 코드로 포함된다.
- 이름이 `Test`여도 `src/main/java`에 있고 `@Controller`가 붙어 있으면 실제 서버 코드이다.

## Task 2: Entity Enum 추가

목표:

`entity` 패키지를 만들고, 앞으로 엔티티에서 사용할 enum을 추가한다.

생성할 파일:

```text
src/main/java/com/example/shift/entity/EmployeeRole.java
src/main/java/com/example/shift/entity/ShiftStatus.java
src/main/java/com/example/shift/entity/ShiftCreationReason.java
src/main/java/com/example/shift/entity/RequestStatus.java
src/main/java/com/example/shift/entity/CoverageRequestType.java
src/main/java/com/example/shift/entity/SchedulePatternType.java
```

진행 순서:

1. `src/main/java/com/example/shift/entity` 패키지를 만든다.
2. `EmployeeRole` enum 하나를 먼저 만든다.
3. `.\mvnw.cmd -q test`를 실행해 통과를 확인한다.
4. 나머지 enum을 같은 방식으로 추가한다.
5. 전체 테스트를 다시 실행한다.

학습 포인트:

- enum은 정해진 선택지 목록을 타입으로 표현하는 Java 문법이다.
- 문자열보다 오타와 잘못된 값을 줄일 수 있다.
- JPA 엔티티 필드로 저장될 값이므로 이번 프로젝트에서는 `entity` 패키지에 둔다.

## Task 3: Staff Entity와 Repository 추가

목표:

직원, 팀, 포지션을 DB에 저장할 수 있게 만든다.

모델링 메모:

- `Team`은 대체 근무 가능 여부를 판단하기 위한 운영 그룹이다. 예: Sushi Bar, Kitchen, Hall, Dinner Service.
- `Position`은 팀 안에서 맡는 직무이다. 예: Chef, Kitchen Helper, Roll Man, Sushi Man, Server, Cashier.
- `Employee`는 연락처를 하나의 필드로 뭉치지 않고 `email`과 `phoneNumber`를 분리한다.
- `Employee`는 하나의 `position`이 아니라 여러 `positions`를 가진다. MVP에서는 `Set<Position>`으로 단순하게 표현하고, 차후 포지션별 시급, 자격 상태, 적용 기간이 필요해지면 `EmployeePosition` 엔티티로 확장한다.
- 오전, 오후, 미들, 마감, 풀타임, 커스텀 시간은 포지션이 아니다. MVP에서는 Shift에 실제 시작/종료 시간을 직접 저장하고, 차후 `ShiftTemplate`으로 매장별 시간 프리셋을 제공한다.

생성할 파일:

```text
src/main/java/com/example/shift/entity/Position.java
src/main/java/com/example/shift/entity/Team.java
src/main/java/com/example/shift/entity/Employee.java
src/main/java/com/example/shift/repository/PositionRepository.java
src/main/java/com/example/shift/repository/TeamRepository.java
src/main/java/com/example/shift/repository/EmployeeRepository.java
src/test/java/com/example/shift/service/EmployeeServiceTest.java
```

TDD 순서:

1. `EmployeeServiceTest`에 “직원이 여러 팀에 속할 수 있다”는 실패 테스트를 먼저 만든다.
2. `.\mvnw.cmd -q -Dtest=EmployeeServiceTest test`를 실행해 실패를 확인한다.
3. `Position`, `Team`, `Employee` 엔티티를 만든다.
4. 각 Repository를 만든다.
5. 같은 테스트가 통과하는지 확인한다.

학습 포인트:

- `@Entity`는 DB 테이블과 매핑되는 객체이다.
- `Repository`는 DB 접근을 담당한다.
- 다대다 관계 또는 조인 테이블이 필요한 지점을 학습한다.

## Task 4: EmployeeService와 대체 근무 가능 여부

목표:

같은 팀에 속한 active 직원만 대체 근무자로 추천되도록 한다.

규칙:

- 대체 근무자는 active 상태여야 한다.
- 요청자와 최소 하나 이상의 팀을 공유해야 한다.
- 자기 자신은 대체 근무자가 될 수 없다.

학습 포인트:

- 컨트롤러가 아니라 서비스에 비즈니스 규칙을 둔다.
- 엔티티에는 작은 도메인 메서드, 예를 들어 `sharesTeamWith(Employee other)`를 둘 수 있다.

## Task 5: Shift와 Fixed Weekly Schedule Pattern 추가

목표:

고정 주간 패턴과 실제 날짜의 Shift를 표현한다.

주요 엔티티:

```text
Shift
SchedulePattern
SchedulePatternShift
```

학습 포인트:

- “반복되는 패턴”과 “실제 날짜의 근무”를 분리한다.
- `LocalDate`, `LocalTime`, enum 상태값을 사용한다.
- MVP의 `SchedulePatternType.FIXED_WEEKLY`는 고정 스케줄 식당을 위한 템플릿이다.
- 차후 1주/2주/3주/4주/커스텀 기간의 매니저 직접 편집 스케줄은 `PublishedSchedulePeriod`로 확장한다.
- `Shift`는 예정 근무 시간이다. 실제 출근/퇴근, 조퇴, 지각, 휴게 시간, payroll export는 차후 `TimeClockRecord` 또는 `ActualWorkRecord`로 분리한다.

## Task 6: ScheduleService Shift 생성

목표:

고정 주간 패턴을 기준으로 날짜 범위 안의 Shift를 생성한다.

학습 포인트:

- 서비스가 비즈니스 동작의 중심이 된다.
- 같은 날짜 범위에 대해 중복 Shift가 생기지 않도록 테스트한다.

## Task 7: Coverage Request 흐름 추가

목표:

직원이 특정 Shift에 대해 대체 요청 또는 결근 요청을 만들 수 있게 한다.

주요 규칙:

- 대체 요청은 요청자, Shift, 대체자, 사유를 가진다.
- 대체자는 요청자와 팀을 공유해야 한다.
- 결근 요청은 대체자 없이 생성될 수 있다.
- 요청 상태는 `PENDING`, `APPROVED`, `DECLINED`, `CANCELLED` 중 하나이다.

학습 포인트:

- 요청 상태 관리를 enum으로 표현한다.
- 승인/거절 같은 상태 변경 규칙은 서비스에 둔다.

## Task 8: Planned Leave 흐름 추가

목표:

직원이 날짜 범위 기반의 leave request를 만들 수 있게 한다.

주요 규칙:

- leave는 시작일과 종료일을 가진다.
- 승인된 leave 기간에 직원이 배정된 Shift가 있으면 open shift로 전환한다.

학습 포인트:

- 날짜 범위 조건을 테스트한다.
- 하나의 승인 동작이 여러 Shift 상태를 바꾸는 흐름을 학습한다.

## Task 9: Open Shift Application과 Assignment 추가

목표:

open shift에 직원이 신청하고, 매니저가 직접 배정할 수 있게 한다.

주요 규칙:

- open shift에만 신청할 수 있다.
- inactive 직원은 배정할 수 없다.
- 승인된 leave 중인 직원은 해당 날짜 Shift에 배정할 수 없다.
- 배정되면 Shift 상태를 `SCHEDULED`로 바꾼다.

학습 포인트:

- 상태 전이를 테스트로 먼저 고정한다.
- 서비스가 여러 Repository를 조합하는 방식을 익힌다.

## Task 10: Versioned REST API, CORS, MVP Security

목표:

프론트엔드와 통신할 `/api/v1/**` REST API를 만든다.

이번 Task는 전체 비즈니스 로직이 끝난 뒤에만 시작하지 않는다. 2026-06-05까지 프론트 개발을 시작할 수 있도록 controller와 dto skeleton을 먼저 만들고, mock 또는 minimal 응답으로 API 계약을 열어둔다. 이후 Task 5~9의 실제 서비스 구현이 완성될 때 skeleton 내부를 실제 service 호출로 교체한다.

생성할 주요 파일:

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

API 예시:

```text
GET  /api/v1/employees/{id}/eligible-substitutes
GET  /api/v1/schedule?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD
POST /api/v1/coverage-requests/substitute
POST /api/v1/coverage-requests/absence
POST /api/v1/leave-requests
POST /api/v1/open-shifts/{shiftId}/applications
POST /api/v1/open-shifts/{shiftId}/assignments
```

DTO 이름 예시:

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

DB profile 기준:

```yaml
# application-dev.yml
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

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:managing_shift_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
  jpa:
    hibernate:
      ddl-auto: create-drop
```

학습 포인트:

- Entity를 API 응답으로 직접 노출하지 않는다.
- Controller는 DTO를 받고 DTO를 반환한다.
- Controller는 Service를 호출만 하고 비즈니스 규칙을 직접 처리하지 않는다.
- CORS는 별도 React/Vite PWA와 통신하기 위해 필요하다.

## 테스트 원칙

새 기능은 가능한 한 다음 순서로 진행한다.

1. 실패하는 테스트를 먼저 작성한다.
2. 테스트가 기대한 이유로 실패하는지 확인한다.
3. 최소한의 구현으로 테스트를 통과시킨다.
4. 전체 테스트를 다시 실행한다.
5. 필요하면 리팩터링한다.

기본 명령:

```powershell
.\mvnw.cmd -q test
```

MySQL dev profile로 앱 실행:

```powershell
$env:SPRING_PROFILES_ACTIVE="dev"
.\mvnw.cmd spring-boot:run
```

운영 전에는 H2 테스트 통과만으로 완료 처리하지 않는다. 주요 API smoke test와 프론트 연동 테스트는 MySQL dev 또는 운영과 같은 MySQL staging 환경에서 확인한다.

특정 테스트만 실행:

```powershell
.\mvnw.cmd -q -Dtest=EmployeeServiceTest test
```

## 실행 메모

이 프로젝트는 학습 모드로 진행한다.

- 먼저 목표를 말로 설명한다.
- 관련 파일과 개념을 확인한다.
- 사용자가 작은 단계를 직접 시도한다.
- 결과를 함께 확인한다.
- 막히면 힌트를 먼저 주고, 필요한 경우에만 최소 코드 예시를 제공한다.

## Self-Review

이 계획은 다음 요구를 다룬다.

- 직원, 포지션, 팀, 여러 팀 소속
- 같은 팀 기반 대체 가능성
- 고정 주간 스케줄 패턴
- 실제 날짜 기반 Shift
- 대체 요청과 결근 요청
- planned leave와 Shift open 전환
- open shift 신청과 직접 배정
- manager/owner가 사용할 수 있는 백엔드 API 표면
- React/Vite PWA와의 CORS 통신
- 미래 React Native 클라이언트가 재사용할 수 있는 `/api/v1/**` API 계약

프론트엔드 화면 구현은 이 계획의 범위가 아니다.
