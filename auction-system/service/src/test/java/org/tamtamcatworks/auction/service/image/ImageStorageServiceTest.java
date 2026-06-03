package org.tamtamcatworks.auction.service.image;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.SetBucketPolicyArgs;
import java.io.ByteArrayInputStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class ImageStorageServiceTest {

  @Mock
  private MinioClient minioClient;

  @Mock
  private MultipartFile multipartFile;

  private ImageStorageService imageStorageService;

  @BeforeEach
  void setUp() {
    imageStorageService = new ImageStorageService(minioClient);
    ReflectionTestUtils.setField(imageStorageService, "bucketName", "test-bucket");
    ReflectionTestUtils.setField(imageStorageService, "minioEndpoint", "http://localhost:9000");
    ReflectionTestUtils.setField(imageStorageService, "externalUrl", "http://localhost:9000");
  }

  @Test
  void testInitCreatesBucketIfNotExist() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

    imageStorageService.init();

    verify(minioClient, times(1)).makeBucket(any(MakeBucketArgs.class));
    verify(minioClient, times(1)).setBucketPolicy(any(SetBucketPolicyArgs.class));
  }

  @Test
  void testInitDoesNotCreateBucketIfExist() throws Exception {
    when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

    imageStorageService.init();

    verify(minioClient, never()).makeBucket(any(MakeBucketArgs.class));
    verify(minioClient, never()).setBucketPolicy(any(SetBucketPolicyArgs.class));
  }

  @Test
  void testUploadImageSuccess() throws Exception {
    when(multipartFile.getOriginalFilename()).thenReturn("test.png");
    when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
    when(multipartFile.getSize()).thenReturn(3L);
    when(multipartFile.getContentType()).thenReturn("image/png");

    String resultUrl = imageStorageService.uploadImage(multipartFile);

    assertNotNull(resultUrl);
    assertTrue(resultUrl.contains("http://localhost:9000/test-bucket/"));
    verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
  }
}
