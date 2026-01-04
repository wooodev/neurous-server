-- 1. 날짜 및 시간 정밀도 수정 (LocalDateTime 매핑 이슈 해결)
ALTER TABLE content MODIFY COLUMN content_date DATETIME(6) NOT NULL;
ALTER TABLE content MODIFY COLUMN batch_time DATETIME(6) NOT NULL;

-- 2. 본문 타입 확장 (LONGTEXT 매핑 이슈 해결)
ALTER TABLE content MODIFY COLUMN content_body LONGTEXT NOT NULL;

-- 3. 누락된 컬럼 추가 (Entity 필드 일치)
ALTER TABLE content
    ADD COLUMN news_article_id BIGINT;
