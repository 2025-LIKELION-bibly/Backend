package likelion.bibly.global.util;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PaginationUtil {
    // 최대 글자 수 제한(공백 포함)
    private static final int MAX_CHAR_PER_PAGE = 564;
    // 최대 바이트 수 제한
    private static final int MAX_BYTE_PER_PAGE = 993;
    // 인코딩 설정
    private static final String ENCODING = "UTF-8";

    public static List<String> splitTextByDualLimit(String originalText) throws UnsupportedEncodingException {
        List<String> pages = new ArrayList<>();
        int startIndex = 0;

        while (startIndex < originalText.length()) {
            int pageEndIndex = startIndex;

            // 현재 페이지의 시작 글자부터 탐색
            for (int i = startIndex; i < originalText.length(); i++) {

                // 현재 글자 수 확인 (564자 제한)
                int currentCharCount = i - startIndex + 1; // 1부터 시작하는 글자 수
                if (currentCharCount > MAX_CHAR_PER_PAGE) {
                    break;
                }

                // 현재까지의 텍스트를 추출하여 바이트 길이 확인 (993 바이트 제한)
                String sub = originalText.substring(startIndex, i + 1);
                int byteLength = sub.getBytes(ENCODING).length;

                if (byteLength <= MAX_BYTE_PER_PAGE) {
                    // 글자 수와 바이트 수 제한을 모두 만족함
                    pageEndIndex = i + 1; // 다음 페이지 시작 인덱스로 임시 저장
                } else {
                    // 바이트 제한을 초과했으므로 루프 종료
                    break;
                }
            }

            // 한 페이지 분량의 텍스트 추출 및 저장
            if (pageEndIndex > startIndex) {
                String pageText = originalText.substring(startIndex, pageEndIndex);
                pages.add(pageText);
                startIndex = pageEndIndex; // 다음 페이지 시작점 업데이트
            } else {
                // 이 경우 인코딩 문제일 수 있음
                System.err.println("오류: 첫 글자가 이미 최대 바이트 수를 초과합니다.");
                break;
            }
        }

        return pages;
    }
}