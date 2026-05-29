# API-First Mobile Client Strategy Design

## Overview

MVP는 모바일을 1차 사용 환경으로 하는 PWA-first 웹앱을 목표로 한다. 주된 사용자는 iPhone 또는 Android phone에서 직원 스케줄 확인, 요청 생성, 매니저/오너 승인을 처리한다. 데스크톱 웹은 사용할 수 있어야 하지만, MVP의 UX 기준은 모바일 화면과 터치 조작이다.

프론트엔드는 현재 Spring Boot 백엔드 프로젝트와 다른 디렉터리의 별도 React/Vite 프로젝트에서 만든다.

현재 `managingShift` 프로젝트는 API-first 백엔드 서버 역할만 맡는다. 직원용 화면과 매니저/오너 승인 화면은 별도 React/Vite PWA가 먼저 담당하고, 이후 앱스토어 배포나 깊은 네이티브 기능이 필요해지면 React Native 앱이 같은 API를 호출하는 두 번째 모바일 클라이언트가 될 수 있다.

핵심 원칙은 **PWA에 종속되지 않는 백엔드 API**다. PWA는 첫 번째 클라이언트이고, React Native는 미래 클라이언트다.

## Selected Approach

선택한 접근은 **API-first backend + React/Vite PWA first client + React Native future client** 방식이다.

MVP 개발 중 네트워킹은 **환경변수 API base URL + Spring CORS** 방식으로 처리한다.

개발 중 기본 구조:

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
      # Optional later, only if testing on a real phone over the same Wi-Fi.
      - http://PC_IP:5173
```

`PC_IP`는 개발자 PC의 LAN IP 주소로 교체한다.

## What CORS Means

CORS는 Cross-Origin Resource Sharing의 줄임말이다. 브라우저가 현재 페이지와 다른 origin의 API를 호출할 때, API 서버가 그 호출을 허용했는지 확인하는 보안 규칙이다.

origin은 protocol, host, port 조합이다.

```text
http://localhost:5173
http://localhost:8080
http://192.168.0.20:5173
```

위 주소들은 서로 다른 origin이다. 따라서 React/Vite frontend가 Spring Boot backend를 호출하려면 backend가 frontend origin을 명시적으로 허용해야 한다.

CORS는 서버 간 통신 제한이 아니라 브라우저 보안 제한이다. Postman이나 backend server에서 직접 호출할 때와 달리, 모바일 Safari나 Chrome에서 frontend JavaScript가 API를 호출할 때 영향을 받는다.

## Primary Device Strategy

우선순위:

1. Mobile phone web/PWA
2. Desktop Chrome for development and occasional admin use
3. React Native mobile app later, if app store distribution and native capabilities become necessary

MVP 화면은 mobile phone viewport를 기준으로 설계한다. 데스크톱에서는 같은 기능을 더 넓은 화면에서 볼 수 있게 responsive layout을 제공하지만, 데스크톱 전용 복잡한 dashboard를 먼저 만들지 않는다.

초기 테스트 기준:

- Chrome DevTools mobile viewport
- iPhone-sized narrow viewport
- Android/Galaxy-sized narrow viewport
- touch-friendly button size and spacing
- one-handed use 가능한 primary actions

## Future React Native Strategy

React/Vite PWA 코드는 React Native로 그대로 복사해서 쓸 수 있다고 가정하지 않는다. 웹의 `div`, CSS, browser router, DOM API는 React Native의 `View`, `Text`, navigation, native module 구조와 다르다.

대신 다음을 재사용 가능하게 설계한다.

- backend API contract
- request/response DTO 이름과 필드
- TypeScript domain type
- API client 함수 이름과 책임
- 화면 흐름과 도메인 용어
- validation rule
- loading, empty, error state 패턴

React Native 전환 시 주로 다시 만드는 것:

- 화면 component
- navigation shell
- native-specific storage
- push notification registration
- app store build/signing configuration
- device permission flow

따라서 지금 백엔드 구현은 `React/Vite PWA 전용 API`가 아니라 `mobile clients용 API`로 작성한다.

## MVP Mobile Scope

직원용 화면과 매니저/오너 화면을 같은 수준으로 모바일 최적화한다.

직원용 PWA가 호출할 API:

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

매니저/오너용 PWA가 호출할 API:

- pending manager queue
- approve or decline coverage requests
- approve or decline planned leave requests
- create open shifts
- assign employees to open shifts
- approve open shift applications
- employee, team, position basic management
- manager-only warning data

## Backend Responsibilities

현재 `managingShift` backend는 다음을 제공한다.

- `/api/**` REST endpoint
- versioned API path such as `/api/v1/**`
- frontend-friendly request and response DTO
- mobile-client-friendly response DTO
- configured CORS for local and LAN origins
- preflight `OPTIONS` request support
- stable error response shape
- API contract that can be reused by a future React Native client
- smoke test for CORS configuration
- documentation for LAN mobile testing

백엔드는 React project directory를 알 필요가 없다. 백엔드는 허용 origin과 API contract만 제공한다.

## Frontend Responsibilities

별도 React/Vite PWA 프로젝트는 다음을 담당한다.

- mobile-first UI
- desktop-compatible responsive layout
- PWA manifest and service worker
- iOS/Android browser layout testing
- API base URL environment configuration
- API client layer separated from UI components
- TypeScript DTO/domain types that can later be copied or moved into a shared package
- request state, loading state, empty state, error state
- employee and manager navigation

프론트 프로젝트는 이 백엔드 저장소 밖에 위치한다.

## Development Networking

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

프론트 기본 주소:

```text
http://localhost:5173
```

주 테스트 방식:

1. Spring Boot backend를 실행한다.
2. 별도 React/Vite frontend dev server를 실행한다.
3. PC Chrome에서 `http://localhost:5173`을 연다.
4. Chrome DevTools의 device toolbar를 켜고 iPhone/Android 화면 크기를 선택한다.
5. frontend가 `http://localhost:8080/api/...`를 호출하는지 확인한다.
6. CORS 오류가 browser console에 없는지 확인한다.

선택적인 휴대폰 실기기 테스트:

1. PC와 휴대폰을 같은 Wi-Fi에 연결한다.
2. PC의 LAN IP를 확인한다.
3. Spring Boot backend를 실행한다.
4. Vite frontend를 `--host 0.0.0.0`로 실행한다.
5. 휴대폰 브라우저에서 `http://PC_IP:5173`을 연다.
6. frontend가 `http://PC_IP:8080/api/...`를 호출하는지 확인한다.
7. CORS 오류가 브라우저 console에 없는지 확인한다.

## Security Notes

MVP 개발 중에는 `/api/**`를 임시로 열 수 있다. production에서는 authentication과 authorization을 반드시 적용한다.

개발용 CORS 설정은 다음을 피한다.

- `allowedOrigins("*")`와 credentials 조합
- 필요 이상으로 넓은 LAN 대역 허용
- production에서 development origin 그대로 허용

MVP는 인증을 단순하게 유지하지만, API 설계는 나중에 session cookie 또는 token authentication을 붙일 수 있게 유지한다.

## Testing Criteria

이 설계는 다음이 가능하면 충족된다.

- PC 브라우저에서 React frontend가 Spring Boot API를 호출한다.
- PC Chrome DevTools mobile viewport에서 React frontend를 확인할 수 있다.
- Chrome mobile viewport에서 열린 frontend가 Spring Boot API를 호출한다.
- 선택적으로 휴대폰 브라우저에서 React frontend를 열고 Spring Boot API를 호출할 수 있다.
- browser console에 CORS preflight 오류가 없다.
- `/api/**` endpoint는 JSON response를 반환한다.
- `/api/v1/**` endpoint naming can support future mobile clients without breaking old clients.
- CORS allowed origins는 설정으로 변경할 수 있다.
- production profile에서는 개발용 origin 허용을 쉽게 제거할 수 있다.
- React Native 전환 시 backend domain logic을 다시 작성하지 않아도 된다.

## Scope Boundaries

이번 설계는 backend API contract, CORS, PWA-first 개발 방식, React Native 전환 가능성을 다룬다. React/Vite PWA 화면 구현과 React Native 앱 구현은 각각 별도 frontend 프로젝트의 구현계획에서 다룬다.
