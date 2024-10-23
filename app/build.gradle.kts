
plugins {
	application
}

repositories {
	mavenCentral()
}

dependencies {
	testImplementation(libs.junit.jupiter)

	testRuntimeOnly("org.junit.platform:junit-platform-launcher")

	implementation(libs.guava)
	implementation("org.bytedeco:javacpp:1.5.10")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

application {
	mainClass = "cct.luau.cbindings.App"
}

tasks.named<Test>("test") {
	useJUnitPlatform()
}

val repoUrl = "https://github.com/luau-lang/luau.git"
val outputDir = layout.buildDirectory.dir("luau")
tasks.register<Exec>("cloneLuauRepo") {
	// Use layout.buildDirectory instead of buildDir
	val destinationDir = File(layout.buildDirectory.asFile.get(), "luau")

	doFirst {
		if (destinationDir.exists()) {
			destinationDir.deleteRecursively()
		}
	}

	commandLine("git", "clone", repoUrl, destinationDir.absolutePath)
}

tasks.register<Exec>("buildLuaU") {
	dependsOn("cloneLuauRepo")

	val luaUPath = outputDir.get().asFile
	workingDir(luaUPath)

	commandLine("cmake", luaUPath)
	commandLine("make")
}

tasks.register<Copy>("cloneLuaU_VMsrc") {
	from("build/luau/VM/src") {
		include("**/*")
	}
	into("cpp/src")
}

tasks.register<Copy>("cloneLuaU_VMinclude") {
	from("build/luau/VM/include") {
		include("**/*")
	}
	into("cpp/include")
}

tasks.register<Copy>("cloneLuaU_CompilerSrc") {
	from("build/luau/Compiler/src") {
		include("**/*")
	}
	into("cpp/src")
}

tasks.register<Copy>("cloneLuaU_CompilerInclude") {
	from("build/luau/Compiler/include") {
		include("**/*")
	}
	into("cpp/include")
}

// Task to clone luau compiled files
tasks.register("cloneLuaU") {

}

tasks.getByName("cloneLuaU").dependsOn("cloneLuaU_VMsrc")
tasks.getByName("cloneLuaU").dependsOn("cloneLuaU_VMinclude")
tasks.getByName("cloneLuaU").dependsOn("cloneLuaU_CompilerSrc")
tasks.getByName("cloneLuaU").dependsOn("cloneLuaU_CompilerInclude")
tasks.getByName("build").dependsOn("cloneLuaU")
