-- Roles de base de datos con mínimo privilegio para EAP03.
--
-- Este archivo NO es una migración: Flyway no lo ejecuta. Se corre a mano como
-- postgres (superusuario) y se puede correr varias veces sin error.
--
-- Uso:
--   psql -U postgres -d eap03 \
--        -v pass_migraciones='...' -v pass_app='...' -v pass_reportes='...' \
--        -f db/seguridad/roles_y_permisos.sql

\set ON_ERROR_STOP on

-- Las contraseñas llegan como variables; si falta alguna, se detiene.
\if :{?pass_migraciones}
\else
  DO $$ BEGIN RAISE EXCEPTION 'Falta la variable pass_migraciones (-v pass_migraciones=...)'; END $$;
\endif
\if :{?pass_app}
\else
  DO $$ BEGIN RAISE EXCEPTION 'Falta la variable pass_app (-v pass_app=...)'; END $$;
\endif
\if :{?pass_reportes}
\else
  DO $$ BEGIN RAISE EXCEPTION 'Falta la variable pass_reportes (-v pass_reportes=...)'; END $$;
\endif

BEGIN;

-- 1. Crear los roles solo si no existen
SELECT format('CREATE ROLE %I', r)
FROM unnest(ARRAY['eap03_migraciones', 'eap03_app', 'eap03_reportes']) AS r
WHERE NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = r)
\gexec

-- Ninguno es superusuario ni puede crear bases o roles
ALTER ROLE eap03_migraciones LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD :'pass_migraciones';
ALTER ROLE eap03_app         LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD :'pass_app';
ALTER ROLE eap03_reportes    LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD :'pass_reportes';

-- 2. Quitar los permisos que PUBLIC (cualquier usuario) trae por defecto
REVOKE ALL ON DATABASE :"DBNAME" FROM PUBLIC;
REVOKE ALL ON SCHEMA public FROM PUBLIC;
GRANT CONNECT ON DATABASE :"DBNAME" TO eap03_migraciones, eap03_app, eap03_reportes;

-- 3. eap03_migraciones: dueño del esquema y de las tablas (crea y cambia tablas)
ALTER SCHEMA public OWNER TO eap03_migraciones;

SELECT format('ALTER TABLE public.%I OWNER TO eap03_migraciones', tablename)
FROM pg_tables
WHERE schemaname = 'public' AND tableowner <> 'eap03_migraciones'
\gexec

-- Secuencias sueltas (las de columnas IDENTITY cambian de dueño con su tabla)
SELECT format('ALTER SEQUENCE public.%I OWNER TO eap03_migraciones', s.relname)
FROM pg_class s
JOIN pg_namespace n ON n.oid = s.relnamespace
WHERE n.nspname = 'public' AND s.relkind = 'S'
  AND pg_get_userbyid(s.relowner) <> 'eap03_migraciones'
  AND NOT EXISTS (SELECT 1 FROM pg_depend d
                  WHERE d.objid = s.oid AND d.deptype IN ('a', 'i'))
\gexec

-- 4. eap03_app: leer y escribir datos, sin crear ni borrar tablas
GRANT USAGE ON SCHEMA public TO eap03_app;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO eap03_app;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO eap03_app;

-- Las tablas nuevas que cree eap03_migraciones también le quedan a la app
ALTER DEFAULT PRIVILEGES FOR ROLE eap03_migraciones IN SCHEMA public
    GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO eap03_app;
ALTER DEFAULT PRIVILEGES FOR ROLE eap03_migraciones IN SCHEMA public
    GRANT USAGE, SELECT ON SEQUENCES TO eap03_app;

-- 5. eap03_reportes: solo lectura
GRANT USAGE ON SCHEMA public TO eap03_reportes;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO eap03_reportes;

-- Sin acceso al historial de Flyway
REVOKE ALL ON public.flyway_schema_history FROM eap03_reportes;

-- De usuario solo ve estas columnas: nunca password_hash ni secret_2fa
REVOKE ALL ON public.usuario FROM eap03_reportes;
GRANT SELECT (id, email, enabled, is_2fa_enabled, created_at)
    ON public.usuario TO eap03_reportes;

-- A propósito NO hay ALTER DEFAULT PRIVILEGES para reportes: una tabla nueva
-- puede traer datos sensibles, así que se revisa y se vuelve a correr este script.

COMMIT;

\echo 'Roles y permisos aplicados.'
