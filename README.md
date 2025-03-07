# VA Trout Buddy (WIP)

## Overview

VA Trout Buddy is an app that scrapes the Virginia Department of Gaming and Inland Fisheries (VGDIF) website for trout stocking information. This includes which bodies of water have been stocked, when, and with what types of trout. The app stores this information offline so it can be accessed efficiently without a network connection. It also supports subscribing to stocking notifications for specific locations **(TODO)**.

## Technical details
One of the reasons I created this app, other than my own trout fishing aspirations, was to create a simple Android app using the latest and greatest tech stack. Specifically, I wanted to do a deep dive on optimizing UI with Compose with Material3. I also am using it as a way to test out newer AI workflows such as Cursor.

### Libraries/patterns used

 - Jetpack Compose/Material3
 - MVVM
 - Room
 - Hilt
 - Jsoup (web scraping)
 - Mockk
 - Robolectric

### Architecture
**TODO**

## TODO

 - Implement actual UI (currently stubbed)
 - Improve filter flow
 - Logo, loading animation
 - Empty state
 - Notifications (subscribing and sending local notifications from WorkManager)
 - Settings screen (About, data management)
 - Sanitize locations from web scraping
 - Nightly integration tests of web scraping
 - Locations tab with directions and more details (future work)
