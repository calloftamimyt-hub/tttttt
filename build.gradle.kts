// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
  alias(libs.plugins.google.services) apply false
}

tasks.register("importGithubRepo") {
  doLast {
    val zipUrl = java.net.URL("https://github.com/circlebazarchannel-commits/lamia/archive/refs/heads/main.zip")
    val tempZip = file("build/temp_repo.zip")
    tempZip.parentFile.mkdirs()
    zipUrl.openStream().use { input ->
      tempZip.outputStream().use { output ->
        input.copyTo(output)
      }
    }
    val zip = java.util.zip.ZipFile(tempZip)
    for (entry in zip.entries()) {
      if (!entry.isDirectory) {
        val name = entry.name
        val relPath = name.substringAfter("/")
        if (relPath.isNotEmpty() && !relPath.startsWith(".git")) {
          val destFile = file(relPath)
          destFile.parentFile.mkdirs()
          zip.getInputStream(entry).use { input ->
            destFile.outputStream().use { output ->
              input.copyTo(output)
            }
          }
        }
      }
    }
    zip.close()
    println("Downloaded and extracted repository successfully!")
  }
}
