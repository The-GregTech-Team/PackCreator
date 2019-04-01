package cn.ac.origind.packcreator

import com.cdancy.jenkins.rest.JenkinsClient
import com.cdancy.jenkins.rest.domain.job.Artifact
import com.github.kittinunf.fuel.httpDownload
import joptsimple.OptionParser
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

const val HMCL_CI = "https://ci.huangyuhui.net"
const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36"
val logger: Logger = LoggerFactory.getLogger("PackCreator")

fun main(args: Array<String>) {
    val parser = OptionParser()
    val options = parser.parse(*args)

    logger.info("Fetching latest HMCL build...")
    val artifact = getLatestArtifact()
    logger.info("Latest Artifact: " + artifact.fileName())
    if (Files.exists(Paths.get(artifact.fileName())))
        logger.info("Skipping download of HMCL")
    else
        downloadArtifact(artifact)

    generateModpack(artifact.fileName())
}

fun downloadArtifact(artifact: Artifact) {
    logger.info("Downloading Artifact $HMCL_CI/job/HMCL/lastSuccessfulBuild/artifact/${artifact.relativePath()}")
    "$HMCL_CI/job/HMCL/lastSuccessfulBuild/artifact/${artifact.relativePath()}"
            .httpDownload()
            .fileDestination { response, request -> File(artifact.fileName()) }
            .progress { readBytes, totalBytes ->
                val progress = readBytes.toFloat() / totalBytes.toFloat() * 100
                println("Bytes downloaded $readBytes / $totalBytes ($progress %)")
            }
            .header("User-Agent", UA)
            .response { _ ->
            }
            .join()
}

fun getLatestArtifact(): Artifact {
    val jenkinsClient = JenkinsClient.builder().endPoint(HMCL_CI).build()
    val latestBuild = jenkinsClient.api().jobsApi().lastBuildNumber(null, "HMCL")
    return jenkinsClient.api().jobsApi().buildInfo(null, "HMCL", latestBuild).artifacts().first { it.fileName().endsWith(".jar") }
}
