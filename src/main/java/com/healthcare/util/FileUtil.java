package com.healthcare.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileUtil {

    public String getFileExtension(MultipartFile file) {
        String orgName = file.getOriginalFilename();
        assert orgName != null;
        String[] parts = orgName.split("\\.");
        return parts[parts.length - 1];
    }

}
