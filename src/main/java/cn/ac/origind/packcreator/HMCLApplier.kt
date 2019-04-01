package cn.ac.origind.packcreator

import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.IOException
import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes

class CopyDirVisitor(private val fromPath: Path, private val toPath: Path, private val copyOption: CopyOption) : SimpleFileVisitor<Path>() {

    @Throws(IOException::class)
    override fun preVisitDirectory(dir: Path, attrs: BasicFileAttributes): FileVisitResult {
        val targetPath = toPath.resolve(fromPath.relativize(dir))
        if (!Files.exists(targetPath)) {
            Files.createDirectory(targetPath)
        }
        return FileVisitResult.CONTINUE
    }

    @Throws(IOException::class)
    override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
        Files.copy(file, toPath.resolve(fromPath.relativize(file)), copyOption)
        return FileVisitResult.CONTINUE
    }
}

fun generateModpack(hmclName: String, modpackDir: String = "modpack", overwrite: Boolean = false, output: String = "pack.zip") {
    if (!Files.exists(Paths.get("hmcl.json")))
        logger.warn("hmcl.json not exists")
    if (!Files.exists(Paths.get("modpack.json")))
        logger.warn("modpack.json not exists")
    if (!Files.exists(Paths.get("pack.json")))
        logger.warn("pack.json not exists")

    val modpack = createTempFile().toPath()
    ZipUtil.pack(File(modpackDir), modpack.toFile()) {
        "minecraft/$it"
    }

    FileSystems.newFileSystem(URI.create("jar:${modpack.toUri()}"), mapOf("create" to "true")).apply {
        if (!Files.isDirectory(getPath("minecraft")))
            Files.createDirectory(getPath("minecraft"))
        Files.copy(Paths.get("modpack.json"), getPath("modpack.json"))
        Files.copy(Paths.get("pack.json"), getPath("minecraft", "pack.json"))
        close()
    }

    val pack = Paths.get(output)
    if (Files.exists(pack)) {
        if (overwrite) {
            logger.warn("Force overwriting pack $pack")
            Files.deleteIfExists(pack)
        } else {
            logger.warn("$pack pack exists")
            return
        }
    }
    FileSystems.newFileSystem(URI.create("jar:${pack.toUri()}"), mapOf("create" to "true")).apply {
        Files.copy(Paths.get("hmcl.json"), getPath("hmcl.json"))
        Files.copy(Paths.get("$hmclName.jar"), getPath("$hmclName.jar"))
        Files.copy(Paths.get("$hmclName.exe"), getPath("$hmclName.exe"))
        Files.copy(modpack, getPath("modpack.zip"))
        close()
    }

    Files.deleteIfExists(modpack)
    logger.info("Done Compressing")
}
