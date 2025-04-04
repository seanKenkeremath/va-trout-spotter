# VA Trout Spotter

<img src="app/src/main/ic_launcher-playstore.png" width="300">

## Overview

VA Trout Spotter is an Android app that provides Virginia anglers with up-to-date information about trout stockings across the state. The app scrapes the Virginia Department of Wildlife Resources (DWR) website for trout stocking information, including which bodies of water have been stocked, when they were stocked, and what types of trout were added. Key features include:

- **Customizable Notifications**: Subscribe to specific counties or stocking locations to receive notifications when new stockings occur
- **Offline Access**: All stocking data is stored locally, allowing anglers to access information even without cell service
- **Comprehensive Filtering**: Filter stockings by county and other criteria
- **Historical Data**: Access years of historical stocking data to identify patterns and plan future trips

## Technical Implementation

### Data Collection & Storage

The app uses a multi-layered approach to gather and manage stocking data:

1. **Web Scraping**: Uses Jsoup to parse the Virginia DWR's stocking website, extracting structured data from HTML tables
2. **Room Database**: Implements a Room database with carefully designed entities and indices for efficient querying and pagination
3. **Repository Pattern**: Abstracts data sources behind repositories, allowing seamless switching between network and local data

### Background Processing

VA Trout Spotter leverages WorkManager for efficient background operations:

1. **Periodic Stocking Updates**: Schedules network requests every 12 hours when on unmetered connections (WiFi) to minimize data usage
2. **Historical Data Fetching**: Performs a one-time fetch of historical stocking data (dating back to 2018) when the app is first installed
3. **Work Constraints**: Implements battery-friendly constraints to ensure background work only runs under optimal conditions

### Notifications

The app implements a sophisticated notification system:

1. **Subscription Management**: Users can subscribe to specific counties or stocking locations
2. **Targeted Notifications**: When new stockings are detected, the app checks against user subscriptions and sends notifications
3. **Notification Channels**: Uses Android's notification channels for proper categorization and user control

### Architecture & Patterns

VA Trout Spotter follows modern Android architecture principles:

1. **MVVM Architecture**: Clear separation of UI, business logic, and data layers
2. **Use Cases**: Implements focused use cases for specific app operations (e.g., `FetchAndNotifyStockingsUseCase`)
3. **Dependency Injection**: Uses Hilt for clean, testable dependency management
4. **Kotlin Coroutines & Flow**: Leverages structured concurrency and reactive programming for asynchronous operations
5. **Pagination**: Custom pagination implementation for efficiently loading large datasets

### UI Implementation

1. **Jetpack Compose**: Built entirely with Compose for a modern, declarative UI
2. **Material 3**: Implements the latest Material Design guidelines with dynamic theming
3. **Responsive Design**: Adapts to different screen sizes and orientations
4. **Animation**: Custom animations for loading states and transitions

### Testing

1. **Unit Tests**: Comprehensive test coverage for ViewModels, repositories, and use cases
2. **Mockk**: Uses Mockk for flexible mocking in tests
3. **Test Dispatchers**: Implements testable coroutine dispatchers for predictable async testing

## Libraries & Technologies

- **UI**: Jetpack Compose, Material 3
- **Architecture**: MVVM, Repository Pattern, Use Cases
- **Concurrency**: Kotlin Coroutines, Flow
- **Dependency Injection**: Hilt
- **Local Storage**: Room, DataStore
- **Background Processing**: WorkManager
- **Web Scraping**: Jsoup
- **Testing**: JUnit, Mockk, Robolectric

## Future Enhancements

- Weather data integration
- Log catches and other notes

## Contributing

Contributions are welcome! Please feel free to file an issue or submit a Pull Request.

## License

This project and its code are licensed under the terms of the Apache License 2.0.
