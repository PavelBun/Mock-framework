plugins {
    java
    id("java-library")
}

version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("net.bytebuddy:byte-buddy:1.14.19")
    implementation("net.bytebuddy:byte-buddy-agent:1.14.19")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.jar {
    manifest {
        attributes(
            "Premain-Class" to "Mockframework.Static.agent.MockAgent",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true"
        )
    }
}

tasks.test {
    dependsOn(tasks.jar)
    useJUnitPlatform()
    jvmArgs("-javaagent:${tasks.jar.get().archiveFile.get().asFile.absolutePath}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}