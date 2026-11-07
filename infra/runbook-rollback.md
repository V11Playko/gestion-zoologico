# Runbook de Rollback — CI/CD

## Cuándo usar este runbook

- El deploy falló y los contenedores en EC2 están caídos o en loop de reinicio.
- El deploy funcionó pero la aplicación tiene errores en prod (500s, timeouts, lógica rota).
- Se desplegó la imagen equivocada.

---

## Rollback en EC2 — dev

### Opción A: volver a la imagen anterior por SHA

Cada push a `dev` genera dos tags en ECR: `dev` (mutable, siempre la última) y `sha-<commit>` (inmutable).

```bash
# Conectarse a EC2 dev
ssh -i <key.pem> ec2-user@<EC2_DEV_HOST>

# Ver los últimos tags disponibles en ECR
aws ecr describe-images \
  --repository-name gestion-zoologico/zoologico-service \
  --query 'sort_by(imageDetails,&imagePushedAt)[-5:].imageTags' \
  --output table

# Reemplazar el contenedor con el SHA anterior
REGISTRY=<ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com
SHA_ANTERIOR=sha-<commit-anterior>

aws ecr get-login-password --region <REGION> \
  | docker login --username AWS --password-stdin $REGISTRY

docker pull $REGISTRY/gestion-zoologico/zoologico-service:$SHA_ANTERIOR
docker stop zoologico-service
docker rm   zoologico-service
docker run -d \
  --name zoologico-service \
  --env-file /home/ec2-user/.env \
  -p 8091:8091 \
  --restart unless-stopped \
  $REGISTRY/gestion-zoologico/zoologico-service:$SHA_ANTERIOR
```

Repetir para `messaging-service` si también está afectado.

---

## Rollback en EC2 — prod

### Opción A: re-ejecutar el workflow con la versión anterior

En GitHub → Actions → `CD — prod` → Run workflow → ingresar el tag anterior (`v1.0.0`).

Requiere aprobación manual del environment `production`.

### Opción B: rollback manual en EC2 prod

```bash
ssh -i <key.pem> ec2-user@<EC2_PROD_HOST>

REGISTRY=<ACCOUNT_ID>.dkr.ecr.<REGION>.amazonaws.com
VERSION_ANTERIOR=v1.0.0  # <- cambiar por el tag anterior

aws ecr get-login-password --region <REGION> \
  | docker login --username AWS --password-stdin $REGISTRY

docker pull $REGISTRY/gestion-zoologico/zoologico-service:$VERSION_ANTERIOR
docker stop zoologico-service
docker rm   zoologico-service
docker run -d \
  --name zoologico-service \
  --env-file /home/ec2-user/.env \
  -p 8091:8091 \
  --restart unless-stopped \
  $REGISTRY/gestion-zoologico/zoologico-service:$VERSION_ANTERIOR
```

---

## Rollback de migraciones Flyway

Flyway no soporta rollback automático. Si una migración rompió el esquema:

1. Conectarse a RDS y corregir la tabla manualmente.
2. Marcar la migración como fallida en `flyway_schema_history` si es necesario:

```sql
-- Ver estado de migraciones
SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;

-- Solo si la migración quedó a medias (success = false)
DELETE FROM flyway_schema_history WHERE version = 'X.Y.Z' AND success = false;
```

3. Revertir el script Flyway en el código y crear uno nuevo de corrección con la versión siguiente.

---

## Verificar estado post-rollback

```bash
# Health checks
curl http://<EC2_HOST>:8091/actuator/health
curl http://<EC2_HOST>:8092/actuator/health

# Logs del contenedor
docker logs --tail 50 zoologico-service
docker logs --tail 50 messaging-service

# Ver imagen corriendo
docker inspect zoologico-service | grep Image
```

---

## Contacto

Si el rollback no resuelve el problema, revisar CloudWatch Logs (HU-19) para el stack trace completo.
