package com.project.picture.utils;

import com.common.core.common.utils.PicFormatUtils;
import com.common.core.exception.BusinessException;
import com.common.core.exception.ErrorCode;
import com.common.core.exception.ThrowUtils;
import lombok.Data;
import net.coobird.thumbnailator.Thumbnails;
import org.apache.commons.imaging.Imaging;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.common.core.common.constant.CommonConstant.THUMBNAIL_FORMAT;

public class ImageUtils {

    @Data
    public static class ImageMeta {
        public long picSize;
        public int picWidth;
        public int picHeight;
        public double picScale;
        public String picFormat;
        public String picColor;
    }

    /**
     * 获取图片信息 流式
     * 直传 无需校验
     */
    public static ImageMeta getImageMeta(InputStream inputStream, String filename) {
        try {
            BufferedImage image = Imaging.getBufferedImage(inputStream, filename);
            ImageMeta meta = new ImageMeta();
            meta.picWidth = image.getWidth();
            meta.picHeight = image.getHeight();
            meta.picScale = (double) image.getWidth() / image.getHeight();
            meta.picFormat = PicFormatUtils.detectFormat(filename);
            meta.picColor = detectMainColor(image);
            return meta;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "解析图片失败");
        }
    }

    /**
     * 提取图片信息 （字节）
     */
    public static ImageMeta getImageMeta(byte[] bytes, String originFilename) {
        // 检测图片格式
        String picFormat = PicFormatUtils.detectFormat(originFilename);
        boolean result = PicFormatUtils.verifyFormat(picFormat);
        ThrowUtils.throwIf(!result, ErrorCode.PARAMS_ERROR, "图片格式错误");
        try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
            BufferedImage image = Imaging.getBufferedImage(inputStream, originFilename);
            ImageMeta meta = new ImageMeta();
            meta.picSize = bytes.length;
            meta.picWidth = image.getWidth();
            meta.picHeight = image.getHeight();
            meta.picScale = (double) image.getWidth() / image.getHeight();
            meta.picFormat = picFormat;
            meta.picColor = detectMainColor(image);
            return meta;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "解析图片失败");
        }
    }

    /**
     * 获取缩略图字节流
     */
    public static byte[] generateThumbnail(byte[] bytes) {
        try (ByteArrayOutputStream thumbOut = new ByteArrayOutputStream();
             ByteArrayInputStream thumbIn = new ByteArrayInputStream(bytes)) {
            Thumbnails.of(thumbIn)
                    .size(300, 300)
                    .outputQuality(0.7)
                    .outputFormat(THUMBNAIL_FORMAT)
                    .toOutputStream(thumbOut);
            return thumbOut.toByteArray();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "获取 Thumbnails 字节流失败！");
        }
    }

    /**
     * 检测图片主色调
     */
    public static String detectMainColor(BufferedImage image) {
        long r = 0, g = 0, b = 0, count = 0;
        for (int x = 0; x < image.getWidth(); x += 10) {
            for (int y = 0; y < image.getHeight(); y += 10) {
                Color c = new Color(image.getRGB(x, y));
                r += c.getRed();
                g += c.getGreen();
                b += c.getBlue();
                count++;
            }
        }
        int rr = (int) (r / count);
        int gg = (int) (g / count);
        int bb = (int) (b / count);
        return String.format("0x%02x%02x%02x", rr, gg, bb);
    }
}