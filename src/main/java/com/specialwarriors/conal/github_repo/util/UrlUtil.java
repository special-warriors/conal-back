package com.specialwarriors.conal.github_repo.util;

import com.specialwarriors.conal.common.exception.GeneralException;
import com.specialwarriors.conal.github_repo.exception.GithubRepoException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlUtil {

    private static final Pattern GITHUB_URL_PATTERN =
        Pattern.compile("^https://github\\.com/([^/]+)/([^/]+)$");


    public static String[] urlToOwnerAndReponame(String url) {
        Matcher matcher = GITHUB_URL_PATTERN.matcher(url);
        if (matcher.find()) {
            String owner = matcher.group(1);
            String repoName = matcher.group(2);

            return new String[]{owner, repoName};
        } else {
            throw new GeneralException(GithubRepoException.INVALID_URL);
        }
    }

    public static void validateGitHubUrl(String url) {
        // GitHub 저장소 URL 형식 검증: https://github.com/{owner}/{repo}
        String regex = "^https://github\\.com/[^/]+/[^/]+$";
        if (!url.matches(regex)) {
            throw new GeneralException(GithubRepoException.INVALID_URL);
        }
    }
}
