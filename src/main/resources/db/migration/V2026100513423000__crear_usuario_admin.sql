INSERT INTO ROL (nombre)
SELECT 'ROLE_ADMIN'
WHERE NOT EXISTS (
    SELECT 1 FROM ROL WHERE nombre = 'ROLE_ADMIN'
);

INSERT INTO USUARIO (
    email,
    password_hash,
    enabled,
    is_2fa_enabled,
    created_at
)
SELECT
    'admin@administrador.com',
    '$2a$10$UAFllSq5LCFU/7Po2nQ4JOSxc4b7N.LFmkXyHlp2.6J/zZHQv7spO',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP
WHERE NOT EXISTS (
    SELECT 1 FROM USUARIO WHERE email = 'admin@administrador.com'
);

INSERT INTO USUARIO_ROL (id_usuario, id_rol)
SELECT u.id, r.id
FROM USUARIO u
JOIN ROL r ON r.nombre = 'ROLE_ADMIN'
WHERE u.email = 'admin@administrador.com'
    AND NOT EXISTS (
            SELECT 1
            FROM USUARIO_ROL ur
            WHERE ur.id_usuario = u.id
                AND ur.id_rol = r.id
    );