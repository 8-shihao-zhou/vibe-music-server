package cn.edu.seig.vibemusic.constants;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * AI MV 可控风格预设
 */
public final class AiVideoStylePresets {

    private static final Map<String, String> STYLE_LABEL_MAP = new LinkedHashMap<>();

    static {
        STYLE_LABEL_MAP.put("healing", "梦幻治愈");
        STYLE_LABEL_MAP.put("cyberpunk", "赛博霓虹");
        STYLE_LABEL_MAP.put("ink", "国风水墨");
        STYLE_LABEL_MAP.put("campus", "清新校园");
        STYLE_LABEL_MAP.put("cinematic", "电影氛围");
        STYLE_LABEL_MAP.put("stage", "舞台热力");
    }

    private AiVideoStylePresets() {
    }

    /**
     * 规范化风格编码，非法值回退到默认风格
     */
    public static String normalizeCode(String styleCode) {
        if (styleCode == null) {
            return "healing";
        }
        String trimmedCode = styleCode.trim().toLowerCase();
        return STYLE_LABEL_MAP.containsKey(trimmedCode) ? trimmedCode : "healing";
    }

    /**
     * 根据编码获取展示名称
     */
    public static String resolveLabel(String styleCode, String styleLabel) {
        String normalizedCode = normalizeCode(styleCode);
        if (styleLabel != null && !styleLabel.trim().isEmpty()) {
            return styleLabel.trim();
        }
        return STYLE_LABEL_MAP.get(normalizedCode);
    }
}
