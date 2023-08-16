val publishVersion by extra {
    System.getenv("VERSION_NAME") ?: "1.0-SNAPSHOT"
}

val publishVersionCode by extra {
    Runtime.getRuntime()
        .exec("git rev-list HEAD --count").inputStream.bufferedReader()
        .readText()
        .trim()
}
