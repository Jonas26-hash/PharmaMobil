import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        KoinInitKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}