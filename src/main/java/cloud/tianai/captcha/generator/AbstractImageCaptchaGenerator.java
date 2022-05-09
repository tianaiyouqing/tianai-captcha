package cloud.tianai.captcha.generator;

import cloud.tianai.captcha.generator.common.model.dto.GenerateParam;
import cloud.tianai.captcha.generator.common.model.dto.ImageCaptchaInfo;
import cloud.tianai.captcha.generator.common.util.CaptchaImageUtils;
import cloud.tianai.captcha.generator.common.util.ImgWriter;
import cloud.tianai.captcha.resource.common.model.dto.Resource;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

/**
 * @Author: 天爱有情
 * @date 2022/4/22 16:30
 * @Description 抽象的验证码生成器
 */
@Slf4j
public abstract class AbstractImageCaptchaGenerator implements ImageCaptchaGenerator {
    public static String DEFAULT_BG_IMAGE_TYPE = "jpeg";
    public static String DEFAULT_SLIDER_IMAGE_TYPE = "png";

    @Getter
    @Setter
    /** 默认背景图片类型. */
    public String defaultBgImageType = DEFAULT_BG_IMAGE_TYPE;
    @Getter
    @Setter
    /** 默认滑块图片类型. */
    public String defaultSliderImageType = DEFAULT_SLIDER_IMAGE_TYPE;

    @Override
    public ImageCaptchaInfo generateCaptchaImage(String type) {
        return generateCaptchaImage(type, defaultBgImageType, defaultSliderImageType);
    }

    @SneakyThrows
    @Override
    public ImageCaptchaInfo generateCaptchaImage(String type, String backgroundFormatName, String sliderFormatName) {
        return generateCaptchaImage(GenerateParam.builder()
                .type(type)
                .backgroundFormatName(backgroundFormatName)
                .sliderFormatName(sliderFormatName)
                .obfuscate(false)
                .build());
    }

    /**
     * 将图片转换成字符串格式
     *
     * @param bufferedImage 图片
     * @param formatType    格式化类型
     * @return String
     */
    @SneakyThrows(Exception.class)
    public String transform(BufferedImage bufferedImage, String formatType) {
        // 这里判断处理一下,加一些警告日志
        String result = beforeTransform(bufferedImage, formatType);
        if (result != null) {
            return result;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        long currentTimeMillis = System.currentTimeMillis();
        ImgWriter.write(bufferedImage, formatType, byteArrayOutputStream, -1);
        System.out.println("耗时:" + (System.currentTimeMillis() - currentTimeMillis));
        //转换成字节码
        byte[] data = byteArrayOutputStream.toByteArray();
        String base64 = Base64.getEncoder().encodeToString(data);
        return "data:image/" + formatType + ";base64,".concat(base64);
    }

    public String beforeTransform(BufferedImage bufferedImage, String formatType) {
        int type = bufferedImage.getType();
        if (BufferedImage.TYPE_4BYTE_ABGR == type) {
            // png , 如果转换的是jpg的话
            if (CaptchaImageUtils.isJpeg(formatType)) {
                // bufferedImage为 png， 但是转换的图片为 jpg
                if (log.isWarnEnabled()) {
                    log.warn("图片验证码转换警告， 原图为 png格式时，指定转换的图片为jpg格式时可能会导致转换异常，如果转换的图片为出现错误，请设置指定转换的类型与原图的类型一致");
                } else {
                    System.err.println("图片验证码转换警告， 原图为 png格式时，指定转换的图片为jpg格式时可能会导致转换异常，如果转换的图片为出现错误，请设置指定转换的类型与原图的类型一致");
                }
            }
        }
        // 其它的暂时不考虑
        return null;
    }

    protected InputStream getTemplateFile(Map<String, Resource> templateImages, String imageName) {
        Resource resource = templateImages.get(imageName);
        if (resource == null) {
            throw new IllegalArgumentException("查找模板异常， 该模板下未找到 ".concat(imageName));
        }
        return getImageResourceManager().getResourceInputStream(resource);
    }

}
