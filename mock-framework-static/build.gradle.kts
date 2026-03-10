plugins {
    `java-library`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.gradle.api.tasks.compile.JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.jar {
    manifest {
        attributes(
            "Premain-Class" to "ru.pozhidaev.mockframework.agent.MockAgent",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true"
        )
    }
}

dependencies {
    implementation(project(":mock-framework-core"))

    implementation("net.bytebuddy:byte-buddy:1.14.19")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.19")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    dependsOn(tasks.jar)
    doFirst {
        jvmArgs("-javaagent:${tasks.jar.get().archiveFile.get().asFile.absolutePath}")
    }
}
