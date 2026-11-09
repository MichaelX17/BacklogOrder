# Detekt Remediation Table

| Issue Type | File:Line | Description / Suggested Fix |
| :--- | :--- | :--- |
| **NestedBlockDepth** | [RoomSettingsRepository.kt:69](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/data/repository/RoomSettingsRepository.kt#L69) | Reduce nesting depth in `exportData()` by extracting helper functions. |
| **EmptyFunctionBlock** | [RoomSettingsRepositoryTest.kt:39](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/test/java/com/backlogorder/data/repository/RoomSettingsRepositoryTest.kt#L39) | Add a comment or assertion inside test stub `setKey`. |
| **EmptyFunctionBlock** | [RoomSettingsRepositoryTest.kt:40](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/test/java/com/backlogorder/data/repository/RoomSettingsRepositoryTest.kt#L40) | Add a comment or assertion inside test stub `clear`. |
| **SwallowedException** | [Entities.kt:117](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/data/local/Entities.kt#L117) | Log caught `IllegalArgumentException` rather than swallowing. |
| **SwallowedException** | [InteractiveCropDialog.kt:69](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/ui/common/InteractiveCropDialog.kt#L69) | Log caught `Exception`. |
| **SwallowedException** | [InteractiveCropDialog.kt:228](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/ui/common/InteractiveCropDialog.kt#L228) | Log caught `Exception`. |
| **SwallowedException** | [OnboardingScreen.kt:80](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/ui/onboarding/OnboardingScreen.kt#L80) | Log caught errors in `RawgApiKeyValidator`. |
| **SwallowedException** | [Theme.kt:136](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/ui/theme/Theme.kt#L136) | Log caught `Exception`. |
| **SwallowedException** | [GameDetailScreen.kt:345](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/ui/gamedetail/GameDetailScreen.kt#L345) | Log caught `Exception`. |
| **ImplicitDefaultLocale** | [SettingsScreen.kt:341](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/ui/settings/SettingsScreen.kt#L341) | Provide explicit `Locale.ROOT` to `String.format`. |
| **UnusedPrivateProperty** | [RoomSettingsRepository.kt:37](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/data/repository/RoomSettingsRepository.kt#L37) | Remove unused `apiKeyProvider` if unneeded. |
| **UnusedParameter** | [ScoreCalculator.kt:21](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/domain/score/ScoreCalculator.kt#L21) | Remove unused parameter `playtimeHours`. |
| **UnusedParameter** | [ScoreCalculator.kt:21](file:///home/miguel/AndroidStudioProjects/BacklogOrder/app/src/main/java/com/backlogorder/domain/score/ScoreCalculator.kt#L21) | Remove unused parameter `isInList`. |
