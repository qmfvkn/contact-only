# 배포 점검 결과 — 2026-10-08

대상: DM만 0.5.8 (app.contactonly.sample, versionCode 9)

## 확인된 사항

- Release APK 서명 v2/v3 검증 통과. debuggable 플래그 없음.
- 임시 WebView 디버깅 코드 없음. 삼성 계열 외부 아이콘 및 라이선스 파일 없음.
- INTERNET / POST_NOTIFICATIONS 권한만 명시. 알림 서비스는 BIND_NOTIFICATION_LISTENER_SERVICE로 보호.
- HTTPS만 허용, SSL 오류 취소, mixed content 차단, 파일 접근 비활성화, JS 네이티브 브리지 없음.
- 서버/광고/분석 SDK 없음. Instagram 웹 통신과 기기 내 WebView 저장은 존재.
- 공개 ZIP에 키, 비밀번호, SDK/캐시, 캡처, 사용자 이메일, 개인 절대경로 없음.
- JavaScript 회귀 검사 통과. Android 단위 검사/Lint/Release 빌드는 별도 공개 폴더에서 검증.
- 연결된 GitHub 계정 qmfvkn의 기존 저장소에 push/admin 권한 확인. 이 문서는 공개 전 기술 점검 결과이며, 공개 저장소는 qmfvkn/contact-only로 결정.

## 배포 전 결정/확인

1. 전용 공개 저장소: qmfvkn/contact-only.
2. 오픈소스로 재사용을 허가할 경우 프로젝트 라이선스 선택. 아직 LICENSE를 지정하지 않음.
3. 배포 운영자: qmfvkn. 문의 창구: 저장소 Issues (PRIVACY.md 참고).
4. Instagram 약관상 화면 수정/비공식 클라이언트 허용 여부 검토는 미완료. 기술 점검은 법적 적합성 보증이 아님.
5. 알림 접근 서비스 때문에 인터넷에서 받은 APK가 일부 기기/지역의 Play Protect에 차단될 수 있음. 기존 USB 설치 성공은 일반 다운로드 설치 성공을 보장하지 않음.

현 단계 권장 형태: 실험/테스트용 prerelease. 모든 사용자에게 바로 설치 가능한 정식 배포판으로 표현하지 않음.

## 공식 참고

- GitHub Releases: https://docs.github.com/en/repositories/releasing-projects-on-github/about-releases
- 설치 경고/차단: https://developers.google.com/android/play-protect/warning-dev-guidance
- Instagram 약관: https://help.instagram.com/581066165581870 (이번 검토에서 최신 전문 확인 제한)
