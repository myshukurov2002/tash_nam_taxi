--------------------------------------------------------------
--                  INSERT PROFILES                         --
--------------------------------------------------------------

INSERT INTO users(visibility, created_date, chat_id,
                  full_name, phone, user_role)
SELECT true, now(), 1174220995, 'Muhammad Yusuf', '+998902812345', 'SUPER_ADMIN'
    WHERE NOT EXISTS(SELECT chat_id
                     FROM users
                     WHERE chat_id = 1174220995);
