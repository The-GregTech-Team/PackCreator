package cn.ac.origind.packcreator

import com.cdancy.jenkins.rest.JenkinsClient
import com.cdancy.jenkins.rest.domain.job.Artifact
import com.github.kittinunf.fuel.httpDownload
import joptsimple.OptionParser
import org.apache.commons.io.FilenameUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

const val HMCL_CI = "https://ci.huangyuhui.net"
const val UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36"
val logger: Logger = LoggerFactory.getLogger("PackCreator")

fun main(args: Array<String>) {
    val parser = OptionParser(false).apply {
        acceptsAll(listOf("d", "directory"), "Modpack folder includes mods, config, etc.").withRequiredArg().defaultsTo("modpack")
        acceptsAll(listOf("f", "force"), "Force overriding existing pack")
        nonOptions().describedAs("Output file").ofType(String::class.java)
    }
    val options = parser.parse(*args)
    if (args.isEmpty() || options.nonOptionArguments().isEmpty()) {
        println("""
            Creates modpack in HMCL format.

            Usage: <program> output.zip

        """.trimIndent())
        parser.printHelpOn(System.out)
        return
    }

    logger.info("Creating modpack ${options.nonOptionArguments().first()}")

    logger.info("Fetching latest HMCL build...")
    val artifacts = getLatestArtifact()
    logger.info("Latest Artifact: " + artifacts.first().fileName())
    artifacts.forEach {
        if (Files.exists(Paths.get(it.fileName())))
            logger.info("Skipping download of ${it.fileName()}")
        else
            downloadArtifact(it)
    }

    generateModpack(FilenameUtils.getBaseName(artifacts.first().fileName()), options.valueOf("d").toString(), options.has("f"), options.nonOptionArguments().first().toString())
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

fun getLatestArtifact(): List<Artifact> {
    val jenkinsClient = JenkinsClient.builder().endPoint(HMCL_CI).build()
    val latestBuild = jenkinsClient.api().jobsApi().lastBuildNumber(null, "HMCL")
    return jenkinsClient.api().jobsApi().buildInfo(null, "HMCL", latestBuild).artifacts().filter { it.fileName().endsWith(".jar") || it.fileName().endsWith(".exe") }
}
