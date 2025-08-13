package com.oreumi.pet_trip_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${spring.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.aws.region.static}")
    private String region;

    /**
     * 파일을 S3에 업로드하고 URL을 반환
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // 파일 확장자 추출
        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 고유한 파일명 생성
        String fileName = folder + "/" + UUID.randomUUID() + fileExtension;

        // S3에 파일 업로드
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(file.getContentType())
                .build();

        PutObjectResponse response = s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        if (response.sdkHttpResponse().isSuccessful()) {
            // 업로드된 파일의 URL 반환
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        } else {
            throw new IOException("파일 업로드에 실패했습니다.");
        }
    }

    /**
     * 프로필 이미지 업로드
     */
    public String uploadProfileImage(MultipartFile file) throws IOException {
        return uploadFile(file, "profile-images");
    }
    
    /**
     * S3에서 파일 삭제
     */
    public void deleteFile(String s3Key) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .build();
        
        s3Client.deleteObject(deleteObjectRequest);
    }
    
    /**
     * URL에서 S3 키 추출
     */
    public String extractS3KeyFromUrl(String imageUrl) {
        if (imageUrl == null || !imageUrl.contains(".amazonaws.com/")) {
            return null;
        }
        
        // URL 형식: https://bucketname.s3.region.amazonaws.com/folder/filename
        String[] parts = imageUrl.split(".amazonaws.com/");
        if (parts.length > 1) {
            return parts[1]; // folder/filename 부분 반환
        }
        
        return null;
    }
}
