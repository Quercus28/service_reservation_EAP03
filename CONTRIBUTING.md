# Guia de contribucion - Flujo Trunk-Based

Este repositorio usa trunk-based development: main es la unica rama de larga duracion y siempre debe quedar en estado desplegable.

## Reglas del flujo

1. No se hace push directo a main. Todo cambio entra por Pull Request.
2. Ramas de corta duracion. Crea la rama desde main actualizado:
   - feature/<descripcion-corta> para funcionalidad nueva
   - fix/<descripcion-corta> para correcciones
   - Objetivo: mergear en horas o 1-2 dias, no semanas. Si la tarea es grande, dividela en PRs mas pequenos o usa feature flags para ocultar trabajo incompleto.
3. Commits pequenos y frecuentes, con mensajes claros de que cambia y por que.
4. Antes de abrir el PR, sincroniza tu rama con main (git fetch origin && git rebase origin/main o merge) para evitar conflictos grandes al final.
5. El PR debe pasar CI antes de poder mergear: las pruebas (.github/workflows/build.yml, job test) y el analisis de SonarQube Cloud con su Quality Gate (.github/workflows/sonarcloud-analysis.yml, job Build and analyze).
6. Se requiere al menos 1 aprobacion de otra persona del equipo.
7. Merge a main con merge commit (no squash, no rebase) - se conserva el historial completo de commits de la rama.
8. Borra la rama despues de mergear.

## Como correr todo localmente antes de abrir el PR

```
./gradlew build
```

Las pruebas usan H2 en memoria, no necesitas PostgreSQL corriendo para esto.

## Calidad con SonarQube Cloud

El Quality Gate (Sonar way) se evalua sobre el codigo nuevo y bloquea el merge si falla. Lo normal es corregir el codigo. Marcar un issue como Accept o False positive es una excepcion y sigue estas reglas:

1. Accept se usa cuando el issue es valido pero hay una razon tecnica para no corregirlo (por ejemplo, una libreria que exige un tipo concreto). False positive se usa cuando el riesgo que describe la regla no aplica en este contexto.
2. Todo cambio de estado lleva un comentario en el issue con la justificacion tecnica: que hace el codigo, por que la regla no aplica o no se corrige, y donde se verifico.
3. La decision la revisa otra persona del equipo antes de mergear el PR. Si el issue se acepta en un PR, se menciona en la descripcion del PR con el enlace al issue.
4. No se cambia el Quality Profile ni se agregan exclusiones de analisis o de cobertura para que el Quality Gate pase.
5. En cada entrega se revisan los issues aceptados y marcados como falso positivo (en SonarQube Cloud, Issues con los filtros Accepted y False positive) para confirmar que la justificacion sigue siendo valida. Si ya no lo es, se reabre el issue.

Decisiones vigentes:

- java:S120 (nombres de paquete): se mantiene el segmento EAP03 en mayusculas. Cada paquete nuevo bajo EAP03 genera un issue que se acepta con la misma justificacion.
- java:S2143 (java.util.Date en JwtAdapter): jjwt exige Date en issuedAt() y expiration(); el codigo trabaja con Instant y solo convierte en el borde.
- java:S4502 (CSRF deshabilitado en SecurityConfig): falso positivo. La API es stateless y el JWT se lee solo del header Authorization.

## Por que este flujo

Trunk-based development reduce el costo de integracion: al mantener las ramas cortas y pasar por CI antes de mergear, main se mantiene siempre en un estado que se puede desplegar, y se evitan los conflictos grandes de ramas de larga duracion.
