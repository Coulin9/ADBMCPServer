## ADDED Requirements

### Requirement: Query device basic info on single device
The system SHALL provide a tool `adb_get_device_info` that returns device basic information including brand, model, Android version, chipset, memory, screen, storage, and battery when a single device is connected.

#### Scenario: Single device connected, no serial provided
- **WHEN** a single Android device is connected and `adb_get_device_info` is called without serial parameter
- **THEN** the tool returns a formatted text containing: brand, model, product name, Android version (with API level), chipset platform, hardware name, total memory, screen resolution, screen DPI, internal storage capacity, and battery status

### Requirement: Query device info with serial on specific device
The system SHALL support an optional `serial` parameter to target a specific device when multiple devices are connected.

#### Scenario: Multiple devices connected, serial provided
- **WHEN** multiple Android devices are connected and `adb_get_device_info` is called with a valid device serial
- **THEN** the tool returns device info for the specified device only

#### Scenario: Single device connected, serial provided
- **WHEN** a single Android device is connected and `adb_get_device_info` is called with that device's serial
- **THEN** the tool returns device info for that device

### Requirement: Error when multiple devices connected without serial
The system SHALL return an error when multiple devices are connected but no serial parameter is provided.

#### Scenario: Multiple devices connected, no serial provided
- **WHEN** multiple Android devices are connected and `adb_get_device_info` is called without serial parameter
- **THEN** the tool returns an error indicating multiple devices are connected and a serial must be specified

### Requirement: Error when no device connected
The system SHALL return an error when no Android device is connected.

#### Scenario: No device connected
- **WHEN** no Android device is connected and `adb_get_device_info` is called
- **THEN** the tool returns an error indicating no device is connected

### Requirement: Partial info on command failure
The system SHALL return available information even if some underlying ADB commands fail.

#### Scenario: Some commands succeed, some fail
- **WHEN** `adb_get_device_info` is called and some property queries succeed while others fail
- **THEN** the tool returns successfully retrieved information with failed sections showing error indicators rather than failing entirely
