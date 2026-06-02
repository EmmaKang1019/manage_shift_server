# API-First Mobile Client Strategy Design

## Overview

The MVP follows a PWA-first strategy with mobile phone usage as the primary environment. The main users will check schedules, create requests, and handle manager or owner approvals from iPhone or Android browsers. Desktop should still work, but the MVP UX baseline is a mobile screen with touch interaction.

The frontend should be built in a separate React/Vite project outside the current Spring Boot backend project. The `managingShift` project remains focused on the API-first backend.

The core principle is a backend API that is not coupled to the PWA. The PWA is the first client, and a future React Native app can become a second mobile client that calls the same API contracts.

## Selected Approach

Use an API-first backend, a React/Vite PWA as the first client, and a future React Native app as a later client.

During development, frontend networking uses an API base URL environment variable plus Spring CORS configuration.

Default development shape:

```text
Chrome on development PC
  -> http://localhost:5173
  -> Chrome DevTools mobile viewport
  -> React/Vite PWA frontend
  -> fetch http://localhost:8080/api/...
  -> Spring Boot backend
  -> H2 or MySQL
```

The frontend should configure the API URL in its own `.env.local` file.

```env
VITE_API_BASE_URL=http://localhost:8080
```

The backend should manage allowed frontend origins in configuration.

```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:5173
      - http://127.0.0.1:5173
      # Add the development PC LAN IP only when testing on a real phone.
      - http://PC_IP:5173
```

Replace `PC_IP` with the development PC LAN IP address.

## CORS Meaning

CORS means Cross-Origin Resource Sharing. It is the browser security rule that checks whether an API server allows JavaScript from a different origin to call it.

An origin is the combination of protocol, host, and port.

```text
http://localhost:5173
http://localhost:8080
http://192.168.0.20:5173
```

These are different origins. For the React/Vite frontend to call the Spring Boot backend, the backend must explicitly allow the frontend origin.

CORS is a browser restriction, not a general server-to-server networking restriction. It matters when frontend JavaScript in mobile Safari or Chrome calls the API.

## Primary Device Strategy

Priority:

1. Mobile phone web/PWA.
2. Desktop Chrome for development and occasional admin use.
3. React Native mobile app later.

MVP screens should be designed against mobile phone viewports. Desktop should use the same functionality in a responsive layout, but the first version should not prioritize a complex desktop-only dashboard.

Initial testing baseline:

- Chrome DevTools mobile viewport.
- iPhone-sized narrow viewport.
- Android/Galaxy-sized narrow viewport.
- Touch-friendly button size and spacing.
- Primary actions that work comfortably on one-handed mobile use.

## Future React Native Strategy

Do not assume React/Vite PWA code can be copied directly into React Native. Browser `div`, CSS, browser routing, and DOM APIs differ from React Native `View`, `Text`, navigation, and native module patterns.

Design these parts for reuse:

- Backend API contract.
- Request/response DTO names and fields.
- TypeScript domain types.
- API client function names and responsibilities.
- Screen flow and domain vocabulary.
- Validation rules.
- Loading, empty, and error state patterns.

Expect to rebuild these parts for React Native:

- Screen components.
- Navigation shell.
- Native-specific storage.
- Push notification registration.
- App store build/signing configuration.
- Device permission flow.

The backend should therefore be a mobile-client API, not a React/Vite-PWA-only API.

## MVP Mobile Scope

Employee and manager/owner experiences should both be mobile-first.

Employee PWA APIs:

- Next 3 days schedule.
- Weekly schedule.
- Monthly schedule summary.
- Eligible substitutes.
- Coverage request with substitute.
- Absence request without substitute.
- Planned leave request.
- Open shift list.
- Open shift application.
- Own request status.

Manager/owner PWA APIs:

- Pending manager queue.
- Approve or decline coverage requests.
- Approve or decline planned leave requests.
- Create open shifts.
- Assign employees to open shifts.
- Approve open shift applications.
- Employee, team, and position basic management.
- Manager-only warning data.

## Backend Responsibilities

The `managingShift` backend provides:

- `/api/**` REST endpoints.
- Versioned `/api/v1/**` API paths.
- Frontend-friendly request/response DTOs.
- Mobile-client-friendly response DTOs.
- CORS configuration for local and LAN origins.
- Preflight `OPTIONS` request support.
- Stable error response shape.
- API contracts reusable by a future React Native client.
- CORS configuration smoke tests.
- LAN mobile testing documentation.

The backend does not need to know the React project directory. It only provides allowed origins and API contracts.

## Frontend Responsibilities

The separate React/Vite PWA project owns:

- Mobile-first UI.
- Desktop-compatible responsive layout.
- PWA manifest and service worker.
- iOS/Android browser layout testing.
- API base URL environment configuration.
- API client layer separated from UI components.
- TypeScript DTO/domain types that can later move into a shared package or React Native project.
- Request, loading, empty, and error states.
- Employee and manager navigation.

The frontend project should live outside the backend repository.

## Frontend Start Date

Start the separate React/Vite frontend project on 2026-06-03. To meet the 2026-06-20 development completion target, frontend work cannot wait until every backend workflow is implemented.

Start conditions:

- The first `/api/v1/**` endpoint names are defined.
- Request/response DTO names and core fields are documented.
- The backend begins exposing skeleton endpoints that return mock or minimal responses.
- CORS allowed origins include `http://localhost:5173` and `http://127.0.0.1:5173` in the development plan.

Frontend goals for 2026-06-03 to 2026-06-05:

- Create the Vite React project.
- Create the mobile-first layout shell.
- Configure the API base URL environment variable.
- Draft the API client layer and TypeScript DTO types.
- Create the base routes for Home and Schedule with loading, empty, and error states.
- Connect to backend skeleton APIs or a temporary mock adapter.

After 2026-06-08, avoid growing mock-only screens. Connect to real backend endpoints every day. By 2026-06-12, Home and Schedule should work against real APIs. By 2026-06-16, the main My Requests and Manager Queue request/approval flow should be connected. After 2026-06-20, stop new feature development and focus on QA, mobile browser verification, and deployment rehearsal.

## Development Networking

Run the backend on the development PC:

```powershell
.\mvnw.cmd spring-boot:run
```

Backend default address:

```text
http://localhost:8080
```

Run Vite in the separate frontend project:

```powershell
npm run dev -- --host 0.0.0.0
```

Frontend default address:

```text
http://localhost:5173
```

Main test flow:

1. Run the Spring Boot backend.
2. Run the separate React/Vite frontend dev server.
3. Open `http://localhost:5173` in PC Chrome.
4. Enable Chrome DevTools device toolbar and choose an iPhone or Android viewport.
5. Confirm the frontend calls `http://localhost:8080/api/...`.
6. Confirm the browser console has no CORS errors.

Optional real phone test:

1. Connect the PC and phone to the same Wi-Fi.
2. Check the PC LAN IP.
3. Run the Spring Boot backend.
4. Run the Vite frontend with `--host 0.0.0.0`.
5. Open `http://PC_IP:5173` on the phone browser.
6. Confirm the frontend calls `http://PC_IP:8080/api/...`.
7. Confirm there are no CORS errors.

## Security Notes

During MVP development, `/api/**` may be temporarily open. In production, authentication and authorization must be applied.

Avoid these development CORS mistakes:

- Combining credentials with `allowedOrigins("*")`.
- Allowing broader LAN ranges than needed.
- Leaving development origins in the production profile.

Even if MVP authentication stays simple, API design should leave room for future session cookie or token authentication.

## Testing Criteria

This design is satisfied when:

- A React frontend can call the Spring Boot API from a PC browser.
- The frontend can be inspected in a PC Chrome mobile viewport.
- The mobile viewport frontend can call the Spring Boot API.
- Optionally, a real phone browser can open the frontend and call the Spring Boot API.
- The browser console has no CORS preflight errors.
- `/api/**` endpoints return JSON responses.
- `/api/v1/**` endpoint names can support future mobile clients without breaking old clients.
- CORS allowed origins can be changed by configuration.
- The production profile can easily remove development origins.
- A future React Native client does not require backend domain logic to be rewritten.

## Scope Boundaries

This design covers backend API contracts, CORS, the PWA-first development flow, and React Native readiness. React/Vite PWA screen implementation and React Native implementation should each have their own frontend implementation plan.
