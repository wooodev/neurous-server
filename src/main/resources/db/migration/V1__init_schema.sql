-- 1. Users 테이블 생성
CREATE TABLE users
(
    user_id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                  VARCHAR(255) NOT NULL,
    provider              VARCHAR(50)  NOT NULL,
    provider_id           VARCHAR(255) NOT NULL,
    profile_img_file_name VARCHAR(255)          DEFAULT 'lv1_profile.png',
    email                 VARCHAR(50) UNIQUE,
    status                VARCHAR(20)           DEFAULT 'NORMAL',
    user_type             VARCHAR(25)  NOT NULL DEFAULT 'USER',
    count_interests       INT          NOT NULL DEFAULT 0,
    level                 VARCHAR(20)  NOT NULL DEFAULT 'BEGINNER',
    sign_up_complete      TINYINT(1) NOT NULL DEFAULT 0,
    notification_status   TINYINT(1) NOT NULL DEFAULT 0,
    point                 INT          NOT NULL DEFAULT 0,
    exp                   INT          NOT NULL DEFAULT 0,
    count_read_content    INT          NOT NULL DEFAULT 0,
    character_level       VARCHAR(20)  NOT NULL DEFAULT 'LEVEL_1',
    last_read_date        DATE,
    attendance_count      INT          NOT NULL DEFAULT 0,
    last_login_at         DATETIME(6) NOT NULL,
    -- BaseTimeEntity 관련 공통 컬럼 (핵심 에러 해결 지점)
    deleted               TINYINT(1) NOT NULL DEFAULT 0,
    created_at            DATETIME(6),
    updated_at            DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. UserInterest 테이블 (User 엔티티의 OneToMany 관계)
CREATE TABLE user_interest
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    interest_field VARCHAR(50), -- UserField Enum 값
    priority       VARCHAR(20), -- Priority Enum 값
    FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
