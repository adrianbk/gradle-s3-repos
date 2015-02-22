package com.github.adrianbk.gradles3.sdk

import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.event.ProgressEvent
import com.amazonaws.event.ProgressListener
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.*
import spock.lang.Specification

class S3ClientTest extends Specification implements ProgressListener {


  public static final String BUCKET_NAME = 'testv4signatures'
  BasicAWSCredentials basicAWSCredentials

  def setup() {
    basicAWSCredentials = new BasicAWSCredentials(System.getenv('G_AWS_ACCESS_KEY_ID'), System.getenv('G_AWS_SECRET_ACCESS_KEY'))
  }

  def "should talk to buckets in all regions"() {
    AmazonS3Client amazonS3Client = new AmazonS3Client(basicAWSCredentials)
    File file = mediumFile('s3-file', '.txt')

    amazonS3Client
//    System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
//    System.setProperty(SDKGlobalConfiguration.ENFORCE_S3_SIGV4_SYSTEM_PROPERTY, "true");
//    ENFORCE_S3_SIGV4_SYSTEM_PROPERTY

    when:
      (Region.values() - [Region.US_GovCloud, Region.CN_Beijing]).each { Region region ->
        String bucketName = "${BUCKET_NAME}-${region ?: region.name}"

        amazonS3Client.setRegion(region.toAWSRegion())

        println("Creating bucket: ${bucketName}")
        CreateBucketRequest createBucketRequest = new CreateBucketRequest(bucketName, region)
        amazonS3Client.createBucket(createBucketRequest)

        println "-- uploading"
        InputStream inputStream = new FileInputStream(file)
        ObjectMetadata metadata = new ObjectMetadata()
        metadata.setContentLength(file.length())
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, file.name, inputStream, metadata)
        amazonS3Client.putObject(putObjectRequest)

        //FAILING HERE FOR EU_Frankfurt - aws sdk cannot autodetect v4 signatures on head methods
//        println "------Getting object metadata"
//        GetObjectMetadataRequest metadataRequest = new GetObjectMetadataRequest(bucketName,file.name)
//        ObjectMetadata objectMetadata = amazonS3Client.getObjectMetadata(metadataRequest)

        println "------Getting object"
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, file.name)
        getObjectRequest.setRange(0, 0);
        S3Object object = amazonS3Client.getObject(getObjectRequest)
        InputStream objectData = object.getObjectContent();
        objectData.close();

        println "-- Deleting object"
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, putObjectRequest.getKey())
        amazonS3Client.deleteObject(deleteObjectRequest)

        println("Deleting bucket: ${bucketName}")
        DeleteBucketRequest deleteBucketRequest = new DeleteBucketRequest(bucketName)
        amazonS3Client.deleteBucket(deleteBucketRequest)
      }
    then:
      true
  }


  public static File mediumFile(String name, String suffix) {
    File mediumFile = null;
    try {
      mediumFile = File.createTempFile(name, suffix);
      BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(mediumFile));
      int offset = 0;
      while (offset < 6 * 1024) {
        bos.write((offset++ % 256));
      }
      bos.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return mediumFile;
  }

  def doUpload() {


  }

  @Override
  void progressChanged(ProgressEvent progressEvent) {
    println "--$progressEvent"
  }
}
