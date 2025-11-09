package com.example.coditas.common.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.coditas.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
@Slf4j
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadFile(MultipartFile file) {
        try {
            File tempFile = convertMultiPartToFile(file);
            var uploadResult = cloudinary.uploader().upload(tempFile, ObjectUtils.emptyMap());
            tempFile.delete();
            return uploadResult.get("secure_url").toString();
        } catch (IOException e) {
            throw new CustomException("Invalid image file or upload failed: " + e.getMessage(), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        } catch (Exception e){
            throw new CustomException("Cloudinary upload failed" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
        try (FileOutputStream fos = new FileOutputStream(convFile)) {
            fos.write(file.getBytes());
        }
        return convFile;
    }

    // Extract public_id from Cloudinary URL
    public String extractPublicIdFromUrl(String url) {
        // Example: https://res.cloudinary.com/demo/image/upload/v1234567890/folder/file.jpg
        try {
            String[] parts = url.split("/upload/");
            if (parts.length < 2) return null;
            String afterUpload = parts[1];
            String withoutVersion = afterUpload.replaceAll("/v\\d+", ""); // remove /v123456
            return withoutVersion.substring(0, withoutVersion.lastIndexOf('.'));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean deleteFile(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            log.warn("deleteFile called with null/empty publicId - skipping");
            return true; // nothing to delete
        }

        publicId = publicId.trim();

        try {
            // Cloudinary returns "ok" or "not found" → both are acceptable
            var result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String status = (String) result.get("result");

            if ("ok".equals(status)) {
                log.info("Successfully deleted from Cloudinary: {}", publicId);
                return true;
            } else if ("not found".equals(status)) {
                log.info("File already deleted or not found in Cloudinary: {}", publicId);
                return true; // idempotent success
            } else {
                log.warn("Unexpected delete result from Cloudinary: {} → {}", publicId, status);
                return false;
            }

        } catch (Exception e) {
            log.error("Failed to delete file from Cloudinary: {}", publicId, e);
            // Don't throw — we don't want user update/delete to fail due to old image cleanup
            return false;
        }
    }
}
