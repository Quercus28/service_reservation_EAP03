INSERT INTO ROL (nombre)
VALUES ('ROLE_ADMIN')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO USUARIO (
    email,
    password_hash,
    enabled,
    is_2fa_enabled,
    created_at
)
VALUES (
    'admin@administrador.com',
    '$2a$10$UAFllSq5LCFU/7Po2nQ4JOSxc4b7N.LFmkXyHlp2.6J/zZHQv7spO',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP
);

INSERT INTO USUARIO_ROL (id_usuario, id_rol)
SELECT u.id, r.id
FROM USUARIO u
JOIN ROL r ON r.nombre = 'ROLE_ADMIN'
WHERE u.email = 'admin@administrador.com'
ON CONFLICT DO NOTHING;