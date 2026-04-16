## ADDED Requirements

### Requirement: Query all foreground services
The system SHALL provide a tool `adb_foreground_services` that queries all Android Foreground Services currently running on the device when no package name is specified.

#### Scenario: Device has foreground services running
- **WHEN** the tool is called without a `packageName` parameter
- **THEN** the system executes `adb shell dumpsys activity services`, filters output to retain only service record blocks containing `isForeground=true`, and returns the filtered output as text

#### Scenario: No foreground services running
- **WHEN** the tool is called without a `packageName` parameter and no services have `isForeground=true`
- **THEN** the system returns the text "没有正在运行的前台服务"

### Requirement: Query foreground services by package name
The system SHALL support an optional `packageName` parameter to query foreground services for a specific application.

#### Scenario: Package has foreground services running
- **WHEN** the tool is called with `packageName` set to a valid package name (e.g., `com.example.app`)
- **THEN** the system executes `adb shell dumpsys activity services com.example.app`, filters output to retain only service record blocks containing `isForeground=true`, and returns the filtered output as text

#### Scenario: Package has no foreground services
- **WHEN** the tool is called with `packageName` set to a valid package name that has no foreground services
- **THEN** the system returns the text "没有正在运行的前台服务"

#### Scenario: Invalid package name
- **WHEN** the tool is called with `packageName` set to a non-existent package name
- **THEN** the system returns the raw dumpsys output (which will contain no service records)

### Requirement: Filter output to foreground records only
The system SHALL filter the dumpsys output to return only complete service record blocks where the service has `isForeground=true`.

#### Scenario: Mixed foreground and background services
- **WHEN** dumpsys output contains both foreground (`isForeground=true`) and background (`isForeground=false`) services
- **THEN** the system returns only the complete record blocks (from `* ServiceRecord` through the next `* ServiceRecord` or end of output) for services marked `isForeground=true`, preserving all fields within those blocks (intent, app, createTime, foregroundNoti, etc.)
