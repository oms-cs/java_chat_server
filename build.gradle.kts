plugins {
    id("java")
    id("application")
}

group = "io.nemesis"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

application {
    mainClass = "io.nemesis.Application"
}

tasks.test {
    useJUnitPlatform()
}