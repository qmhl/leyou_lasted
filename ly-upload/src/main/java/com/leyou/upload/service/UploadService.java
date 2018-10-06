package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-24 17:13
 **/
@Service
public class UploadService {

    private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

    private static final List<String> ALLOW_TYPE = Arrays.asList("image/png", "image/jpeg");

    @Autowired
    private FastFileStorageClient storageClient;

    public String uploadImage(MultipartFile file) {
        try {
            // 1、文件的校验
            // 1.1、校验文件类型
            String contentType = file.getContentType();
            if(!ALLOW_TYPE.contains(contentType)){
                return null;
            }

            // 1.2、校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null){
                return null;
            }

            // 保存文件
//            File dest = new File("D:\\heima30\\upload", file.getOriginalFilename());
//            file.transferTo(dest);

            // 上传到FastDFS
            String extension = StringUtils.substringAfterLast(file.getOriginalFilename(), ".");
            StorePath storePath = this.storageClient.uploadFile(
                    file.getInputStream(), file.getSize(), extension, null);

            // 返回路径
            String url = "http://image.leyou.com/" + storePath.getFullPath();
            return url;
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("文件上传失败！文件名：{}", file.getOriginalFilename(), e);
        }
        return null;
    }
}
