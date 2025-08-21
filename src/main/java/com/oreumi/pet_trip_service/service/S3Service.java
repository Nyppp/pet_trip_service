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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
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
     * 리뷰 이미지 업로드
     */
    public String uploadReviewImage(MultipartFile file) throws IOException {
        return uploadFile(file, "review-images");
    }
    
    /**
     * Base64 이미지를 S3에 업로드
     */
    public String uploadBase64Image(String base64Image, String folder) throws IOException {
        if (base64Image == null || !base64Image.startsWith("data:image/")) {
            throw new IllegalArgumentException("올바르지 않은 Base64 이미지 형식입니다.");
        }
        
        // Base64 데이터 파싱
        String[] parts = base64Image.split(",");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Base64 이미지 형식이 올바르지 않습니다.");
        }
        
        // MIME 타입 안전하게 추출 (data:image/jpeg;base64 또는 data:image/jpeg)
        String header = parts[0];
        String mimeType;
        if (header.contains(";")) {
            mimeType = header.split(":")[1].split(";")[0];
        } else {
            mimeType = header.split(":")[1];
        }
        
        byte[] decodedBytes = Base64.getDecoder().decode(parts[1]);
        
        // 파일 확장자 추출
        String fileExtension = getExtensionFromMimeType(mimeType);
        
        // 고유한 파일명 생성
        String fileName = folder + "/" + UUID.randomUUID() + fileExtension;
        
        // S3에 파일 업로드
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(mimeType)
                .build();
        
        PutObjectResponse response = s3Client.putObject(putObjectRequest, 
                RequestBody.fromInputStream(new ByteArrayInputStream(decodedBytes), decodedBytes.length));
        
        if (response.sdkHttpResponse().isSuccessful()) {
            return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, fileName);
        } else {
            throw new IOException("Base64 이미지 업로드에 실패했습니다.");
        }
    }
    
    /**
     * 리뷰 Base64 이미지들을 업로드
     */
    public List<String> uploadReviewBase64Images(List<String> base64Images) throws IOException {
        List<String> uploadedUrls = new ArrayList<>();
        List<String> uploadedKeys = new ArrayList<>();
        
        try {
            for (String base64Image : base64Images) {
                if (base64Image != null && base64Image.startsWith("data:image/")) {
                    String url = uploadBase64Image(base64Image, "review-images");
                    uploadedUrls.add(url);
                    uploadedKeys.add(extractS3KeyFromUrl(url));
                }
            }
            return uploadedUrls;
            
        } catch (Exception e) {
            // 실패 시 업로드된 파일들 삭제 (보상 트랜잭션)
            for (String key : uploadedKeys) {
                try {
                    if (key != null) {
                        deleteFile(key);
                    }
                } catch (Exception ignored) {
                    // 삭제 실패는 무시
                }
            }
            throw e;
        }
    }
    
    /**
     * MIME 타입에서 파일 확장자 추출
     */
    private String getExtensionFromMimeType(String mimeType) {
        switch (mimeType) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/png":
                return ".png";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            default:
                return ".jpg"; // 기본값
        }
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
