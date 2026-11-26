import Foundation
import UserNotifications

public enum DailyPracticeNotificationManager {

    private static let reminderIdentifier = "pianoflow_daily_practice_reminder"

    public static func requestPermissionAndSchedule() async -> Bool {
        let center = UNUserNotificationCenter.current()
        do {
            let granted = try await center.requestAuthorization(options: [.alert, .sound, .badge])
            if granted {
                await scheduleDailyReminder()
            }
            return granted
        } catch {
            return false
        }
    }

    public static func scheduleDailyReminder(hour: Int = 20, minute: Int = 0) async {
        let center = UNUserNotificationCenter.current()
        center.removePendingNotificationRequests(withIdentifiers: [reminderIdentifier])

        let content = UNMutableNotificationContent()
        content.title = L10n.tr("reminder_notification_title")
        content.body = L10n.tr("reminder_notification_body")
        content.sound = .default

        var dateComponents = DateComponents()
        dateComponents.hour = hour
        dateComponents.minute = minute

        let trigger = UNCalendarNotificationTrigger(dateMatching: dateComponents, repeats: true)
        let request = UNNotificationRequest(identifier: reminderIdentifier, content: content, trigger: trigger)

        try? await center.add(request)
    }

    public static func cancelReminder() {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: [reminderIdentifier])
    }
}
