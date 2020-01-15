package org.broadinstitute.yootilz.gcp.storage

import java.nio.channels.FileChannel

import better.files.File
import com.google.api.gax.paging.Page
import com.google.auth.Credentials
import com.google.cloud.{ReadChannel, WriteChannel}
import com.google.cloud.storage.Storage.{BlobListOption, CopyRequest}
import com.google.cloud.storage.{Blob, BlobId, BlobInfo, Storage, StorageOptions}
import org.broadinstitute.yootilz.core.snag.Snag
import org.broadinstitute.yootilz.core.text.UIStringUtils

import scala.jdk.CollectionConverters.IteratorHasAsScala

class GoogleStorageUtils(credentials: Credentials, projectIdOpt: Option[String] = None) {
  val storage: Storage = {
    var builder = StorageOptions.newBuilder().setCredentials(credentials)
    projectIdOpt.foreach(projectId => builder = builder.setProjectId(projectId))
    builder.build().getService
  }

  def listBucket(bucketName: String, prefixOpt: Option[String] = None): Iterator[Blob] = {
    val blobListOptions: Seq[BlobListOption] =
      prefixOpt.map(BlobListOption.prefix).toSeq ++ projectIdOpt.map(Storage.BlobListOption.userProject).toSeq
    val page = storage.list(bucketName, blobListOptions: _*)
    page.iterateAll().iterator().asScala
  }

  def copyFile(source: BlobId, target: BlobId): Unit = {
    var builder: CopyRequest.Builder = new Storage.CopyRequest.Builder().setSource(source).setTarget(target)
    projectIdOpt.foreach(projectId => {
      val option = Storage.BlobSourceOption.userProject(projectId)
      builder = builder.setSourceOptions(option)
    })
    val copyRequest = builder.build()
    storage.copy(copyRequest)
  }

  def copyFiles(sourceBucket: String, sourcePrefix: String,
                targetBucket: String, targetPrefix: String): Seq[String] = {
    val blobIter = listBucket(sourceBucket, Some(sourcePrefix))
    var filesCopied: Seq[String] = Seq.empty
    val sourcePrefixSize = sourcePrefix.size
    for (sourceBlob <- blobIter) {
      val sourceFileName = sourceBlob.getBlobId.getName
      val targetFileName = targetPrefix + sourceFileName.substring(sourcePrefixSize)
      filesCopied :+= targetFileName
      val targetBlobId = BlobId.of(targetBucket, targetFileName)
      copyFile(sourceBlob.getBlobId, targetBlobId)
    }
    filesCopied
  }

  val maxFileReadSize = 1000000

  def uploadFile(file: File, blobInfo: BlobInfo): Either[Snag, Long] = {
    file.fileChannel.map { fileChannel =>
      val writer = storage.writer(blobInfo)
      var pos: Long = 0L
      val fileSize = fileChannel.size()
      var failedAttempts: Int = 0
      val startTime = System.currentTimeMillis()
      while (pos < fileSize && failedAttempts < 3) {
        val remaining = fileSize - pos
        val readSize = if (remaining < maxFileReadSize) remaining else maxFileReadSize
        val buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, pos, readSize)
        val timeNow = System.currentTimeMillis()
        val timePassed = timeNow - startTime
        val timePassedString = UIStringUtils.prettyTimeInterval(timePassed)
        val timeEstimate = UIStringUtils.timeEstimate(timePassed, pos, fileSize)
        val timeEstimateString = UIStringUtils.prettyTimeInterval(timeEstimate)
        val timesStrings = s"timePassed=$timePassedString, timeEstimate=$timeEstimateString"
        println(s"Writing pos=$pos, readSize=$readSize, remaining=$remaining, $timesStrings")
        val writtenSize = writer.write(buffer)
        if (writtenSize == 0) {
          failedAttempts += 1
        } else {
          failedAttempts = 0
        }
        pos += writtenSize
      }
      writer.close()
      if (pos < fileSize) {
        Left(Snag(s"Could only write $pos of $fileSize bytes."))
      } else {
        Right(pos)
      }
    }.get()
  }

  def downloadFile(blobId: BlobId, file: File): Unit = {
    val blob = storage.get(blobId)
    blob.downloadTo(file.path)
  }

  def reader(blobId: BlobId): ReadChannel = storage.get(blobId).reader()

  def writer(blobId: BlobId): WriteChannel = storage.get(blobId).writer()
}

object GoogleStorageUtils {
  def apply(credentials: Credentials): GoogleStorageUtils = new GoogleStorageUtils(credentials)

  def apply(credentials: Credentials, projectId: String): GoogleStorageUtils =
    new GoogleStorageUtils(credentials, Some(projectId))

  def apply(credentials: Credentials, projectIdOpt: Option[String]): GoogleStorageUtils =
    new GoogleStorageUtils(credentials, projectIdOpt)

  def getPageIterator[T](page: Page[T]): Iterator[Page[T]] = new PageIterator[T](page)

  def getItemIterator[T](page: Page[T]): Iterator[T] = getPageIterator(page).flatMap(_.getValues.iterator().asScala)

  private class PageIterator[T](page: Page[T]) extends Iterator[Page[T]] {
    private var currentPage = page

    override def hasNext: Boolean = currentPage.hasNextPage

    override def next(): Page[T] = {
      currentPage = currentPage.getNextPage
      currentPage
    }
  }

}
