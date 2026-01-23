# FlatLaf 적용 안내

## 변경 사항

이 프로그램에 FlatLaf Look and Feel이 적용되었습니다.

### 수정된 파일
1. **PatchHelper.java** - FlatLightLaf 테마를 초기화하도록 수정
2. **.classpath** - FlatLaf 라이브러리 경로 추가
3. **lib/flatlaf-3.5.2.jar** - FlatLaf 라이브러리 추가

### 기존 레이아웃 유지
- BasicForm의 모든 레이아웃 코드는 그대로 유지됨
- 컴포넌트 위치, 크기, 동작은 변경되지 않음
- 단지 Look and Feel만 변경됨

## 다른 테마 사용하기

FlatLaf는 여러 테마를 제공합니다. PatchHelper.java에서 다음과 같이 변경할 수 있습니다:

### Light 테마 (현재 설정)
```java
FlatLightLaf.setup();
```

### Dark 테마
```java
import com.formdev.flatlaf.FlatDarkLaf;
// ...
FlatDarkLaf.setup();
```

### IntelliJ 테마
```java
import com.formdev.flatlaf.FlatIntelliJLaf;
// ...
FlatIntelliJLaf.setup();
```

### Darcula 테마
```java
import com.formdev.flatlaf.FlatDarculaLaf;
// ...
FlatDarculaLaf.setup();
```

## Eclipse에서 실행하기

1. Eclipse에서 프로젝트를 새로고침하세요 (F5)
2. 프로젝트가 자동으로 빌드됩니다
3. PatchHelper.java를 실행하면 FlatLaf가 적용된 UI를 확인할 수 있습니다

## 원래 Look and Feel로 되돌리기

원래 시스템 Look and Feel로 되돌리려면 PatchHelper.java에서:

```java
// FlatLaf 설정 부분을 제거하거나 주석 처리
// FlatLightLaf.setup();

// 또는 시스템 Look and Feel 사용
UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
```

## 참고
- FlatLaf 공식 사이트: https://www.formdev.com/flatlaf/
- GitHub: https://github.com/JFormDesigner/FlatLaf
