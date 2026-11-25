# moodcatch-character

## 개요
- MoodCatch 캐릭터 생성 서비스 (성향 분석, 이미지 생성)
- sw-back(8080)과 분리된 독립 서비스, 포트 8081, sw-back 내부 호출 전용
- PostgreSQL. 스키마 변경은 Flyway로만 (ddl-auto=validate)

## 규칙 (ECC 규칙·스킬 기본 적용, 아래는 추가·예외만)
- Java 파일은 Read/Edit/Write 도구로만 다룬다.
- JPA 엔티티는 불변성 규칙의 예외로 둔다.
- 이미지 생성 등 긴 작업은 비동기 + 타임아웃, 외부 API 호출은 재시도 포함.
- 기능 하나가 끝나면 java-reviewer를 실행한다.
- 커밋·push는 직접 하지 않는다. README.md 규칙대로 초안만 제안하고 확인받는다.
- 답변은 짧게. 코드 수정 작업 후에만 3줄 보고: 사용한 규칙·스킬·에이전트 / 미사용 규칙과 이유 / 확인 필요

- Ponytail은 해결 방법 단순화에만 적용. 비동기, 재시도, 타임아웃, 테스트는 줄이지 않는다.