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

fun generateModpack(hmclName: String) {
    val modpack = Paths.get("modpack.zip")
    ZipUtil.pack(File("modpack"), modpack.toFile()) {
        "minecraft/$it"
    }

    FileSystems.newFileSystem(URI.create("jar:${modpack.toUri()}"), mapOf("create" to "true")).apply {
        if (!Files.isDirectory(getPath("minecraft")))
            Files.createDirectory(getPath("minecraft"))
        Files.copy(Paths.get("modpack.json"), getPath("modpack.json"))
        Files.copy(Paths.get("pack.json"), getPath("minecraft", "pack.json"))
        close()
    }

    val pack = Paths.get("pack.zip")
    Files.deleteIfExists(pack)
    FileSystems.newFileSystem(URI.create("jar:${pack.toUri()}"), mapOf("create" to "true")).apply {
        Files.copy(Paths.get("hmcl.json"), getPath("hmcl.json"))
        Files.copy(Paths.get(hmclName), getPath(hmclName))
        Files.copy(modpack, getPath("modpack.zip"))
        close()
    }

    Files.deleteIfExists(modpack)
    logger.info("Done Compressing")
}
