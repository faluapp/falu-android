val ktlint by configurations.creating

dependencies {
    ktlint("com.pinterest:ktlint:0.34.2")
}

tasks {
    val ktlint by creating(JavaExec::class) {
        group = "verification"
        description = "Check Kotlin code style."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = configurations["ktlint"]
        args("src/**/*.kt")
    }

    val checkTask = this.getByName("check")
    checkTask.dependsOn(ktlint)

    val ktlintFormat by creating(JavaExec::class) {
        group = "formatting"
        description = "Fix Kotlin code style deviations."
        mainClass.set("com.pinterest.ktlint.Main")
        classpath = configurations["ktlint"]
        args("-F", "src/**/*.kt")
    }
}