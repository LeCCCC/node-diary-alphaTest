package org.example.nodediary.controller;

import org.example.nodediary.pojo.Result;
import org.example.nodediary.pojo.UploadResult;
import org.example.nodediary.utils.AliOSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/api/upload")
public class UploadController {
    @Autowired
    AliOSSUtils aliOSSUtils;
    // 允许的图片类型
    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );
    // 允许的后缀
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "webp"
    );
    // 文件大小限制：10MB
    private static final long MAX_SIZE = 10 * 1024 * 1024;

    //上传图像到阿里云
    @PostMapping("/image")
    public Result uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error("文件为空");
        } else if (file.getSize() > MAX_SIZE) {
            return Result.error("文件过大，最大支持10MB");
        }
        //校验文件类型
        else if (file.getOriginalFilename() == null || !file.getOriginalFilename().contains(".")) {
            return Result.error("文件格式不受支持，仅支持jpg、jpeg、png、webp");
        } else if (!ALLOWED_EXTENSIONS.contains(file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1).toLowerCase())) {
            return Result.error("文件格式不支持，仅支持jpg、jpeg、png、webp");
        }
        UploadResult uploadResult = aliOSSUtils.upload(file);

        return Result.success("上传成功", uploadResult);
    }
}
