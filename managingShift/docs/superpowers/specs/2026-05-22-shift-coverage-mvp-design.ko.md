# Shift Coverage MVP 설계

## 개요

이 문서는 단일 레스토랑 매장을 위한 가벼운 스케줄 및 직원 관리 MVP를 정의한다. 첫 사용 사례는 캐나다의 스시 레스토랑처럼 직원들이 고정 또는 반복 스케줄로 일하지만, 가끔 근무를 대신할 사람이 필요한 상황이다.

MVP의 초점은 **shift coverage**, 즉 빠진 근무를 놓치지 않고 직원이 대체근무를 요청하며 매니저가 빈 근무를 빠르게 채우는 흐름이다.

첫 버전은 한 레스토랑, 한 지점을 전제로 한다. 데이터 모델은 향후 여러 회사와 여러 지점으로 확장할 여지를 남기되, 멀티 지점 관리는 MVP 범위에 포함하지 않는다.

## 클라이언트 및 플랫폼 전략

백엔드는 API-first 서비스로 설계한다. 첫 프론트엔드 클라이언트는 별도 React/Vite 모바일 우선 PWA이며, 백엔드 API는 이 웹 클라이언트에 종속되지 않아야 한다.

장기적으로는 앱스토어 배포나 더 깊은 네이티브 기능이 필요할 때 React Native 앱을 지원할 수 있다. PWA는 MVP 검증을 빠르게 하기 위한 첫 클라이언트이고, React Native는 같은 백엔드 API를 재사용하는 이후 모바일 클라이언트가 될 수 있다.

클라이언트 우선순위:

1. 모바일 우선 React/Vite PWA.
2. 데스크톱에서도 사용할 수 있는 반응형 웹.
3. 향후 앱스토어 배포와 네이티브 기능을 위한 React Native 앱.

따라서 백엔드 API는 웹과 네이티브 모바일 모두에서 사용할 수 있는 안정적인 JSON DTO를 반환해야 한다. HTML이나 특정 프론트엔드 구현에만 맞는 응답은 피한다.

개발 일정은 2026-07-01 운영 시작을 기준으로 하되, 기능 개발 완료 목표는 2026-06-20으로 둔다. 프론트엔드는 백엔드 전체 구현을 기다리지 않고 2026-06-03에 별도 React/Vite 프로젝트로 시작하며, 초기에는 `/api/v1/**` skeleton endpoint와 mock/minimal response를 이용해 화면과 API client를 병행 개발한다.

## 제품 범위

### MVP 포함 범위

- 직원 홈 화면에서 현재 시점 기준 다음 3일 스케줄 표시.
- 직원용 3일, 주간, 월간 스케줄 조회.
- 직원이 자신의 근무에 대해 대체근무자를 지정한 coverage request 생성.
- 대체근무자는 요청자와 최소 한 팀을 공유하는 직원만 선택 가능.
- 직원은 여러 팀에 속할 수 있음.
- 대체근무자 없이 결근 또는 휴가 요청 가능.
- 2주, 3주, 4주 같은 계획 휴가 요청 가능.
- 승인된 휴가 기간에는 해당 직원을 스케줄 생성 대상에서 제외하고, 이미 배정된 근무는 open shift 또는 needs-coverage 상태로 전환.
- 매니저와 오너가 모든 스케줄 변경 요청을 승인 또는 거절.
- 매니저와 오너가 open shift를 직접 생성.
- 매니저와 오너가 open shift에 직원을 배정.
- open shift는 결근, 추가 인력 필요, 새 포지션 커버, 수동 생성 등의 이유로 생성 가능.
- MVP에서는 고정 주간 스케줄 패턴 지원.
- 향후 매니저가 1주, 2주, 3주, 1개월 또는 사용자 지정 기간의 published schedule period를 만들 수 있도록 모델 확장 여지 유지.
- 승인 전 매니저 전용 경고: 스케줄 충돌, 포지션 불일치, 주간 근무시간 증가, 초과근무 위험.
- 향후 급여, 초과근무 계산, 실제 근무 기록 확장을 방해하지 않는 데이터 모델.
- 첫 PWA와 향후 React Native 클라이언트가 사용할 API-first 백엔드 endpoint.
- 개발 중 별도 React/Vite 프론트엔드와 통신하기 위한 CORS 지원.
- 모바일 클라이언트에 적합한 schedule, request status, manager queue, open shift 응답 형태.

### MVP 제외 범위

- 출퇴근 기록.
- 자동 급여 계산.
- 초과근무 정산 또는 급여 export.
- 직원 측 substitute acceptance flow.
- 멀티 지점 관리.
- 주/도별 노동법 자동화.
- 월간 화면에서 직접 편집.
- React Native 앱 구현.
- App Store 또는 Play Store 제출.
- push notification, camera, geolocation, biometric login, offline sync 같은 네이티브 기능.

## 사용자와 역할

### Employee

직원은 다음을 할 수 있다.

- 다음 3일의 자신의 근무 조회.
- 주간 및 월간 스케줄 조회.
- 자신의 근무에 대해 coverage request 생성.
- 이미 대체자를 구한 경우 substitute employee 지정.
- 대체자 없이 결근 또는 휴가 요청.
- 일정 기간의 planned leave 요청.
- open shift 신청.
- 자신의 요청 상태 조회.

직원은 다음을 할 수 없다.

- 스케줄 변경 승인.
- 공식 open shift 생성.
- 다른 직원 배정.
- 스케줄 패턴 편집.
- 매니저 전용 초과근무 또는 비용 위험 경고 조회.

### Manager

매니저는 직원 기능에 더해 다음을 할 수 있다.

- coverage request 승인 또는 거절.
- substitute 없는 absence request 검토.
- planned leave request 승인 또는 거절.
- open shift 생성.
- open shift에 직원 배정.
- fixed weekly schedule pattern 관리.
- 향후 custom date range의 published schedule period 생성.
- 승인 전 conflict, position, hours warning 확인.
- 초과근무 위험 경고 확인.

### Owner/Admin

오너와 관리자는 매니저 기능에 더해 다음을 할 수 있다.

- 직원 추가 및 비활성화.
- 직원 역할 관리.
- 팀과 직원 팀 멤버십 관리.
- 포지션 관리.
- 향후 급여 및 초과근무 설정 관리.

## 핵심 화면

### Home

직원 홈은 직원의 시작 화면이다.

직원에게는 다음을 보여준다.

- 다음 3일의 근무.
- pending, approved, declined, cancelled 상태의 요청.
- 신청 가능한 open shift.

매니저와 오너에게는 추가로 다음을 보여준다.

- pending approval count.
- 오늘과 가까운 날짜의 open shift.
- 긴급 uncovered shift.
- 확인이 필요한 schedule warning.

### Schedule

스케줄 화면은 세 가지 view를 가진다.

- 3 Days
- Weekly
- Monthly

3-day view는 가까운 운영 상황을 빠르게 보는 화면이다. Weekly view는 날짜, 시간, 포지션별로 누가 일하는지 보여주는 주된 읽기 전용 운영 화면이다. Monthly view는 읽기 전용 overview이며, 특정 날짜를 선택하면 day 또는 week detail로 이동할 수 있지만 MVP에서는 월간 화면에서 직접 편집하지 않는다.

### My Requests

직원은 이 화면에서 요청을 만들고 추적한다.

요청 종류:

- substitute를 지정한 coverage request.
- substitute 없는 absence 또는 leave request.
- date range 기반 planned leave request.

상태:

- Pending
- Approved
- Declined
- Cancelled

### Manager Queue

매니저와 오너가 승인해야 할 일을 보는 화면이다.

queue item:

- substitute가 있는 coverage request.
- substitute 없는 absence/leave request.
- planned leave request.
- open shift application.
- 아직 배정되지 않은 manager-created open shift.

승인 전에는 다음 경고를 보여준다.

- substitute schedule conflict 여부.
- substitute position match 여부.
- 승인 후 shift가 uncovered로 남는지 여부.
- approved leave가 open shift를 만드는지 여부.
- 주간 근무시간 증가 여부.
- 초과근무 위험 여부.

경고는 정보를 제공하지만 MVP에서는 자동으로 승인을 막지 않는다.

### Staff and Patterns

매니저와 오너가 다음을 관리하는 화면이다.

- Employees.
- Teams and employee team memberships.
- Positions.
- Roles.
- Fixed weekly schedule patterns.
- 향후 published schedule periods.

### MVP 직원 생성

스케줄 커버리지가 실제 레스토랑에서 동작하려면 매니저가 직원을 추가할 수 있어야 한다.

직원 생성 필드:

- Name
- Email
- Phone number
- Role: employee, manager, owner
- Positions: 여러 직무 가능
- Team memberships: 여러 팀 가능
- Active/inactive status
- Optional fixed weekly schedule pattern

다음은 MVP에서 제외한다.

- Email invitation flow
- Profile photos
- Detailed employment records
- Per-position pay rate and payroll profile
- Visa/document/compliance management
- Hire date and termination workflow
- Fine-grained permissions

## Schedule Pattern과 Published Schedule 설계

MVP는 fixed weekly pattern을 지원한다. 자동 biweekly 반복을 MVP의 핵심 패턴으로 보지 않는다.

### Fixed Weekly Pattern

매주 같은 근무가 반복되는 패턴이다.

예:

- Monday AM
- Tuesday PM
- Friday Full

### Future PublishedSchedulePeriod

향후에는 자동 반복만이 아니라 매니저가 선택한 기간의 schedule period를 만들고 publish하는 모델을 지원한다. 이는 레스토랑이나 카페가 다음 1주 또는 2주의 실제 스케줄을 만들고 직원에게 공개하는 운영 방식에 더 잘 맞는다.

매니저와 오너는 다음 기간의 schedule period를 만들 수 있다.

- 1 week
- 2 weeks
- 3 weeks
- 4 weeks
- 1 month
- custom start/end date

MVP에서 `SchedulePattern`은 fixed weekly schedule을 위한 기본 템플릿이다. 실제 날짜의 근무는 `Shift`가 나타낸다. 향후 `PublishedSchedulePeriod`는 draft, published, archived 같은 상태를 가지는 별도 모델로 확장한다.

## 데이터 모델

### Employee

직원을 나타낸다.

필드:

- Name
- Email
- Phone number
- Positions
- Teams
- Role
- Active status

MVP에서는 직원의 가능한 직무를 `Employee.positions`로 단순하게 표현한다. 향후 포지션별 시급, 자격 상태, 기간별 변경이 필요해지면 `EmployeePosition` 엔티티로 확장한다.

### Team

대체근무자 선택 범위를 제한하기 위한 운영 그룹이다.

예:

- Sushi team
- Server team
- Kitchen team
- Dinner service team

직원은 여러 팀에 속할 수 있다. coverage request에서 substitute picker는 요청자와 최소 한 팀을 공유하는 직원만 보여준다.

### Position

근무에서 필요한 직무를 나타낸다.

예:

- Chef
- Kitchen helper
- Roll man
- Sushi man
- Server
- Cashier

포지션은 시간대 라벨이 아니다. Opening, closing, morning, afternoon, middle, full shift 같은 값은 포지션으로 모델링하지 않는다.

### Future ShiftTemplate

향후 매장별 근무 시간 preset을 나타낸다. MVP 범위는 아니다.

예:

- Morning: 09:00-14:00
- Afternoon: 14:00-18:00
- Middle: 11:00-16:00
- Closing: 17:00-22:00
- Full: 09:00-22:00
- Custom

MVP의 `Shift`는 실제 `startTime`, `endTime`을 직접 저장한다.

### SchedulePattern

직원의 반복 스케줄이다.

필드:

- Employee
- Pattern type: fixed_weekly
- Weekly shifts

패턴은 기본 규칙이지 최종 스케줄이 아니다.

### PublishedSchedulePeriod

향후 매니저가 특정 기간의 실제 스케줄 묶음을 만들고 publish하는 모델이다. MVP 구현 대상은 아니지만 모델 확장을 고려한다.

필드 예:

- Location
- Start date
- End date
- Status: draft, published, archived
- Created by
- Published at
- Notes

### Shift

특정 날짜의 실제 예정 근무다.

필드:

- Date
- Start time
- End time
- Position
- Assigned employee, optional
- Status: scheduled, open, cancelled, covered
- Creation reason

`Shift`는 예정 근무이지 실제 출퇴근 기록이 아니다. 실제 근무 기록, 조퇴, 지각, 휴게시간, 급여 export는 향후 `TimeClockRecord` 또는 `ActualWorkRecord`로 분리한다.

### CoverageRequest

직원이 일할 수 없는 근무에 대해 만드는 요청이다.

필드:

- Requesting employee
- Target shift
- Substitute employee, optional
- Reason, optional
- Request type
- Status

MVP에서는 substitute 직원이 앱 안에서 별도로 수락하는 흐름은 없다. 매니저 또는 오너가 최종 승인한다.

### LeaveRequest

2주, 3주, 4주 같은 기간 휴가 요청이다.

필드:

- Employee
- Start date
- End date
- Reason, optional
- Status
- Approved by, optional
- Approved at, optional

승인되면 해당 직원은 기간 내 스케줄 생성에서 제외되고, 이미 배정된 shift는 open 또는 needs-coverage로 전환된다.

### OpenShiftApplication

직원이 open shift에 신청한 기록이다.

필드:

- Open shift
- Applying employee
- Status

## 상태 흐름

### Coverage Request With Substitute

1. 직원이 자신의 shift를 선택한다.
2. 같은 팀의 substitute를 지정해 coverage request를 만든다.
3. 매니저가 요청을 검토한다.
4. 매니저는 conflict, position, hours warning을 본다.
5. 승인하면 shift assignment가 기존 직원에서 substitute로 바뀐다.
6. 거절하면 원래 직원 배정이 유지된다.

### Absence Request Without Substitute

1. 직원이 자신의 shift를 선택한다.
2. substitute 없이 absence 또는 leave request를 만든다.
3. 매니저가 검토한다.
4. 승인하면 원래 shift가 open shift가 된다.
5. 이후 매니저가 직접 배정하거나 직원 신청을 승인한다.

### Planned Leave Request

1. 직원이 start date와 end date로 leave request를 만든다.
2. 매니저 또는 오너가 기간을 검토한다.
3. 거절하면 해당 기간에도 직원은 스케줄 대상에 남는다.
4. 승인하면 해당 기간에 직원은 스케줄에서 제외된다.
5. 이미 배정된 shift는 open 또는 needs-coverage가 된다.
6. 다른 직원이 신청할 수 있다.
7. 매니저 또는 오너가 신청을 승인하거나 직접 배정한다.

### Manager-Created Open Shift

1. 매니저가 open shift를 만든다.
2. 날짜, 시간, 포지션, 이유를 선택한다.
3. 직원이 신청하거나 매니저가 직접 배정한다.
4. 배정되면 shift는 scheduled가 된다.

## 초과근무와 급여 확장

MVP는 급여 계산이나 초과근무 정산을 하지 않는다. 다만 매니저 전용으로 근무시간 증가와 초과근무 위험 경고를 보여줄 수 있다.

향후 모델:

- `TimeClockRecord` 또는 `ActualWorkRecord`
- `PayrollPeriod`
- `EmployeePosition`
- `OvertimeRule`
- `PayrollSummary`

중요 원칙은 예정 스케줄, 변경 요청, 실제 근무 기록, 급여 요약을 하나의 모델에 섞지 않는 것이다.

## 인증, 테넌트, 초대 확장

Authentication, tenant management, email invitation, password reset, multi-location access control은 향후 확장이다. MVP는 가볍게 유지하되 향후 `Company`, `Location`, `UserAccount`, `Employee` 분리를 방해하지 않도록 한다.

향후 모델:

- `Company` 또는 `Tenant`
- `Location`
- `UserAccount`
- `Invitation`
- Multi-location access control

실제 운영 보안에서는 이메일로 raw password를 보내는 방식은 피하고, 초대 링크를 통해 사용자가 직접 비밀번호를 설정하게 하는 방식을 선호한다.

## 알림

MVP 알림은 앱 내부 상태 알림으로 단순하게 시작한다.

이벤트:

- 직원이 요청을 만들면 매니저에게 알림.
- 매니저가 승인/거절하면 직원에게 알림.
- 매니저가 open shift를 만들면 관련 직원에게 알림.
- 매니저가 open shift에 배정하면 배정 직원에게 알림.
- 긴급 open shift는 manager home에 표시.

SMS, email, push notification은 이후 추가한다.

## 승인 경고

승인 전 매니저에게 보여줄 경고:

- Substitute has a time conflict.
- Substitute has a different position.
- Shift remains uncovered.
- Approved leave creates shifts that need coverage.
- Open shift within next 3 days is still unassigned.
- Approval increases expected weekly hours.
- Possible overtime risk.

MVP에서는 경고가 승인을 자동으로 막지 않는다.

## 테스트 기준

MVP는 다음 흐름이 동작하면 기능적으로 충분하다고 본다.

- 직원이 substitute가 있는 coverage request를 만들고 manager queue에 나타난다.
- 직원은 같은 팀이 아닌 substitute를 선택할 수 없다.
- 직원은 최소 한 팀을 공유하는 substitute를 선택할 수 있다.
- 매니저가 substitute request를 승인하면 shift assignment가 바뀐다.
- 매니저가 decline하면 원래 assignment가 유지된다.
- 직원이 substitute 없는 absence request를 만들 수 있다.
- 매니저가 absence request를 승인하면 shift가 open이 된다.
- 직원이 date range planned leave request를 만들 수 있다.
- 매니저가 planned leave를 승인하면 해당 기간 스케줄에서 제외된다.
- 승인된 leave 기간의 기존 shift는 open 또는 needs-coverage가 된다.
- 매니저가 open shift를 수동 생성할 수 있다.
- 직원이 open shift에 신청할 수 있다.
- 매니저가 open shift application을 승인하면 shift가 배정된다.
- 매니저가 직접 open shift에 직원을 배정할 수 있다.
- fixed weekly pattern이 날짜 범위에 맞는 shift를 생성한다.
- 직원 home은 다음 3일 스케줄을 보여준다.
- weekly view는 해당 주의 근무자를 보여준다.
- monthly view는 해당 월의 근무자를 보여준다.
- 직원은 승인 또는 배정을 할 수 없다.
- manager/owner는 승인과 배정을 할 수 있다.
- manager-only warning은 일반 직원에게 보이지 않는다.
- React/Vite PWA가 `/api/**` JSON endpoint를 호출할 수 있다.
- 향후 React Native 클라이언트가 같은 API contract를 재사용할 수 있다.

## 초기 구현 추천 순서

MVP는 payroll이나 full scheduling suite가 아니라 shift coverage 중심으로 만든다.

추천 순서:

1. Employees, roles, teams, positions.
2. Fixed weekly schedule patterns.
3. Shift generation and read-only schedule views.
4. Coverage, absence, planned leave requests.
5. Manager approval queue.
6. Open shift creation, application, assignment.
7. Manager-only warnings.
8. Basic in-app notifications.
