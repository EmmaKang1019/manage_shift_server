# Shift Coverage MVP 설계 문서

## 클라이언트 및 플랫폼 전략 업데이트

MVP는 모바일을 1차 사용 환경으로 하는 API-first 제품으로 설계한다. 첫 번째 클라이언트는 별도 디렉터리에서 개발하는 React/Vite PWA이고, 데스크톱 웹은 보조 사용 환경이다.

장기적으로 앱스토어 배포나 네이티브 기능이 중요해지면 React Native 앱을 두 번째 모바일 클라이언트로 만든다. 이때 백엔드 도메인 로직을 다시 작성하지 않도록 Spring Boot 백엔드는 PWA 전용이 아니라 stable JSON API 서버로 만든다.

우선순위:

1. React/Vite mobile-first PWA
2. 데스크톱에서도 사용 가능한 responsive web
3. 미래 React Native 앱

백엔드 API는 `/api/v1/**`처럼 versioned endpoint를 사용하고, request/response DTO는 PWA와 React Native가 모두 이해할 수 있는 모바일 클라이언트 중심 계약으로 설계한다. HTML이나 특정 웹 화면에 종속된 response는 피한다.

## 개요

이 문서는 한 식당, 한 지점에서 사용할 가벼운 스케줄 및 직원 관리 애플리케이션의 MVP 설계를 정의한다. 첫 대상은 캐나다의 식당, 예를 들어 스시집처럼 직원에게 고정 또는 반복 스케줄이 주어지고, 직원 사정에 따라 대체 근무가 필요한 환경이다.

MVP의 핵심은 shift coverage이다. 즉, 빈 스케줄이 방치되지 않게 하고, 직원이 대체 근무 또는 결근/휴가 요청을 쉽게 올리며, 매니저가 빠르게 빈자리를 채우는 것이다.

첫 버전은 한 식당, 한 지점용으로 만든다. 다만 데이터 구조는 나중에 여러 회사와 여러 지점으로 확장할 수 있도록 여지를 남긴다. 여러 지점 관리는 MVP 범위에 포함하지 않는다.

## 제품 범위

### MVP 포함 범위

- 직원 홈 화면에서 현재 시점 기준 앞으로 3일 스케줄을 보여준다.
- 직원과 매니저가 3일, 주간, 월간 스케줄을 볼 수 있다.
- 주간/월간 화면은 편집 도구가 아니라 누가 언제 일하는지 확인하는 읽기 전용 화면이다.
- 직원은 자기 shift에 대해 대체자를 지정한 coverage request를 만들 수 있다.
- 직원이 대체자를 선택할 때는 같은 팀에 속한 직원만 선택할 수 있다.
- 한 직원은 여러 팀에 동시에 포함될 수 있다.
- 직원은 대체자 없이 결근 또는 휴가 요청을 만들 수 있다.
- 직원은 2주, 3주, 4주처럼 여러 날짜 또는 여러 주에 걸친 장기휴가를 신청할 수 있다.
- 승인된 휴가 기간에는 해당 직원이 스케줄 배정 대상에서 제외되고, 이미 배정된 shift는 open 또는 needs coverage 상태가 된다.
- 매니저와 오너는 모든 스케줄 변경 요청을 승인하거나 거절할 수 있다.
- 매니저와 오너는 open shift를 직접 만들 수 있다.
- 매니저와 오너는 open shift에 직원을 직접 배정할 수 있다.
- open shift는 결근 요청, 추가 인력 필요, 새 coverage 필요, 매니저 수동 생성 등의 이유로 만들어질 수 있다.
- MVP에서는 고정 weekly schedule pattern을 지원한다.
- 차후에는 매니저가 1주, 2주, 3주, 1개월 또는 직접 지정한 기간의 published schedule period를 만들 수 있도록 확장한다.
- 매니저에게만 시간 충돌, 포지션 불일치, 근무시간 증가, 오버타임 가능성 경고를 보여준다.
- 추후 payroll, overtime 계산, 실제 근무 기록 기능을 붙일 수 있도록 데이터 모델을 분리한다.

### MVP 제외 범위

- 출퇴근 clock-in / clock-out
- 자동 급여 계산
- 오버타임 정산 또는 payroll export
- 대체 직원의 앱 내 수락 단계
- 여러 지점 관리
- 주/지역별 복잡한 노동법 자동화
- 월간 화면에서 직접 세부 편집

## 사용자 역할과 권한

### Employee

직원은 다음을 할 수 있다.

- 앞으로 3일 자기 스케줄 확인
- 주간/월간 화면에서 누가 일하는지 확인
- 자기 shift에 대한 coverage request 생성
- 이미 대체자를 구한 경우 대체 직원 지정
- 대체자가 없는 결근/휴가 요청 생성
- 2주, 3주, 4주처럼 날짜 범위가 있는 장기휴가 요청 생성
- open shift 지원
- 자기 요청 상태 확인

직원은 다음을 할 수 없다.

- 스케줄 변경 승인
- 공식 open shift 생성
- 다른 직원을 shift에 배정
- schedule pattern 편집
- 매니저 전용 오버타임 또는 비용 리스크 경고 확인

### Manager

매니저는 직원 권한을 모두 가지며, 추가로 다음을 할 수 있다.

- coverage request 승인 또는 거절
- 대체자 없는 결근/휴가 요청 검토
- 장기휴가 요청 승인 또는 거절
- open shift 생성
- open shift에 직원 배정
- 고정 weekly schedule pattern 관리
- 차후 확장에서는 매니저가 직접 기간을 정해 published schedule period 생성
- 승인 전 충돌, 포지션, 근무시간 경고 확인
- 오버타임 가능성 경고 확인

### Owner/Admin

오너와 관리자는 매니저 권한을 모두 가지며, 추가로 다음을 할 수 있다.

- 직원 추가 및 비활성화
- 직원 역할 관리
- 팀과 직원의 팀 소속 관리
- 포지션 관리
- 향후 payroll/overtime 기능이 추가될 때 관련 설정 관리

## 핵심 화면

### Home

Home은 직원이 앱을 열었을 때 가장 먼저 보는 화면이다.

직원에게 보여줄 내용:

- 앞으로 3일 내 자기 shift
- pending, approved, declined, cancelled 상태의 자기 요청
- 지원 가능한 open shift

매니저와 오너에게 추가로 보여줄 내용:

- 승인 대기 요청 수
- 오늘과 가까운 날짜의 open shift
- 급하게 채워야 하는 uncovered shift
- 확인이 필요한 스케줄 경고

### Schedule

Schedule 화면은 세 가지 보기로 구성한다.

- 3 Days
- Weekly
- Monthly

3-day view는 당장 가까운 운영 상황을 빠르게 확인하기 위한 화면이다.

Weekly view는 요일, 시간, 포지션별로 누가 일하는지 보여준다. 실제 운영에서 가장 자주 쓰는 읽기 전용 스케줄 화면이다.

Monthly view는 한 달 전체 흐름을 확인하는 읽기 전용 화면이다. 직원과 매니저가 해당 달에 누가 일하는지, 어느 날짜에 open shift나 요청이 있는지 확인할 수 있다. 날짜를 선택하면 해당 날짜 또는 해당 주 상세로 이동할 수 있지만, MVP에서는 월간 화면에서 직접 세부 편집하지 않는다.

### My Requests

직원이 요청을 만들고 상태를 확인하는 화면이다.

요청 유형:

- 대체자를 지정한 coverage request
- 대체자 없는 결근/휴가 요청
- 날짜 범위가 있는 장기휴가 요청

요청 상태:

- Pending
- Approved
- Declined
- Cancelled

### Manager Queue

매니저와 오너가 승인해야 할 작업을 확인하는 화면이다.

큐에 표시되는 항목:

- 대체자가 지정된 coverage request
- 대체자 없는 결근/휴가 요청
- 장기휴가 요청
- open shift 지원
- 아직 배정자가 없는 매니저 생성 open shift

승인 전 보여줄 경고:

- 대체 직원에게 시간 충돌이 있는지
- 대체 직원의 포지션이 맞는지
- 승인 후 shift가 여전히 비는지
- 장기휴가 승인으로 open 또는 needs coverage shift가 생기는지
- 승인 후 직원의 주간 근무시간이 증가하는지
- 오버타임 가능성이 있는지

MVP에서는 경고가 자동 차단 역할을 하지 않는다. 식당 운영에는 예외가 많기 때문에 매니저가 경고를 보고도 승인할 수 있어야 한다.

### Staff and Patterns

매니저와 오너가 다음을 관리하는 화면이다.

- 직원
- 팀과 직원의 팀 소속
- 포지션
- 역할
- 고정 weekly schedule pattern
- 차후 published schedule period

### MVP 직원 추가

MVP에는 가벼운 직원 추가 기능을 포함해야 한다. 실제 식당에서 새 직원을 추가할 수 없으면 스케줄 배정과 대체 근무 관리가 바로 막히기 때문이다.

매니저 또는 오너는 직원을 만들 때 다음 정보를 입력할 수 있어야 한다.

- 이름
- 이메일
- 전화번호
- 역할: employee, manager, owner
- 포지션 목록, 한 직원이 여러 직무를 맡을 수 있음
- 팀 소속, 한 직원이 여러 팀에 포함될 수 있음
- 활성 또는 비활성 상태
- 고정 weekly schedule pattern, 생성 시 선택 입력 가능

MVP에서 완전한 HR 관리는 하지 않는다. 다음 기능은 차후로 미룬다.

- 이메일 초대 플로우
- 프로필 사진
- 상세 인사 기록
- 포지션별 시급 또는 급여 프로필
- 비자, 서류, 컴플라이언스 관리
- 입사일/퇴사일 기반 HR 워크플로우
- 세분화된 권한 관리

## Schedule Pattern과 Published Schedule 설계

MVP는 고정 weekly pattern을 지원한다. MVP에서는 biweekly schedule을 자동 반복 패턴으로 고정하지 않는다.

### 고정 Weekly Pattern

고정 weekly pattern은 매주 같은 shift가 반복되는 구조다. 첫 버전은 가볍게 shift coverage에 집중해야 하므로, 이 방식을 MVP의 기본 스케줄 모델로 사용한다.

예:

- Monday AM
- Tuesday PM
- Friday Full

### 차후 PublishedSchedulePeriod

차후 스케줄 기능은 자동 biweekly 반복만 지원하기보다, 매니저가 직접 게시 스케줄 기간을 만드는 방식으로 확장한다. 이 방향은 매니저가 1주 또는 2주 단위로 실제 근무표를 직접 짜고 직원에게 게시하는 식당/카페 운영 방식에 더 잘 맞는다.

매니저와 오너는 다음과 같은 기간을 선택해 스케줄 기간을 만들 수 있어야 한다.

- 1주
- 2주
- 3주
- 4주
- 1개월
- 직접 지정한 시작일과 종료일

published schedule period 안에서 매니저는 해당 기간의 실제 shift를 만들거나 수정한 뒤 직원에게 게시한다.

이 방식은 2주마다 또는 한 달마다 새 스케줄을 짜서 직원에게 주는 식당 운영 방식에 더 잘 맞는다.

MVP에서는 `SchedulePattern`을 고정 weekly schedule을 쓰는 식당을 위한 기본 템플릿으로 사용한다. 예를 들어 매주 월/화 full, 수요일 evening, 목/토 off처럼 같은 패턴이 반복되는 운영을 표현한다.

차후 매니저가 1주, 2주, 3주, 4주 또는 커스텀 기간의 스케줄을 만드는 기능은 `SchedulePatternType` 값을 늘리는 방식이 아니라 `PublishedSchedulePeriod`를 중심으로 확장한다. 고정 weekly pattern은 선택한 기간의 draft shift를 빠르게 생성하는 템플릿으로 사용할 수 있고, 매니저는 생성된 shift를 직접 추가, 수정, 삭제한 뒤 게시한다.

`SchedulePatternType`은 반복 템플릿의 종류를 나타낸다. MVP에서는 `FIXED_WEEKLY`만 필요하다. 나중에 A주/B주처럼 패턴 자체가 여러 주기로 자동 반복되는 요구가 생길 때만 `ROTATING_MULTI_WEEK` 같은 값을 추가한다.

이 기능은 MVP 구현 범위가 아니라 차후 확장 범위다. MVP에서는 `Shift`를 특정 날짜의 실제 스케줄로 보고, `SchedulePattern`은 shift를 생성하는 기본값으로만 두면 된다.

향후 `PublishedSchedulePeriod` 필드 예:

- Location
- 시작일
- 종료일
- 상태: draft, published, archived
- 생성한 매니저 또는 오너
- 게시 시간
- 메모

## 데이터 모델

### Employee

직원을 나타낸다.

필드:

- 이름
- 이메일
- 전화번호
- 포지션 목록, 한 직원이 여러 직무를 맡을 수 있음
- 팀 목록, 한 직원이 여러 팀에 포함될 수 있음
- 역할: employee, manager, owner
- 활성 상태

MVP에서는 직원이 맡을 수 있는 직무를 `Employee.positions`로 직접 표현한다. 이렇게 하면 한 직원이 평소에는 cashier로 일하고 특정 요일에는 sushi bar helper로 들어가는 실제 식당 운영 사례를 단순하게 처리할 수 있다.

차후 payroll, 포지션별 자격 상태, 시급 변경 이력이 중요해지면 `EmployeePosition` 엔티티로 확장한다. 이 엔티티는 직원과 포지션을 연결하면서 기본 시급, 활성 상태, 적용 시작일과 종료일 같은 메타데이터를 가질 수 있다.

### Team

대체자 선택 범위를 제한하기 위한 운영 그룹을 나타낸다.

예:

- Sushi team
- Server team
- Kitchen team
- Dinner service team

직원은 여러 팀에 동시에 속할 수 있다. 직원이 대체자를 지정한 coverage request를 만들 때, 대체자 선택 목록에는 요청 직원과 하나 이상의 팀을 공유하는 직원만 표시되어야 한다.

### Position

팀 안에서 맡는 직무를 나타낸다. 예를 들어 chef, kitchen helper, roll man, sushi man, server, cashier, manager 같은 값이다.

포지션은 대체자가 원래 shift의 포지션과 맞지 않을 때 매니저에게 경고를 보여주는 데 사용한다.

포지션은 시간대 이름이 아니다. 오픈, 마감, 오전, 오후, 미들, 풀타임 같은 값은 포지션으로 모델링하지 않는다.

직원은 여러 포지션을 가질 수 있다. 다만 특정 `Shift`에는 필요한 직무가 하나 있어야 하므로 `Shift.position`은 해당 근무에서 반드시 커버되어야 하는 직무를 나타낸다.

### Future ShiftTemplate

차후 shift를 빠르게 만들기 위한 매장별 시간 프리셋을 나타낸다.

첫 MVP 구현 범위에는 포함하지 않는다. MVP에서는 실제 `startTime`, `endTime`을 `Shift`에 직접 저장한다.

차후 예:

- Morning: 09:00-14:00
- Afternoon: 14:00-18:00
- Middle: 11:00-16:00
- Closing: 17:00-22:00
- Full: 09:00-22:00
- Custom: 매니저가 직접 입력한 시작/종료 시간

ShiftTemplate은 식당과 카페마다 사용하는 이름과 시간이 다르므로 매장별로 설정 가능해야 한다. 템플릿에서 Shift를 만들더라도 실제 Shift에는 최종 시작 시간과 종료 시간을 복사해 저장한다. 그래야 나중에 템플릿 시간이 바뀌어도 과거 스케줄이 바뀌지 않는다.

### SchedulePattern

직원의 반복 스케줄 규칙을 나타낸다.

필드:

- 직원
- 패턴 유형: fixed_weekly
- weekly shift 목록

SchedulePattern은 기본 반복 규칙이지 최종 실제 스케줄이 아니다. MVP에서는 고정 스케줄을 쓰는 식당을 위해 `fixed_weekly`만 지원한다.

### PublishedSchedulePeriod

차후 매니저가 선택한 기간에 대해 만드는 게시 스케줄 기간을 나타낸다.

이 기능은 첫 MVP 구현 범위에는 포함하지 않지만, 설계상 확장 가능해야 한다.

PublishedSchedulePeriod는 1주, 2주, 3주, 4주 또는 커스텀 날짜 범위의 실제 스케줄 묶음이다. 매니저는 고정 weekly pattern에서 draft shift를 생성할 수도 있고, 빈 기간에서 직접 shift를 넣을 수도 있다. 게시 전에는 draft 상태로 수정 가능하고, 게시 후에는 직원이 해당 기간의 실제 스케줄로 확인한다.

포함될 수 있는 필드:

- Location
- 시작일
- 종료일
- 상태: draft, published, archived
- 생성한 매니저 또는 오너
- 게시 시간
- 메모

published schedule period를 만들고, 수정하고, 게시하는 권한은 매니저와 오너에게만 있어야 한다.

### Shift

특정 날짜에 실제로 예정된 shift를 나타낸다.

필드:

- 날짜
- 시작 시간
- 종료 시간
- 포지션
- 배정된 직원, open shift인 경우 비어 있을 수 있음
- 상태: scheduled, open, cancelled, covered
- 생성 이유: pattern, absence_request, extra_staffing, new_position_coverage, manual

주간/월간 스케줄 화면은 raw pattern이 아니라 Shift를 보여준다.

`Shift`는 예정된 근무 스케줄이지 실제 출퇴근 기록이 아니다. 직원이 11:00-21:00 근무 예정이었는데 19:00에 조퇴했다면, 원래 `Shift`는 11:00-21:00으로 유지하고 차후 실제 근무 기록 모델이 조퇴를 저장해야 한다.

### CoverageRequest

직원이 자기 shift를 못 나오는 상황과 관련된 요청을 나타낸다.

필드:

- 요청 직원
- 대상 shift
- 대체 직원, optional
- 대체자 자격: 대체 직원은 요청 직원과 하나 이상의 팀을 공유해야 함
- 사유, optional
- 요청 유형: substitute_requested 또는 absence_without_substitute
- 상태: pending, approved, declined, cancelled

MVP에서는 대체 직원이 앱 안에서 따로 수락할 필요가 없다. 최종 승인 여부는 매니저 또는 오너가 결정한다. 요청 직원과 같은 팀이 아닌 대체자를 지정한 coverage request는 manager queue에 올라가기 전에 validation에서 거절되어야 한다.

### LeaveRequest

2주, 3주, 4주 휴가처럼 날짜 범위가 있는 장기휴가 요청을 나타낸다.

필드:

- 직원
- 시작일
- 종료일
- 사유, optional
- 상태: pending, approved, declined, cancelled
- 승인한 매니저 또는 오너, optional
- 승인 시간, optional

LeaveRequest가 승인되면 해당 직원은 승인된 기간 동안 스케줄 배정 대상에서 제외되어야 한다. 승인된 휴가 기간에 이미 해당 직원에게 배정된 shift가 있으면, 그 shift는 open 또는 needs coverage 상태가 되어 다른 직원이 지원하고 매니저가 승인하거나 직접 배정할 수 있어야 한다.

### OpenShiftApplication

직원이 open shift에 지원한 기록을 나타낸다.

필드:

- open shift
- 지원 직원
- 상태: pending, approved, declined

## 상태 흐름

### 대체자를 지정한 Coverage Request

1. 직원이 자기 shift 중 하나를 선택한다.
2. 직원이 자기와 같은 팀에 속한 대체 직원을 지정해 coverage request를 만든다.
3. 매니저가 요청을 검토한다.
4. 매니저는 충돌, 포지션, 근무시간 경고를 확인한다.
5. 승인되면 shift 담당자가 원래 직원에서 대체 직원으로 변경된다.
6. 거절되면 원래 직원이 그대로 shift에 배정되어 있다.

### 대체자 없는 결근/휴가 요청

1. 직원이 자기 shift 중 하나를 선택한다.
2. 직원이 대체자 없이 결근/휴가 요청을 만든다.
3. 매니저가 요청을 검토한다.
4. 승인되면 원래 shift가 open shift가 된다.
5. 이후 매니저가 직접 직원을 배정하거나, 직원이 지원하고 매니저가 승인한다.

### 장기휴가 요청

1. 직원이 시작일과 종료일이 있는 LeaveRequest를 만든다.
2. 매니저 또는 오너가 날짜 범위를 검토한다.
3. 거절되면 해당 직원은 그 기간에도 shift 배정 대상에 남아 있다.
4. 승인되면 해당 직원은 그 기간 동안 스케줄 배정 대상에서 제외된다.
5. 승인된 휴가 기간에 이미 해당 직원에게 배정된 shift는 open 또는 needs coverage 상태가 된다.
6. 다른 직원들이 해당 open shift에 지원할 수 있다.
7. 매니저 또는 오너가 지원을 승인하거나 직접 직원을 배정한다.

### 매니저가 직접 생성한 Open Shift

1. 매니저가 open shift를 생성한다.
2. 날짜, 시간, 포지션, 생성 이유를 선택한다.
3. 생성 이유는 extra staffing, new coverage need, manual 또는 기타 운영상 이유가 될 수 있다.
4. 직원이 지원하거나 매니저가 직접 직원을 배정한다.
5. 직원이 배정되면 shift 상태는 scheduled가 된다.

## 오버타임과 Payroll 확장

MVP에서는 payroll을 계산하거나 오버타임을 정산하지 않는다. 대신 매니저에게만 근무시간 증가와 오버타임 가능성 경고를 보여준다.

이 확장 방향은 나중에 변경될 수 있다. 지금 중요한 MVP 원칙은 예정 스케줄, 변경 요청, 실제 근무 기록, 급여 요약을 하나의 모델에 섞지 않는 것이다.

향후 추가될 수 있는 모델:

### TimeClockRecord

실제 출근/퇴근 시간을 나타낸다. 이후 구현 계획에서는 `ActualWorkRecord`라는 이름을 검토할 수도 있다.

포함될 수 있는 필드:

- 직원
- shift, optional
- 출근 시간
- 퇴근 시간
- 휴게 시간
- 매니저 조정
- 승인 상태
- 조퇴 또는 지각 같은 메모

### PayrollPeriod

weekly 또는 biweekly 같은 급여 기간을 나타낸다.

### EmployeePosition

차후 직원과 포지션의 관계에 payroll 메타데이터를 붙이기 위한 엔티티다.

포함될 수 있는 필드:

- 직원
- 포지션
- 해당 포지션의 기본 시급
- 활성 상태
- 적용 시작일
- 적용 종료일, 선택 값

MVP에서는 `Employee.positions`를 직접 사용한다. 포지션별 시급, 자격 상태, 시급 변경 이력이 필요해지는 시점에 `EmployeePosition`을 추가한다.

### OvertimeRule

주, 지역, 회사 정책, 적용 시작일 기준의 오버타임 규칙을 나타낸다.

### PayrollSummary

특정 급여 기간의 급여 계산 결과를 나타낸다.

포함될 수 있는 필드:

- regular hours
- overtime hours
- gross pay estimate
- export status

향후 payroll 기능은 예정 shift만이 아니라 실제 근무 기록을 기준으로 계산해야 한다. 이렇게 하면 예정 스케줄, 실제 출퇴근, 조퇴, 지각, 휴게 시간, 실제 유급 시간, payroll export를 서로 분리해서 관리할 수 있다.

## 로그인, 가게, 초대 기능 확장

로그인 관리, 가게/테넌트 관리, 이메일 초대, 비밀번호 재설정, 여러 지점 접근 제어는 차후 확장 기능이다. MVP에서는 전체 로그인/초대 시스템을 구현하지 않지만, 나중에 Company, Location, UserAccount, Employee를 분리할 수 있도록 엔티티 경계를 열어두어야 한다.

향후 모델은 다음을 지원할 수 있어야 한다.

### Company 또는 Tenant

서비스를 사용하는 식당 사업체 또는 고객 계정을 나타낸다.

예:

- 한 스시집 회사
- 여러 매장을 가진 식당 그룹
- 단일 독립 식당

### Location

회사 아래의 특정 지점 또는 매장을 나타낸다.

예:

- 본점
- 2호점
- Downtown branch

MVP는 한 지점용이지만, 향후에는 스케줄, shift, 팀, 포지션, 직원, 요청을 특정 location에 연결할 수 있어야 한다.

### UserAccount

로그인 계정을 나타낸다.

UserAccount는 Employee와 분리하는 것이 좋다. 그래야 하나의 로그인 계정이 나중에 직원 기록, 오너 기록, 여러 지점 접근 권한과 연결될 수 있다.

향후 포함될 수 있는 필드:

- 이메일
- 유저명
- 비밀번호 해시
- 계정 상태
- 마지막 로그인 시간
- 비밀번호 재설정 상태

### Invitation

오너, 매니저, 직원에게 보내는 이메일 초대를 나타낸다.

향후 오너 온보딩 흐름:

1. 서비스 운영자가 새 company 또는 tenant를 만든다.
2. 서비스 운영자가 오너 이메일로 초대 메일을 보낸다.
3. 오너가 초대를 수락하고 로그인 계정을 만든다.
4. 오너는 로그인 후 자기 company와 location 데이터만 볼 수 있다.
5. 오너가 해당 location의 직원을 추가한다.

향후 직원 온보딩 흐름:

1. 오너 또는 매니저가 직원을 추가한다.
2. 시스템이 직원에게 초대 이메일을 보낸다.
3. 직원은 초대 링크에서 직접 비밀번호를 설정한다.
4. 직원은 로그인 후 허용된 location과 직원용 데이터만 볼 수 있다.
5. 직원은 나중에 비밀번호 변경 또는 재설정을 할 수 있다.

보안상 실제 서비스에서는 유저명과 비밀번호 원문을 이메일로 보내는 방식보다, 초대 링크를 통해 사용자가 직접 비밀번호를 설정하는 방식을 우선해야 한다.

### 여러 지점 접근 제어

향후 접근 규칙은 다음을 보장해야 한다.

- 오너는 자신이 소유하거나 관리하는 company와 location만 볼 수 있다.
- 매니저는 자신이 배정된 location만 볼 수 있다.
- 직원은 자신에게 허용된 location 데이터와 직원용 스케줄 정보만 볼 수 있다.
- 한 사용자는 나중에 여러 location에 접근할 수도 있다.

이 내용은 차후 범위다. MVP는 가볍게 유지하고, 도메인 모델을 만드는 동안에는 개발용 사용자 또는 단일 지점 계정 가정을 사용할 수 있다.

## 알림

MVP 알림은 앱 내부의 단순 상태 알림으로 시작한다.

알림 이벤트:

- 직원이 요청을 만들면 매니저에게 알림
- 매니저가 요청을 승인/거절하면 직원에게 알림
- 매니저가 open shift를 만들면 관련 포지션 직원에게 알림
- 매니저가 open shift에 직원을 배정하면 해당 직원에게 알림
- 급한 open shift는 매니저 홈 화면에 표시

SMS, email, push notification은 나중에 추가한다.

## 승인 전 경고

매니저는 승인 전에 다음 경고를 확인할 수 있어야 한다.

- 대체 직원에게 시간 충돌이 있음
- 대체 직원의 포지션이 다름
- 승인 후에도 shift가 비어 있음
- 승인된 장기휴가로 인해 coverage가 필요한 shift가 생김
- open shift가 앞으로 3일 이내인데 아직 배정자가 없음
- 승인 시 직원의 예상 주간 근무시간이 증가함
- 승인 시 오버타임 가능성이 있음

MVP에서는 경고가 자동 차단 역할을 하지 않는다.

## 테스트 기준

다음 흐름이 동작하면 MVP의 핵심 기능이 충족된 것으로 본다.

- 직원이 대체자를 지정한 coverage request를 만들면 manager queue에 표시된다.
- 직원은 같은 팀을 공유하지 않는 직원을 대체자로 선택할 수 없다.
- 한 직원이 여러 팀에 포함되어 있어도, 요청 직원과 하나 이상의 팀을 공유하면 대체자로 선택할 수 있다.
- 매니저가 대체자 요청을 승인하면 shift 담당자가 변경된다.
- 매니저가 대체자 요청을 거절하면 원래 담당자가 유지된다.
- 직원이 대체자 없는 결근/휴가 요청을 만들 수 있다.
- 매니저가 대체자 없는 요청을 승인하면 shift가 open 상태가 된다.
- 직원이 날짜 범위가 있는 장기휴가 요청을 만들 수 있다.
- 매니저가 장기휴가 요청을 승인하면 해당 직원은 그 기간 동안 스케줄 배정 대상에서 제외된다.
- 승인된 장기휴가 기간에 이미 배정된 shift는 open 또는 needs coverage 상태가 된다.
- 매니저가 open shift를 직접 만들 수 있다.
- 직원이 open shift에 지원할 수 있다.
- 매니저가 open shift 지원을 승인하면 shift가 배정된다.
- 매니저가 open shift에 직원을 직접 배정할 수 있다.
- weekly pattern이 특정 기간에 올바른 shift를 생성한다.
- 고정 weekly pattern이 선택한 기간에 올바른 shift를 생성한다.
- 차후 published schedule period는 1주, 2주, 3주, 1개월 또는 직접 지정한 기간을 표현할 수 있다.
- 직원 홈에 앞으로 3일 스케줄이 표시된다.
- weekly view에서 선택한 주에 누가 일하는지 확인할 수 있다.
- monthly view에서 선택한 달에 누가 일하는지 확인할 수 있다.
- 직원은 요청 승인이나 shift 배정을 할 수 없다.
- 매니저와 오너는 요청 승인과 shift 배정을 할 수 있다.
- 매니저 전용 경고는 일반 직원에게 보이지 않는다.

## 초기 구현 추천 순서

MVP는 payroll이나 완전한 scheduling suite가 아니라 shift coverage를 중심으로 먼저 만든다.

추천 구현 순서:

1. 직원, 역할, 팀, 포지션
2. 고정 weekly schedule pattern
3. shift 생성과 읽기 전용 schedule view
4. 직원 coverage request, absence request, 장기휴가 request
5. manager approval queue
6. open shift 생성, 지원, 배정
7. 매니저 전용 경고
8. 기본 앱 내 알림

이 순서로 만들면 첫 버전을 가볍게 유지하면서도, 나중에 payroll, overtime, multi-location, 더 넓은 직원 관리 기능으로 확장할 수 있다.
