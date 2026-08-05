package org.runnect.server.external.aws;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.runnect.server.common.exception.BadRequestException;
import org.runnect.server.common.exception.NotFoundException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        s3Service = new S3Service(amazonS3);
        ReflectionTestUtils.setField(s3Service, "bucket", "runnect-test-bucket");
    }

    private void stubUploadedUrl() throws Exception {
        when(amazonS3.getUrl(any(), any())).thenReturn(
            new URL("https://runnect-test-bucket.s3.ap-northeast-2.amazonaws.com/course/image/test.jpg"));
    }

    @Nested
    @DisplayName("uploadImage")
    class UploadImage {

        @Test
        @DisplayName("정상적인 이미지 파일이면 업로드하고 URL을 반환한다")
        void 정상_업로드() throws Exception {
            stubUploadedUrl();
            MultipartFile file = new MockMultipartFile("image", "photo.jpg", "image/jpeg", "content".getBytes());

            String url = s3Service.uploadImage(file, "course");

            assertThat(url).isEqualTo(
                "https://runnect-test-bucket.s3.ap-northeast-2.amazonaws.com/course/image/test.jpg");
        }

        @Test
        @DisplayName("대문자 확장자(.PNG)도 정상 업로드된다")
        void 대문자_확장자() throws Exception {
            stubUploadedUrl();
            MultipartFile file = new MockMultipartFile("image", "photo.PNG", "image/png", "content".getBytes());

            assertThat(s3Service.uploadImage(file, "course")).isNotNull();
        }

        @Test
        @DisplayName("지원하지 않는 확장자면 BadRequestException")
        void 지원하지_않는_확장자() {
            MultipartFile file = new MockMultipartFile("image", "photo.gif", "image/gif", "content".getBytes());

            assertThatThrownBy(() -> s3Service.uploadImage(file, "course"))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("[버그 수정 검증] 파일명이 null이면 500(NPE) 대신 NotFoundException")
        void 파일명이_null() {
            MultipartFile file = new MockMultipartFile("image", null, "image/jpeg", "content".getBytes());

            assertThatThrownBy(() -> s3Service.uploadImage(file, "course"))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("파일명이 빈 문자열이면 NotFoundException")
        void 파일명이_빈문자열() {
            MultipartFile file = new MockMultipartFile("image", "", "image/jpeg", "content".getBytes());

            assertThatThrownBy(() -> s3Service.uploadImage(file, "course"))
                .isInstanceOf(NotFoundException.class);
        }

        @Test
        @DisplayName("[버그 수정 검증] 확장자(.)가 없는 파일명이면 500(StringIndexOutOfBoundsException) 대신 BadRequestException")
        void 확장자가_없는_파일명() {
            MultipartFile file = new MockMultipartFile("image", "photo_without_extension", "image/jpeg",
                "content".getBytes());

            assertThatThrownBy(() -> s3Service.uploadImage(file, "course"))
                .isInstanceOf(BadRequestException.class);
        }

        @Test
        @DisplayName("파일 스트림을 읽는 중 오류가 나면 NotFoundException")
        void 스트림_읽기_실패() throws IOException {
            MultipartFile file = mock(MultipartFile.class);
            when(file.getOriginalFilename()).thenReturn("photo.jpg");
            when(file.getInputStream()).thenThrow(new IOException("disk error"));

            assertThatThrownBy(() -> s3Service.uploadImage(file, "course"))
                .isInstanceOf(NotFoundException.class);
        }
    }
}
