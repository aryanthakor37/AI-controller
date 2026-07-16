# Android Architecture & Integration Plan

## Foundation Architecture
The AI Mobile Control Agent Android application is structured using Clean Architecture and MVVM principles.

### Directory Structure
- `com.aimobile.ui`: Jetpack Compose views, ViewModels, and Navigation.
- `com.aimobile.domain`: Business logic, abstract interfaces, and use cases.
- `com.aimobile.data`: Repositories, Room Database (local storage), and Retrofit API services.
- `com.aimobile.di`: Dagger-Hilt injection modules.
- `com.aimobile.services`: Background services like `MainService` to keep the connection alive.
- `com.aimobile.managers`: Concrete singleton classes (PermissionManager, DeviceInfoManager, ConnectionManager).

## Permission Flow
The `PermissionScreen` will recursively request permissions through the Android system based on API levels (e.g. `POST_NOTIFICATIONS` on API 33+). The app will not proceed to the Home screen until critical permissions are granted.
*Note: Accessibility Service is intentionally omitted at this stage.*

## Lifecycle Flow
1. **App Start (`AiMobileApp.kt`)**: Initializes Hilt and Logging.
2. **MainActivity**: Launches `AppNavigation` in Compose.
3. **Splash Screen**: Initial load, checks if settings exist in Room database.
4. **Permissions Check**: Routes to Permission Screen if any are missing.
5. **Foreground Service**: Starts the `MainService` to maintain persistence.
6. **Dashboard**: Connects UI to managers to display live metrics.

## Future Integration (Phase 7+)
When integrating the MERN backend:
1. `ConnectionManager` will instantiate `io.socket.client.Socket` connecting to the Node.js server.
2. Socket events will be funneled into Kotlin `SharedFlow` objects for UI reactivity.
3. Socket commands will trigger local device actions via `CommandReceiver`.
