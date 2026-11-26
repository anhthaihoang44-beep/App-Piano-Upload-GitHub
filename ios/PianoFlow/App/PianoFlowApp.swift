import SwiftData
import SwiftUI

@main
struct PianoFlowApp: App {

    @StateObject private var container = AppContainer()

    var body: some Scene {
        WindowGroup {
            PianoFlowRootView(container: container)
                .preferredColorScheme(.dark)
                .task {
                    await container.bootstrap()
                }
        }
        .modelContainer(container.dataStore.container)
    }
}
