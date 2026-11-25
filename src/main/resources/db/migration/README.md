# DB 마이그레이션 (Flyway)

테이블 생성·변경은 이 폴더의 SQL 파일로만 한다. (`ddl-auto=validate`라 Hibernate는 검사만 함)

- 파일 이름: `V{버전}__{설명}.sql` (언더스코어 2개)
  - 예: `V1__create_character.sql`, `V2__add_character_mood.sql`
- 이미 적용된 파일은 수정하지 않는다. 바꿀 내용이 있으면 새 버전 파일을 추가한다.
- 서버가 뜰 때 아직 적용되지 않은 파일이 순서대로 실행되고, 이력은 `flyway_schema_history` 테이블에 남는다.
- 엔티티를 추가·수정하면 같은 내용의 마이그레이션 파일도 함께 만들어야 서버가 뜬다.
