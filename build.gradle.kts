plugins {
    id("com.github.johnrengelman.shadow") version "7.1.0"
    java
    `maven-publish`
}

repositories {
    mavenCentral()
    maven("https://m2.dv8tion.net/releases")
    maven {
        url = uri("https://jcenter.bintray.com")
    }

    maven {
        url = uri("https://dl.bintray.com/kodehawa/maven")
    }

    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation("org.slf4j", "slf4j-api", "1.7.32")
    implementation("org.apache.logging.log4j", "log4j-core", "2.17.0")
    implementation("org.apache.logging.log4j", "log4j-slf4j-impl", "2.17.0")
    implementation("org.apache.commons", "commons-lang3", "3.12.0")
    implementation("org.apache.commons", "commons-text", "1.9")
    implementation("com.sparkjava", "spark-core", "2.9.3")
    implementation("postgresql", "postgresql", "9.3-1102.jdbc41")
    implementation("net.dv8tion", "JDA", "4.3.0_339") {
        exclude(module = "opus-java")
    }
    implementation("club.minnced", "discord-webhooks", "0.7.4")
    implementation("com.google.api-client", "google-api-client", "1.32.2")
    implementation("com.squareup.retrofit2", "retrofit", "2.9.0")
    implementation("com.google.code.gson", "gson", "2.8.9")
    implementation("org.yaml", "snakeyaml", "1.30")
    implementation("org.postgresql", "postgresql", "42.3.1")
    implementation("info.debatty", "java-string-similarity", "2.0.0")
    implementation("com.google.guava", "guava", "31.0.1-jre")
    compileOnly("org.projectlombok", "lombok", "1.18.22")
    annotationProcessor("org.projectlombok", "lombok", "1.18.22")

    // unit testing
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testAnnotationProcessor("org.projectlombok", "lombok", "1.18.22")
    testCompileOnly("org.projectlombok", "lombok", "1.18.22")
}

group = "ShepardBot"
version = "2.0.9"
description = "ShepardBot"

java {
    sourceCompatibility = JavaVersion.VERSION_15
}


tasks {
    shadowJar {
        mergeServiceFiles()
        manifest {
            attributes(mapOf("Main-Class" to "de.eldoria.shepard.ShepardBot"))
        }
    }
    build {
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = "UTF-8"
    }

    javadoc {
        options.encoding = "UTF-8"
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}
