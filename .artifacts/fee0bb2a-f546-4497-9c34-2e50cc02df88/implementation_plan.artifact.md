# Implementation Plan - Final Fixes and Verification

The goal is to resolve the "STRING_TOO_LARGE" build error by simplifying `logo_riau.xml`, verify the dynamic API key logic, ensure correct year-based data fetching, and perform final tests.

## User Review Required

> [!IMPORTANT]
> The `logo_riau.xml` vector drawable is extremely complex (converted from SVG), which causes build failures. I will simplify it by removing ultra-fine details and splitting large path data into multiple paths.

## Proposed Changes

### Resources

#### [MODIFY] [logo_riau.xml](file:///D:/Apps/Android/InfoPajak/app/src/main/res/drawable/logo_riau.xml)
- Simplify complex paths to reduce string length.
- Split extremely long `pathData` strings into multiple smaller `<path>` tags if necessary.
- *Strategy*: Use a more optimized version of the vector drawable to ensure build compatibility.

### UI & Logic

#### [MODIFY] [PajakViewModel.kt](file:///D:/Apps/Android/InfoPajak/app/src/main/java/com/example/infopajak/ui/PajakViewModel.kt)
- Double-check that all `fetchPajak...` methods use the dynamic `generateApiKey(year)` function.
- Ensure state handling correctly resets when changing years or categories.

#### [MODIFY] [PajakScreen.kt](file:///D:/Apps/Android/InfoPajak/app/src/main/java/com/example/infopajak/ui/PajakScreen.kt)
- Verify the "Tahun Ini" button sets the correct date range (Jan 1 to Dec 31 of current year).
- Ensure chart interactions (bar/line) are smooth and labels are correct.

## Verification Plan

### Automated Tests
- Build the app using `gradle_build("app:assembleDebug")` to confirm the "STRING_TOO_LARGE" error is gone.
- Run the app and check logcat for any Retrofit or Gson errors during data fetching.

### Manual Verification
- **Splash Screen**: Confirm the pulsing animation works and transitions to the Main Screen.
- **Main Dashboard**: Select different years and categories; verify data updates.
- **Analytics**: Switch between Daily and Monthly trends; confirm charts render correctly.
- **Data Accuracy**: Check if PKB, BBNKB, etc., are displayed in the requested order and format.
