// build.gradle.kts (nivel de proyecto)
// Aquí se declaran los plugins que usará el proyecto completo.
// No se aplican directamente aquí, solo se ponen a disposición de los módulos (apply false).

plugins {
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false // Necesario para Room
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false
}
