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
5. El PR debe pasar CI (./gradlew build, definido en .github/workflows/ci.yml) antes de poder mergear.
6. Se requiere al menos 1 aprobacion de otra persona del equipo.
7. Merge a main con merge commit (no squash, no rebase) - se conserva el historial completo de commits de la rama.
8. Borra la rama despues de mergear.

## Como correr todo localmente antes de abrir el PR

```
./gradlew build
```

Las pruebas usan H2 en memoria, no necesitas PostgreSQL corriendo para esto.

## Por que este flujo

Trunk-based development reduce el costo de integracion: al mantener las ramas cortas y pasar por CI antes de mergear, main se mantiene siempre en un estado que se puede desplegar, y se evitan los conflictos grandes de ramas de larga duracion.
