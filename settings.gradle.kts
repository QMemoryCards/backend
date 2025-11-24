rootProject.name = "backend"

buildCache {
    local {
        directory = File(File(rootDir, ".gradle").toString(), "build-cache")
        isEnabled = true
    }
}
