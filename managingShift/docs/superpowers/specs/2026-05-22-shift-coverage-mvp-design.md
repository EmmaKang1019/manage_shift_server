# Shift Coverage MVP Design

## Overview

This document defines the MVP design for a lightweight schedule and staff management application for a single restaurant location. The first target use case is a Canadian restaurant, such as a sushi restaurant, where employees often have fixed or repeating schedules but occasionally need coverage when they cannot work.

The MVP focuses on shift coverage: preventing open shifts from being missed, helping employees request coverage, and helping managers fill schedule gaps quickly.

The first version is for one restaurant and one location. The data model should leave room for future expansion to multiple companies and locations, but multi-location management is not part of the MVP.

## Client and Platform Strategy

The backend should be designed as an API-first service. The first frontend client will be a separate React/Vite mobile-first PWA, but the backend API should not be coupled to that web client.

The long-term mobile direction is to support a React Native app if native app store distribution or deeper native device features become important. The PWA is the first client for fast MVP validation; React Native can become a later mobile client that reuses the same backend API.

Client priority:

1. Mobile-first React/Vite PWA as the first MVP client.
2. Desktop-compatible responsive web as a secondary use case.
3. React Native mobile app as a future client for app store distribution and richer native capabilities.

This means backend APIs should return stable JSON DTOs that are useful to both web and native mobile clients. The backend should avoid returning HTML or view-specific payloads that only make sense for one frontend implementation.

## Product Scope

### In Scope

- Employee home screen showing the next 3 days of schedule from the current time.
- Employee schedule views for 3-day, weekly, and monthly schedules.
- Weekly and monthly views are read-only schedule visibility tools for employees and managers.
- Employees can request coverage for their own shift with a substitute employee specified.
- When choosing a substitute, employees can only select people who share at least one team with them.
- One employee can belong to multiple teams.
- Employees can request absence or leave without a substitute.
- Employees can request planned multi-day or multi-week leave, such as 2, 3, or 4 weeks.
- Approved leave periods exclude the employee from scheduling and convert affected assigned shifts into open or needs-coverage shifts.
- Managers and owners can approve or decline all schedule change requests.
- Managers and owners can create open shifts directly.
- Managers and owners can assign employees to open shifts.
- Open shifts can be created because of absence requests, extra staffing needs, new coverage needs, or manual manager action.
- Fixed weekly schedule patterns are supported for the MVP.
- Future manager-created published schedule periods can cover 1 week, 2 weeks, 3 weeks, 1 month, or another configured date range.
- Manager-only warnings show schedule conflicts, position mismatches, increased weekly hours, and possible overtime risk.
- The data model should keep future payroll, overtime calculation, and actual work records possible.
- API-first backend endpoints designed for both the first PWA client and a future React Native client.
- CORS support for the separate React/Vite frontend during development.
- Mobile-first response shapes for employee schedule, request status, manager queue, and open shift workflows.

### Out of Scope for MVP

- Clock-in and clock-out.
- Automatic payroll calculation.
- Overtime settlement or payroll export.
- Employee-side substitute acceptance flow.
- Multi-location management.
- Complex province-specific labor rule automation.
- Direct editing from monthly view.
- Building the React Native app itself.
- App Store or Play Store submission.
- Deep native device integrations such as push notifications, camera, geolocation, biometric login, or offline sync.

## Users and Roles

### Employee

Employees can:

- View their next 3 days of scheduled shifts.
- View weekly and monthly schedules to see who is working.
- Request coverage for their own shift.
- Specify a substitute employee when they have already arranged coverage.
- Request absence or leave without a substitute.
- Request planned leave for a date range, such as 2, 3, or 4 weeks.
- Apply for open shifts.
- View the status of their own requests.

Employees cannot:

- Approve schedule changes.
- Create official open shifts.
- Assign other employees to shifts.
- Edit schedule patterns.
- See manager-only overtime or cost-risk warnings.

### Manager

Managers can do everything employees can do, plus:

- Review and approve or decline coverage requests.
- Review absence requests without substitutes.
- Review and approve or decline planned leave requests.
- Create open shifts.
- Assign employees to open shifts.
- Manage fixed weekly schedule patterns.
- In a future scheduling expansion, create published schedule periods for custom date ranges.
- See conflict, position, and hours warnings before approval.
- See possible overtime risk warnings.

### Owner/Admin

Owners and admins can do everything managers can do, plus:

- Add and deactivate employees.
- Manage employee roles.
- Manage teams and employee team memberships.
- Manage positions.
- Configure future payroll and overtime settings when those features are added.

## Core Screens

### Home

The home screen should be the main starting point for employees.

For employees, it shows:

- Their next 3 days of shifts.
- Their pending, approved, declined, or cancelled requests.
- Open shifts they may be able to apply for.

For managers and owners, it also shows:

- Pending approval count.
- Open shifts today and in the next few days.
- Urgent uncovered shifts.
- Schedule warnings that require attention.

### Schedule

The schedule screen has three views:

- 3 Days
- Weekly
- Monthly

The 3-day view is optimized for immediate operational awareness.

The weekly view shows who is working by day, time, and position. It is the main read-only schedule overview for actual operations.

The monthly view is a read-only overview. It helps employees and managers see who works during the month, which dates have open shifts, and which dates have request activity. Selecting a date can navigate to that day or week detail, but monthly view does not need full editing in the MVP.

### My Requests

Employees use this screen to create and track requests.

Request types:

- Coverage request with a specified substitute.
- Absence or leave request without a substitute.
- Planned leave request for a date range.

Request statuses:

- Pending
- Approved
- Declined
- Cancelled

### Manager Queue

Managers and owners use this screen to review work that needs approval.

Queue items include:

- Coverage requests with substitutes.
- Absence or leave requests without substitutes.
- Planned leave requests.
- Open shift applications.
- Manager-created open shifts that still need assignment.

Before approval, the screen should show:

- Whether the substitute has a schedule conflict.
- Whether the substitute matches the position.
- Whether the shift will remain uncovered.
- Whether approved leave will create open or needs-coverage shifts.
- Whether the approval increases weekly hours.
- Whether there is possible overtime risk.

Warnings should inform the manager but not automatically block approval in the MVP. Restaurant operations often require exceptions.

### Staff and Patterns

Managers and owners use this screen to manage:

- Employees.
- Teams and employee team memberships.
- Positions.
- Roles.
- Fixed weekly schedule patterns.
- Future published schedule periods.

### MVP Employee Creation

The MVP should include lightweight employee creation because schedule coverage cannot work in a real restaurant if managers cannot add new staff.

Managers or owners should be able to create an employee with:

- Name
- Email
- Phone number
- Role: employee, manager, or owner
- Positions, allowing one employee to work multiple job duties
- Team memberships, allowing multiple teams per employee
- Active or inactive status
- Fixed weekly schedule pattern, optional at creation

The MVP should not include full HR management. The following can be deferred:

- Email invitation flow
- Profile photos
- Detailed employment records
- Per-position pay rate and payroll profile
- Visa, document, or compliance management
- Hire date and termination workflow
- Fine-grained permission management

## Schedule Pattern and Published Schedule Design

The MVP supports fixed weekly patterns. The design should not treat biweekly scheduling as an automatic repeating pattern in the MVP.

### Fixed Weekly Pattern

A fixed weekly pattern repeats the same shifts every week. This is the initial MVP scheduling model because the first version should be lightweight and focused on shift coverage.

Example:

- Monday AM
- Tuesday PM
- Friday Full

### Future PublishedSchedulePeriod

Future scheduling should support manager-created published schedule periods instead of only automatic biweekly repetition. This better matches restaurant and cafe operations where managers often create the actual schedule for the next week or two, then publish it to staff.

Managers and owners should be able to create a schedule period for a selected date range, such as:

- 1 week
- 2 weeks
- 3 weeks
- 4 weeks
- 1 month
- A custom start and end date

Within a published schedule period, managers can create or edit the actual shifts for that period and then publish them for employees to view.

This better matches restaurants that prepare a new 1-week, 2-week, 4-week, or monthly schedule instead of using an indefinitely repeating biweekly pattern.

For the MVP, `SchedulePattern` supports restaurants that run a fixed weekly schedule, such as Monday/Tuesday full shifts, Wednesday evening, Thursday/Saturday off, and Friday/Sunday full shifts.

The future 1-week, 2-week, 3-week, 4-week, or custom-period manager scheduling workflow should be modeled with `PublishedSchedulePeriod`, not by adding many `SchedulePatternType` values. A fixed weekly pattern can be used as a template to generate draft shifts for the selected period. Managers can then add, edit, or remove shifts before publishing.

`SchedulePatternType` describes the kind of repeating template. The MVP only needs `FIXED_WEEKLY`. Add values such as `ROTATING_MULTI_WEEK` only if the pattern itself repeats across A/B weeks or another multi-week cycle.

This is future scope, not MVP implementation. The MVP should keep the data model compatible with it by treating `Shift` as the actual dated schedule and `SchedulePattern` as only a default source for generating shifts.

Future fields for `PublishedSchedulePeriod` may include:

- Location
- Start date
- End date
- Status: draft, published, archived
- Created by manager or owner
- Published at
- Notes

## Data Model

### Employee

Represents a staff member.

Fields:

- Name
- Email
- Phone number
- Positions, allowing one employee to be qualified for multiple job duties
- Teams, allowing one employee to belong to multiple teams
- Role: employee, manager, or owner
- Active status

The MVP can model employee qualifications directly as `Employee.positions`. This keeps the first implementation simple while still supporting real restaurant cases where one employee can work as a cashier on most days and help at the sushi bar on another day.

If payroll, qualification status, or historical pay changes become important, the model should expand to an `EmployeePosition` entity that connects one employee to one position with metadata such as default hourly rate, active status, and effective dates.

### Team

Represents an operational group used to limit substitute selection.

Examples:

- Sushi team
- Server team
- Kitchen team
- Dinner service team

Employees can belong to more than one team. When an employee creates a coverage request with a substitute, the substitute picker should only show employees who share at least one team with the requesting employee.

### Position

Represents job duties inside an operational team, such as chef, kitchen helper, roll man, sushi man, server, cashier, or manager.

Positions are used to warn managers when a requested substitute does not match the original shift position.

Positions are not time-of-day labels. Opening, closing, morning, afternoon, middle, and full shifts should not be modeled as positions.

Employees may have more than one position. A `Shift` still has one required position so managers know what duty must be covered for that specific shift.

### Future ShiftTemplate

Represents a future store-configurable time preset for creating shifts quickly.

This is not part of the first MVP implementation. The MVP stores actual `startTime` and `endTime` directly on `Shift`.

Future examples:

- Morning: 09:00-14:00
- Afternoon: 14:00-18:00
- Middle: 11:00-16:00
- Closing: 17:00-22:00
- Full: 09:00-22:00
- Custom: manager-entered start and end time

ShiftTemplate should be configurable per store because restaurants and cafes use different labels and hours. When a manager creates a shift from a template, the resulting `Shift` still stores the actual start and end times so later template edits do not rewrite historical schedules.

### SchedulePattern

Represents an employee's repeating schedule.

Fields:

- Employee
- Pattern type: fixed_weekly
- Weekly shifts

Schedule patterns are default rules, not the final schedule. The MVP supports only `fixed_weekly` for restaurants with stable recurring schedules.

### PublishedSchedulePeriod

Represents a future manager-created schedule period for a selected date range.

This is not part of the first MVP implementation, but the design should leave room for it.

A PublishedSchedulePeriod is the actual schedule bundle for a 1-week, 2-week, 3-week, 4-week, or custom date range. Managers may generate draft shifts from a fixed weekly pattern or start from an empty period and enter shifts manually. Before publishing, the period is editable; after publishing, employees see it as the real schedule for that date range.

Fields may include:

- Location
- Start date
- End date
- Status: draft, published, archived
- Created by manager or owner
- Published at
- Notes

Managers and owners should be the only roles allowed to create, edit, or publish schedule periods.

### Shift

Represents an actual scheduled shift on a specific date.

Fields:

- Date
- Start time
- End time
- Position
- Assigned employee, optional for open shifts
- Status: scheduled, open, cancelled, covered
- Creation reason: pattern, absence_request, extra_staffing, new_position_coverage, manual

Weekly and monthly schedule views should show shifts, not raw patterns.

`Shift` is the planned work schedule, not the actual clock-in/clock-out record. If an employee is scheduled for 11:00-21:00 but leaves at 19:00, the original `Shift` should remain 11:00-21:00 and the future actual work record should store the early departure.

### CoverageRequest

Represents an employee request related to a shift they cannot work.

Fields:

- Requesting employee
- Target shift
- Substitute employee, optional
- Substitute eligibility: the substitute must share at least one team with the requesting employee
- Reason, optional
- Request type: substitute_requested or absence_without_substitute
- Status: pending, approved, declined, cancelled

The MVP does not require the substitute employee to accept inside the app. The manager or owner makes the final approval decision. A coverage request with a substitute outside the requester's teams should be rejected by validation before it reaches the manager queue.

### LeaveRequest

Represents a planned leave request for a date range, such as a 2-week, 3-week, or 4-week vacation.

Fields:

- Employee
- Start date
- End date
- Reason, optional
- Status: pending, approved, declined, cancelled
- Approved by manager or owner, optional
- Approved at, optional

When a leave request is approved, the employee should be excluded from scheduling during that date range. If shifts are already assigned to that employee during the approved leave period, those shifts should become open or needs-coverage shifts so other employees can apply and managers can approve or directly assign coverage.

### OpenShiftApplication

Represents an employee applying for an open shift.

Fields:

- Open shift
- Applying employee
- Status: pending, approved, declined

## State Flows

### Coverage Request With Substitute

1. Employee selects one of their own shifts.
2. Employee creates a coverage request and specifies a substitute employee from one of their teams.
3. Manager reviews the request.
4. Manager sees conflict, position, and hours warnings.
5. If approved, the shift assignment changes from the original employee to the substitute employee.
6. If declined, the original shift remains assigned to the original employee.

### Absence Request Without Substitute

1. Employee selects one of their own shifts.
2. Employee creates an absence or leave request without a substitute.
3. Manager reviews the request.
4. If approved, the original shift becomes an open shift.
5. Manager later assigns an employee, or an employee applies and manager approves.

### Planned Leave Request

1. Employee creates a leave request with a start date and end date.
2. Manager or owner reviews the date range.
3. If declined, the employee remains eligible for shifts during that period.
4. If approved, the employee is excluded from scheduling during that date range.
5. Existing shifts assigned to that employee during the approved leave period become open or needs-coverage shifts.
6. Other employees can apply for those open shifts.
7. Manager or owner approves applications or directly assigns employees to cover the shifts.

### Manager-Created Open Shift

1. Manager creates an open shift.
2. Manager selects the date, time, position, and reason.
3. Reason can be extra staffing, new coverage need, manual, or another operational reason.
4. Employees can apply, or the manager can directly assign an employee.
5. Once assigned, the shift becomes scheduled.

## Overtime and Payroll Expansion

The MVP does not calculate payroll or settle overtime. It only shows manager-only warnings for increased hours and possible overtime risk.

This direction can change in the future. The important MVP principle is to avoid mixing planned schedules, change requests, actual work records, and payroll summaries into one model.

Future models may include:

### TimeClockRecord

Represents actual clock-in and clock-out time. It may also be called `ActualWorkRecord` in later implementation planning.

Fields may include:

- Employee
- Shift, optional
- Clock-in time
- Clock-out time
- Break duration
- Manager adjustment
- Approval status
- Notes, such as early leave or late arrival

### PayrollPeriod

Represents a pay period, such as weekly or biweekly.

### EmployeePosition

Represents a future employee-to-position qualification with payroll metadata.

Fields may include:

- Employee
- Position
- Default hourly rate for that position
- Active status
- Effective start date
- Effective end date, optional

The MVP should use `Employee.positions` directly. Add `EmployeePosition` when the product needs per-position pay rates, qualification status, or historical rate changes.

### OvertimeRule

Represents configurable overtime rules by province, region, policy, and effective date.

### PayrollSummary

Represents payroll results for a period.

Fields may include:

- Regular hours
- Overtime hours
- Gross pay estimate
- Export status

The future payroll feature should use actual work records, not only scheduled shifts. This keeps the planned schedule separate from actual attendance, early leave, late arrival, breaks, paid hours, and payroll export.

## Authentication, Tenant, and Invitation Expansion

Authentication, tenant management, email invitations, password reset, and multi-location access control are future expansion features. The MVP should keep entity boundaries compatible with future Company, Location, UserAccount, and Employee separation, but should not implement the full login and invitation system yet.

The API should also leave room for mobile authentication. The first PWA client may use a simple development-friendly login flow, but future React Native support may require token-based authentication, refresh tokens, device sessions, push notification device tokens, and app-version-aware API behavior.

The future model should support:

### Company or Tenant

Represents the restaurant business or customer account using the service.

Examples:

- A sushi restaurant company
- A restaurant group
- A single independent restaurant

### Location

Represents a specific branch or store under a company.

Examples:

- Main location
- Second location
- Downtown branch

The MVP is single-location, but future data should be able to associate schedules, shifts, teams, positions, employees, and requests with a location.

### UserAccount

Represents the login identity.

UserAccount should be separate from Employee so one login identity can later be connected to employee records, owner records, or cross-location access rules.

Future fields may include:

- Email
- Username
- Password hash
- Account status
- Last login time
- Password reset state

### Invitation

Represents an email invitation for owners, managers, or employees.

Future owner onboarding flow:

1. Service operator creates a new company or tenant.
2. Service operator sends an invitation email to the owner.
3. Owner accepts the invitation and creates a login account.
4. Owner logs in and only sees their own company and location data.
5. Owner creates employees for the location.

Future employee onboarding flow:

1. Owner or manager creates an employee.
2. System sends the employee an invitation email.
3. Employee sets their own password through the invitation link.
4. Employee logs in and only sees the allowed location and employee-facing data.
5. Employee can later change or reset their password.

For security, the future production system should prefer invitation links where users set their own passwords. Sending raw usernames and passwords by email should be avoided for real service deployment.

### Multi-Location Access Control

Future access rules should ensure:

- Owners only see companies and locations they own or manage.
- Managers only see locations they are assigned to.
- Employees only see their own allowed location data and schedule information.
- A user can potentially have access to more than one location.

This is future scope. The MVP should stay lightweight and may use a simple development user or basic single-location account assumption while the domain model is built.

## Notifications

MVP notifications are simple status notifications inside the app.

Events:

- Employee creates a request, notifying managers.
- Manager approves or declines a request, notifying the employee.
- Manager creates an open shift, notifying relevant employees.
- Manager assigns an open shift, notifying the assigned employee.
- Urgent open shifts appear on the manager home screen.

SMS, email, and push notifications can be added later.

## Approval Warnings

Before approval, managers should see warnings for:

- Substitute has a time conflict.
- Substitute has a different position.
- Shift remains uncovered after approval.
- Approved leave creates shifts that need coverage.
- Open shift is within the next 3 days and still unassigned.
- Approval increases the employee's expected weekly hours.
- Approval may create overtime risk.

Warnings do not automatically block approval in the MVP.

## Test Criteria

The MVP should be considered functionally covered when these flows work:

- Employee creates a coverage request with a substitute and it appears in the manager queue.
- Employee cannot select a substitute who does not share a team with them.
- Employee can select a substitute who shares at least one team, even if either employee belongs to multiple teams.
- Manager approves the substitute request and the shift assignment changes.
- Manager declines the substitute request and the original assignment remains unchanged.
- Employee creates an absence request without a substitute.
- Manager approves the absence request and the shift becomes open.
- Employee creates a planned leave request for a date range.
- Manager approves the planned leave request and the employee is excluded from scheduling during that period.
- Existing shifts assigned during approved leave become open or needs-coverage shifts.
- Manager creates an open shift manually.
- Employee applies for an open shift.
- Manager approves an open shift application and the shift becomes assigned.
- Manager directly assigns an employee to an open shift.
- Weekly pattern generates the correct shifts for a specific date range.
- Fixed weekly pattern generates the correct shifts for the selected date range.
- Future published schedule periods can be modeled for 1 week, 2 weeks, 3 weeks, 1 month, or a custom date range.
- Employee home shows the next 3 days of schedule.
- Weekly view shows who is working during the selected week.
- Monthly view shows who is working during the selected month.
- Employee cannot approve requests or assign shifts.
- Manager and owner can approve requests and assign shifts.
- Manager-only warnings are not visible to regular employees.
- React/Vite PWA can call the backend through `/api/**` JSON endpoints.
- A future React Native client can reuse the same core API contracts without rewriting backend domain logic.

## Initial Implementation Recommendation

Build the MVP around shift coverage first, not payroll and not a full scheduling suite.

The recommended build order is:

1. Employees, roles, teams, and positions.
2. Fixed weekly schedule patterns.
3. Shift generation and read-only schedule views.
4. Employee coverage, absence, and planned leave requests.
5. Manager approval queue.
6. Open shift creation, application, and assignment.
7. Manager-only warnings.
8. Basic in-app notifications.

This keeps the first version light while preserving a clean path toward payroll, overtime, multi-location support, and richer workforce management later.
