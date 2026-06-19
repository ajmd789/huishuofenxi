import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

dependencies {
    implementation(projects.shared)

    implementation(compose.desktop.currentOs)
    implementation(libs.compose.material3)
    implementation(libs.kotlinx.coroutinesSwing)
    implementation(libs.apache.poi.ooxml)

    implementation(libs.compose.uiToolingPreview)
    // 核心导航库
    implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
    // 页面切换动画库 (可选，但推荐，很丝滑)
    implementation("cafe.adriel.voyager:voyager-transitions:1.0.0")
    testImplementation(libs.junit)
}

compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "org.example.project"
            packageVersion = "1.0.0"
        }
    }
}
