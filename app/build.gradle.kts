
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

tasks.register<Copy>("cloneCompiler") {
	//dependsOn("buildLuaU") // Ensure this task runs after building LuaU

	val luaUPath = outputDir.get().asFile
	val includeSrcDir = File(luaUPath, "Compiler/include")
	val includeDestDir = File("app/cpp/include")

	val srcDir = File(luaUPath, "Compiler/src")
	val srcDestDir = File("app/cpp/src")

	includeDestDir.mkdirs()
	srcDestDir.mkdirs()

	// Copy Compiler header files
	from(File(includeSrcDir, "Compiler")) {
		into(includeDestDir)
	}

	// Copy Compiler source files
	from(File(srcDir, "Compiler")) {
		into(srcDestDir)
	}
}

// Task to clone VM files
tasks.register<Copy>("cloneVM") {
	//dependsOn("buildLuaU") // Ensure this task runs after building LuaU

	val luaUPath = outputDir.get().asFile
	val includeSrcDir = File(luaUPath, "VM/include")
	val includeDestDir = File("app/cpp/include")

	val srcDir = File(luaUPath, "VM/src")
	val srcDestDir = File("app/cpp/src")

	includeDestDir.mkdirs()
	srcDestDir.mkdirs()

	// Copy VM header files
	from(File(includeSrcDir, "VM")) {
		into(includeDestDir)
	}

	// Copy VM source files
	from(File(srcDir, "VM")) {
		into(srcDestDir)
	}
}
