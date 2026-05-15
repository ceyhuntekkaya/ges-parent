package com.genixo.ges.api.languagecamp;

import java.util.regex.Pattern;

final class LanguageCampProjectMediaUrls {

    private static final Pattern PORTAL_FILE_URL = Pattern.compile(".*/v1/portal/files/([0-9a-fA-F-]{36})/download.*");

    private LanguageCampProjectMediaUrls() {}

    static String toPublicMediaUrl(String maybeUrl) {
        if (maybeUrl == null) return null;
        String u = maybeUrl.trim();
        if (u.isEmpty()) return u;
        var m = PORTAL_FILE_URL.matcher(u);
        if (!m.matches()) return u;
        return "/v1/public/files/" + m.group(1) + "/download";
    }
}
