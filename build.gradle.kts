import com.avast.gradle.dockercompose.tasks.ComposeUp
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.google.protobuf.gradle.ExecutableLocator
import de.undercouch.gradle.tasks.download.Download
import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
    base
    id("com.avast.gradle.docker-compose") version "0.9.5"
    id("com.bmuschko.docker-remote-api") version "4.10.0"
    id("de.undercouch.download") version "3.4.3"
    id("org.unbroken-dome.test-sets") version "2.1.1"
    id("com.google.protobuf") version "0.8.10"
}

val dockerImageName = project.name
val dockerImageVersion = project.version
val dockerComposeCacheDir = "$rootDir/.gradle/docker-compose/"
val dockerComposeVersion: String by extra
val dockerComposeScript = "$dockerComposeCacheDir/$dockerComposeVersion/docker-compose.sh"
val isOsLinux = System.getProperty("os.name").toLowerCase().contains("linux")
val dockerComposeCommand = if (isOsLinux && project.hasProperty("dockerComposeVersion")) dockerComposeScript else "docker-compose"
val pbVersion = "3.6.1"
val dockerfileProperties = mapOf(
        "project.name" to project.name,
        "project.version" to project.version
)
val composeProperties = mapOf(
        "version" to project.version
)

testSets {
    create("functionalTest") {
        extendsFrom(unitTest.get())
    }
}

docker {
    if (project.hasProperty("dockerUri")) {
        url.set(project.properties["dockerUri"] as String)
    } else if (System.getenv()["DOCKER_HOST"] != null) {
        url.set(System.getenv()["DOCKER_HOST"])
    }
    if (project.hasProperty("dockerCerts")) {
        certPath.set(file(project.properties["dockerCerts"] as String))
    } else if (System.getenv()["DOCKER_CERT_PATH"] != null) {
        certPath.set(file(System.getenv()["DOCKER_CERT_PATH"] as String))
    }
}

protobuf {
    this.protobuf.protoc(closureOf<ExecutableLocator> {
        artifact = "com.google.protobuf:protoc:$pbVersion"
    })
}


repositories {
    jcenter()
}

dockerCompose {
    dockerComposeWorkingDirectory = "$buildDir/docker-compose"
    captureContainersOutputToFile = file("$buildDir/docker-compose/compose.log")
    useComposeFiles = listOf("docker-compose.yml")
    projectName = "proxy"
    executable = dockerComposeCommand
    waitForTcpPorts = false
    removeVolumes = true
    removeOrphans = true
    removeContainers = true
}

val functionalTestImplementation: Configuration by configurations.getting

dependencies {
    implementation("com.google.protobuf:protobuf-java:$pbVersion")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.5.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")

    functionalTestImplementation("ch.qos.logback:logback-core:1.2.3")
}

tasks {
    wrapper {
        distributionType = Wrapper.DistributionType.ALL
        gradleVersion = "5.6.2"
    }

    val downloadDockerCompose by creating(Download::class) {
        group = "build setup"

        val composeVersion = project.findProperty("dockerComposeVersion")
        val composeSrc = "https://github.com/docker/compose/releases/download/$composeVersion/docker-compose-linux-x86_64"

        inputs.property("composeVersion", composeVersion)
        overwrite(false)

        src(composeSrc)
        dest(dockerComposeScript)

        onlyIf { isOsLinux && composeVersion != null }
        doFirst { delete(dockerComposeCacheDir) }
        doLast { file(dockerComposeScript).setExecutable(true) }
    }

    val copyDockerComposeResources by creating(Sync::class) {
        group = "docker"
        dependsOn(downloadDockerCompose)
        inputs.properties(dockerfileProperties)

        from("docker-compose") {
            filter<ReplaceTokens>("tokens" to dockerfileProperties)
        }
        into("$buildDir/docker-compose")
    }

    val copyDockerResources by creating(Sync::class) {
        group = "docker"
        dependsOn(downloadDockerCompose, assemble)
        inputs.properties(dockerfileProperties)

        from("src/main/docker") {
            filter<ReplaceTokens>("tokens" to dockerfileProperties)
        }
        from("$buildDir/libs") {
            include("${project.name}-${project.version}.jar")
            rename("${project.name}-${project.version}.jar", "${project.name}.jar")
        }
        into("$buildDir/docker")
    }

    val buildDockerImage by creating(DockerBuildImage::class) {
        group = "docker"
        dependsOn(copyDockerResources)
        inputDir.set(copyDockerResources.destinationDir)
        tags.add("$dockerImageName:$dockerImageVersion")
    }

    withType<ComposeUp> {
        dependsOn(copyDockerComposeResources, buildDockerImage)
    }

    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
            freeCompilerArgs = listOf("-Xjsr305=strict")
        }
    }

    val functionalTest = "functionalTest"(Test::class) {
        useJUnitPlatform()
        testLogging {
            showStackTraces = true
            showStandardStreams = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }

    check {
        dependsOn(functionalTest)
    }
}