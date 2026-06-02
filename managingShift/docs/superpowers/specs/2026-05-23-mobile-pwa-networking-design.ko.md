# API-First Mobile Client Strategy 설계

## 개요

MVP는 모바일 사용을 첫 번째 사용 환경으로 보는 PWA-first 전략을 따른다. 주 사용자는 iPhone 또는 Android phone에서 자신의 스케줄을 확인하고, 요청을 만들고, 매니저 또는 오너가 승인을 처리한다. 데스크톱 사용도 가능해야 하지만 MVP UX의 기준은 모바일 화면과 터치 조작이다.

프론트엔드는 현재 Spring Boot 백엔드 프로젝트와 분리된 별도 React/Vite 프로젝트에서 만든다. `managingShift` 프로젝트는 API-first 백엔드 서버 역할에 집중한다.

핵심 원칙은 **PWA에 종속되지 않는 백엔드 API**다. PWA는 첫 번째 클라이언트이고, React Native는 향후 같은 API를 호출할 수 있는 두 번째 모바일 클라이언트가 될 수 있다.

## 선택한 접근

선택한 방향은 **API-first backend + React/Vite PWA first client + React Native future client**다.

개발 중 네트워크 연결은 **환경변수 API base URL + Spring CORS** 방식으로 처리한다.

기본 개발 구조:

```text
Chrome on development PC
  -> http://localhost:5173
  -> Chrome DevTools mobile viewport
  -> React/Vite PWA frontend
  -> fetch http://localhost:8080/api/...
  -> Spring Boot backend
  -> H2 or MySQL
```

프론트엔드는 별도 프로젝트의 `.env.local` 같은 파일에서 API 주소를 설정한다.

```env
VITE_API_BASE_URL=http://localhost:8080
```

백엔드는 설정 파일에서 허용할 frontend origin을 관리한다.

```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://127.0.0.1:5173
      # 실제 휴대폰 테스트가 필요할 때만 PC LAN IP로 교체한다.
      - http://PC_IP:5173
```

`PC_IP`는 개발 PC의 LAN IP 주소로 교체한다.

## CORS 의미

CORS는 Cross-Origin Resource Sharing의 줄임말이다. 브라우저가 현재 페이지와 다른 origin의 API를 호출할 때, API 서버가 그 호출을 허용하는지 확인하는 보안 규칙이다.

origin은 protocol, host, port 조합이다.

```text
http://localhost:5173
http://localhost:8080
http://192.168.0.20:5173
```

위 주소들은 서로 다른 origin이다. React/Vite frontend가 Spring Boot backend를 호출하려면 backend가 frontend origin을 명시적으로 허용해야 한다.

CORS는 서버 간 통신 제한이 아니라 브라우저 보안 제한이다. Postman이나 backend server에서 직접 호출할 때와 달리, 모바일 Safari 또는 Chrome에서 frontend JavaScript가 API를 호출할 때 영향을 받는다.

## 주요 디바이스 전략

우선순위:

1. Mobile phone web/PWA
2. Desktop Chrome for development and occasional admin use
3. React Native mobile app later

MVP 화면은 mobile phone viewport를 기준으로 설계한다. 데스크톱에서는 같은 기능을 더 넓은 화면에서 볼 수 있게 responsive layout을 제공하되, 데스크톱 전용 복잡한 dashboard를 먼저 만들지 않는다.

초기 테스트 기준:

- Chrome DevTools mobile viewport.
- iPhone-sized narrow viewport.
- Android/Galaxy-sized narrow viewport.
- touch-friendly button size and spacing.
- one-handed use가 가능한 primary actions.

## 향후 React Native 전략

React/Vite PWA 코드를 React Native로 그대로 복사할 수 있다고 가정하지 않는다. `div`, CSS, browser router, DOM API와 React Native의 `View`, `Text`, navigation, native module 구조는 다르다.

재사용 가능하게 설계할 대상:

- backend API contract
- request/response DTO names and fields
- TypeScript domain types
- API client function names and responsibilities
- screen flow and domain vocabulary
- validation rules
- loading, empty, error state patterns

React Native 전환 때 주로 다시 만들 것:

- screen components
- navigation shell
- native-specific storage
- push notification registration
- app store build/signing configuration
- device permission flow

따라서 지금 백엔드는 `React/Vite PWA 전용 API`가 아니라 `mobile clients용 API`로 작성한다.

## MVP Mobile Scope

직원 화면과 매니저/오너 화면은 모두 모바일 우선으로 최적화한다.

직원 PWA가 호출할 API:

- next 3 days schedule
- weekly schedule
- monthly schedule summary
- eligible substitutes
- coverage request with substitute
- absence request without substitute
- planned leave request
- open shift list
- open shift application
- own request status

매니저/오너 PWA가 호출할 API:

- pending manager queue
- approve or decline coverage requests
- approve or decline planned leave requests
- create open shifts
- assign employees to open shifts
- approve open shift applications
- employee, team, position basic management
- manager-only warning data

## 백엔드 책임

현재 `managingShift` backend는 다음을 제공한다.

- `/api/**` REST endpoint
- `/api/v1/**` 같은 versioned API path
- frontend-friendly request/response DTO
- mobile-client-friendly response DTO
- local/LAN origins를 위한 CORS 설정
- preflight `OPTIONS` request support
- stable error response shape
- 향후 React Native 클라이언트가 재사용할 API contract
- CORS configuration smoke test
- LAN mobile testing documentation

백엔드는 React project directory를 알 필요가 없다. 허용 origin과 API contract만 제공한다.

## 프론트엔드 책임

별도 React/Vite PWA 프로젝트가 담당한다.

- mobile-first UI
- desktop-compatible responsive layout
- PWA manifest and service worker
- iOS/Android browser layout testing
- API base URL environment configuration
- UI component와 분리된 API client layer
- React Native 또는 shared package로 옮기기 쉬운 TypeScript DTO/domain types
- request/loading/empty/error state
- employee and manager navigation

프론트엔드 프로젝트는 백엔드 저장소 밖에 위치한다.

## 프론트엔드 시작 시점

프론트엔드 프로젝트는 2026-06-03에 별도 React/Vite 프로젝트로 시작한다. 2026-06-20 개발 완료 목표를 맞추려면 백엔드 전체 구현을 기다린 뒤 프론트를 시작하면 늦다.

시작 조건:

- `/api/v1/**` endpoint 이름이 1차로 정해져 있다.
- request/response DTO 이름과 핵심 필드가 문서에 정리되어 있다.
- backend가 mock 또는 minimal response를 반환할 수 있는 skeleton endpoint를 제공하기 시작한다.
- CORS allowed origin에 `http://localhost:5173`과 `http://127.0.0.1:5173`이 들어갈 계획이 확정되어 있다.

2026-06-03 ~ 2026-06-05의 프론트엔드 목표:

- Vite React 프로젝트 생성.
- mobile-first layout shell 생성.
- API base URL 환경변수 설정.
- API client layer와 TypeScript DTO type 초안 생성.
- Home, Schedule 화면의 기본 route와 loading/empty/error state 생성.
- backend skeleton API 또는 임시 mock adapter와 연결.

2026-06-08 이후에는 mock 화면을 계속 늘리기보다 실제 backend endpoint와 매일 연결한다. 2026-06-12까지 Home/Schedule은 실제 API 기반으로 동작해야 하고, 2026-06-16까지 My Requests/Manager Queue의 주요 요청/승인 흐름이 연결되어야 한다. 2026-06-20 이후에는 신규 화면 개발을 멈추고 QA, 모바일 브라우저 검증, 배포 리허설에 집중한다.

## 개발 네트워킹

PC에서 백엔드 실행:

```powershell
.\mvnw.cmd spring-boot:run
```

백엔드 기본 주소:

```text
http://localhost:8080
```

별도 frontend 프로젝트에서 Vite 실행:

```powershell
npm run dev -- --host 0.0.0.0
```

프론트엔드 기본 주소:

```text
http://localhost:5173
```

주요 테스트 방식:

1. Spring Boot backend를 실행한다.
2. 별도 React/Vite frontend dev server를 실행한다.
3. PC Chrome에서 `http://localhost:5173`을 연다.
4. Chrome DevTools device toolbar를 켜고 iPhone/Android 화면 크기를 선택한다.
5. frontend가 `http://localhost:8080/api/...`를 호출하는지 확인한다.
6. browser console에 CORS 오류가 없는지 확인한다.

선택적 실제 휴대폰 테스트:

1. PC와 휴대폰을 같은 Wi-Fi에 연결한다.
2. PC LAN IP를 확인한다.
3. Spring Boot backend를 실행한다.
4. Vite frontend를 `--host 0.0.0.0`로 실행한다.
5. 휴대폰 브라우저에서 `http://PC_IP:5173`을 연다.
6. frontend가 `http://PC_IP:8080/api/...`를 호출하는지 확인한다.
7. CORS 오류가 없는지 확인한다.

## 보안 메모

MVP 개발 중에는 `/api/**`를 임시로 열 수 있다. production에서는 authentication과 authorization을 반드시 적용한다.

개발 CORS 설정에서 피해야 할 것:

- credentials와 `allowedOrigins("*")` 조합.
- 필요 이상으로 넓은 LAN 대역 허용.
- production profile에 development origin을 그대로 남겨두기.

MVP 인증은 단순하게 유지하더라도 API 설계는 나중에 session cookie 또는 token authentication을 붙일 수 있게 유지한다.

## 테스트 기준

이 설계는 다음이 가능하면 충분하다.

- PC 브라우저에서 React frontend가 Spring Boot API를 호출한다.
- PC Chrome DevTools mobile viewport에서 frontend를 확인할 수 있다.
- mobile viewport에서 열린 frontend가 Spring Boot API를 호출한다.
- 선택적으로 실제 휴대폰 브라우저에서 frontend를 열고 Spring Boot API를 호출할 수 있다.
- browser console에 CORS preflight 오류가 없다.
- `/api/**` endpoint가 JSON response를 반환한다.
- `/api/v1/**` endpoint naming이 향후 모바일 클라이언트를 깨지 않고 지원할 수 있다.
- CORS allowed origins를 설정으로 변경할 수 있다.
- production profile에서는 development origin을 쉽게 제거할 수 있다.
- React Native 전환 때 backend domain logic을 다시 작성하지 않아도 된다.

## 범위 경계

이 설계는 backend API contract, CORS, PWA-first 개발 방식, React Native 전환 가능성을 다룬다. React/Vite PWA 화면 구현과 React Native 앱 구현은 각각 별도 frontend 프로젝트의 구현 계획에서 다룬다.
