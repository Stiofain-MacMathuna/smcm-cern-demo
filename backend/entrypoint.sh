#!/bin/sh

# 1. Wait for Postgres to be ready
echo "Waiting for postgres..."
python << END
import socket
import time
import os

db_host = os.environ.get('DB_HOST', 'db')
db_port = int(os.environ.get('DB_PORT', 5432))

while True:
    try:
        with socket.create_connection((db_host, db_port), timeout=1):
            break
    except OSError:
        print("PostgreSQL not ready, waiting...")
        time.sleep(1)
END
echo "PostgreSQL started"

# 2. Apply Database Migrations
echo "Applying migrations..."
python manage.py migrate --noinput

# 3. Create or Reset Superuser
echo "Syncing demo admin credentials..."
python manage.py shell << END
from django.contrib.auth import get_user_model
User = get_user_model()
username = 'cern'
password = 'cms123'
email = 'admin@cern.ch'

user, created = User.objects.get_or_create(username=username, defaults={'email': email, 'is_superuser': True, 'is_staff': True})
user.set_password(password)
user.is_superuser = True
user.is_staff = True
user.save()

if created:
    print(f"Superuser '{username}' created.")
else:
    print(f"Superuser '{username}' updated/verified.")
END

# 4. Seed Data if the Member table is empty
echo "Checking data seed..."
    MEMBER_COUNT=$(python manage.py shell -c "from api.models import Member; print(Member.objects.count())" | tail -1)

if [ "$MEMBER_COUNT" = "0" ]; then
    echo "Database empty. Seeding 5,000 members and papers..."
    python manage.py seed_glance
else
    echo "Database already contains $MEMBER_COUNT members. Skipping seed."
fi

# 5. Start the Server
echo "Starting Gunicorn..."
exec gunicorn --bind 0.0.0.0:8000 config.wsgi:application