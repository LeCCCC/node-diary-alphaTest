package org.example.nodediary.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import lombok.Data;
import org.example.nodediary.pojo.UploadResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.UUID;

//阿里云OOS工具类
@Data
@Component
public class AliOSSUtils {

    @Autowired
    private AliProperties aliProperties;

    //上传图片到OSS
    public UploadResult upload(MultipartFile file) throws IOException {
        // 获取上传的文件的输入流
        String endpoint = aliProperties.getEndpoint();
        String bucketName = aliProperties.getBucketName();
        String accessKeyId = aliProperties.getAccessKeyId();
        String accessKeySecret = aliProperties.getAccessKeySecret();

        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("OSS 配置缺失：aliyun.oss.endpoint 未设置，请检查 application.yml");
        }
        if (accessKeyId == null || accessKeyId.isBlank()) {
            throw new IllegalStateException("OSS 配置缺失：aliyun.oss.accessKeyId 未设置，请检查 application.yml");
        }
        if (accessKeySecret == null || accessKeySecret.isBlank()) {
            throw new IllegalStateException("OSS 配置缺失：aliyun.oss.accessKeySecret 未设置，请检查 application.yml");
        }
        if (bucketName == null || bucketName.isBlank()) {
            throw new IllegalStateException("OSS 配置缺失：aliyun.oss.bucketName 未设置，请检查 application.yml");
        }

        InputStream inputStream = file.getInputStream();

        //获取源文件名称
        String originalFilename = file.getOriginalFilename();
        //判断文件是否为空
        if (originalFilename == null) {
            throw new RuntimeException("文件名不能为空");
        }
        // 获取后缀
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        //创建文件夹便于分类
        String dir = LocalDate.now().toString().replace("-", "/");
        //生成唯一文件名
        String fileName = dir + "/" +
                System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().replace("-", "")
                + extension;
        //上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        try {
            ossClient.putObject(bucketName, fileName, inputStream);
        } finally {
            // 关闭ossClient
            ossClient.shutdown();
        }

        //文件访问路径
        String url = endpoint.split("//")[0] + "//" + bucketName + "." + endpoint.split("//")[1] + "/" + fileName;

        // 把上传到oss的对象返回
        return new UploadResult(url, fileName);
    }

}
