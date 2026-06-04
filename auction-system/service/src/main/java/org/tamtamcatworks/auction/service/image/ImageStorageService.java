package org.tamtamcatworks.auction.service.image;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

  private static final Logger log = LoggerFactory.getLogger(ImageStorageService.class);

  private final MinioClient minioClient;

  @Value("${minio.bucketName}")
  private String bucketName;

  @Value("${minio.endpoint}")
  private String minioEndpoint;

  @Value("${minio.externalUrl}")
  private String externalUrl;

  public ImageStorageService(MinioClient minioClient) {
    this.minioClient = minioClient;
  }

  @PostConstruct
  public void init() {
    try {
      boolean found =
          minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
      if (!found) {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        log.info("Created MinIO bucket: {}", bucketName);

        // Set public policy so files can be fetched directly via HTTP GET
        String policy =
            "{\n"
                + "  \"Version\": \"2012-10-17\",\n"
                + "  \"Statement\": [\n"
                + "    {\n"
                + "      \"Effect\": \"Allow\",\n"
                + "      \"Principal\": \"*\",\n"
                + "      \"Action\": [\"s3:GetObject\"],\n"
                + "      \"Resource\": [\"arn:aws:s3:::"
                + bucketName
                + "/*\"]\n"
                + "    }\n"
                + "  ]\n"
                + "}";
        minioClient.setBucketPolicy(
            SetBucketPolicyArgs.builder().bucket(bucketName).config(policy).build());
        log.info("Set public read policy on MinIO bucket: {}", bucketName);
      }
    } catch (Exception e) {
      log.error("Failed to initialize MinIO bucket on startup. Bucket name = {}", bucketName, e);
    }
  }

  public String uploadImage(MultipartFile file) {
    try {
      String originalFilename = file.getOriginalFilename();
      String extension = ".jpg"; // fallback
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }
      String filename = UUID.randomUUID().toString() + extension;

      try (InputStream is = file.getInputStream()) {
        minioClient.putObject(
            PutObjectArgs.builder().bucket(bucketName).object(filename).stream(
                    is, file.getSize(), -1)
                .contentType(file.getContentType() != null ? file.getContentType() : "image/jpeg")
                .build());
      }

      // Return the external direct URL of the image
      return externalUrl + "/" + bucketName + "/" + filename;
    } catch (Exception e) {
      log.error("Failed to upload image to MinIO", e);
      throw new RuntimeException("Failed to upload image to MinIO", e);
    }
  }
}
