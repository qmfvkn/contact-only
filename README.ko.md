# DM만 · DirectOnly

[English](README.md)

### No 피드, No 릴스. DM만 하고, 시간을 되찾으세요.

답장 하나 하려고 Instagram을 열었다가, 어느새 릴스와 게시물을 보고 있던 적 있나요?

**DM만은 DM에 집중하기 위한 Android 앱입니다.** 필요한 대화만 확인하고, 끝없이 이어지는 콘텐츠 대신 내 시간으로 돌아오세요.

## 연락은 편하게, 딴길은 짧게

- **DM 바로 열기** — 대화와 답장에 집중하세요.
- **피드·게시물·탐색 이동 제한** — 콘텐츠 피드로 새는 동선을 줄입니다.
- **릴스 연속 탐색 차단** — 친구가 DM으로 보낸 릴스는 확인할 수 있지만, 다음 릴스로 계속 넘기는 동작은 막습니다.
- **릴스를 위·아래로 밀면 대화방 복귀** — 받은 영상만 보고 원래 대화로 돌아오세요.
- **메모 영역 숨김** — DM 목록을 간결하게 유지합니다.
- **밝은 테마 / 어두운 테마** — 익숙하고 단순한 화면으로 사용하세요.
- **사진 전송과 선택적 DM 알림 연결** — 메시지에 필요한 기능은 유지합니다.

> ‘No 릴스’는 DM으로 받은 영상까지 지운다는 뜻이 아니라, 끝없는 릴스 탐색을 막는다는 의미입니다. 사용 시간을 측정하거나 특정 절약 시간을 보장하지 않습니다.

## 다운로드

[최신 테스트 버전 다운로드](https://github.com/qmfvkn/contact-only/releases)

휴대폰이 영어 설정이면 앱 이름과 기본 UI가 **DirectOnly**로 표시됩니다.

현재 **0.5.9 테스트 배포판**입니다. 개인 사용과 피드백을 위한 실험 앱이며, 모든 기기에서의 동작을 보장하지 않습니다.

Instagram / Meta / Samsung과 제휴하거나 승인받은 앱이 아닙니다. Instagram 웹 DM을 이용하는 비공식 클라이언트입니다.

## 설치와 제한

Android 8 이상을 대상으로 합니다. 공식 Instagram 앱은 메시지 보기에는 필요하지 않지만, 알림 연결에는 설치·로그인 및 사용자 권한 허용이 필요합니다. 웹에서 로그인하는 계정과 공식 앱 계정을 맞추세요.

알림 접근 서비스를 포함하므로, GitHub에서 받은 APK도 일부 기기/지역의 Play Protect 정책에 따라 설치가 차단될 수 있습니다. 보호 기능 해제를 설치 요건으로 요구하지 않습니다. Instagram 웹 변경에 따라 기능이 깨질 수 있으며 통화 지원은 보장하지 않습니다.

0.5.9은 제한된 실기기 검증을 거친 시험 배포 버전입니다. 모든 기기에서의 설치와 동작을 검증한 버전은 아닙니다.

## 개인정보

[개인정보 처리 설명](PRIVACY.ko.md)을 확인하세요. 비밀번호와 메시지는 Instagram 웹에서 처리하며, 앱 개발자의 별도 서버로 전송하지 않습니다. 이것이 Instagram이나 Android/WebView 제공자의 데이터 처리까지 없다는 뜻은 아닙니다.

## 빌드

JDK 17과 Android SDK platform 35가 필요합니다. Android Studio에서 이 폴더를 열거나 ANDROID_HOME을 설정한 뒤 실행합니다.

Windows: `gradlew.bat testReleaseUnitTest lintRelease assembleRelease`

macOS/Linux: `sh gradlew testReleaseUnitTest lintRelease assembleRelease`

생성된 Release APK는 서명되지 않습니다. 배포 서명 키는 공개하지 않으며 같은 설치 앱 업데이트에는 같은 서명이 필요합니다.

JavaScript 검사: `node tests/reel-script.test.cjs`, `node tests/guard-script.test.cjs`, `node tests/wallpaper-script.test.cjs`

## 공개 범위와 권리

종·톱니바퀴·앱 아이콘은 프로젝트에서 구성한 벡터입니다. 삼성 앱에서 모은 OneUIProject 아이콘은 이 소스와 샘플 APK에 포함하지 않습니다.

프로젝트 전체의 오픈소스 라이선스는 아직 선택하지 않았습니다. 공개 저장소라는 이유만으로 무제한 재사용을 허가한 것은 아닙니다. 외부 구성요소는 [출처 안내](THIRD-PARTY-NOTICES.md)를 확인하세요.

화면 수정 방식에 대한 Instagram 이용약관상 허용 여부는 별도 검토가 남아 있습니다. 이 프로젝트 공개는 적법성이나 Meta의 승인을 보증하지 않습니다.
