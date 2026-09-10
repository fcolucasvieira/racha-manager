set -e

BACKUP_DIR="$HOME/backups"
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
FILENAME="racha_manager_${TIMESTAMP}.sql"
RETENTION_DAYS=7

mkdir -p "$BACKUP_DIR"

docker exec racha-postgres pg_dump -U postgres racha_manager > "$BACKUP_DIR/$FILENAME"

find "$BACKUP_DIR" -name "racha_manager_*.sql" -mtime +$RETENTION_DAYS -delete

echo "$(date '+%Y-%m-%d %H:%M:%S') - Backup criado: $FILENAME" >> "$BACKUP_DIR/backup.log"